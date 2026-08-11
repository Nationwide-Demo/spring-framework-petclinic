resource "aws_ecs_cluster" "main" {
  name = local.name

  setting {
    name  = "containerInsights"
    value = "enhanced"
  }
}

resource "aws_ecs_cluster_capacity_providers" "main" {
  cluster_name       = aws_ecs_cluster.main.name
  capacity_providers = ["FARGATE"]

  default_capacity_provider_strategy {
    capacity_provider = "FARGATE"
    weight            = 1
  }
}

# Service Connect namespace: this is what replaces the Eureka discovery server.
resource "aws_service_discovery_http_namespace" "main" {
  name = local.name
}

resource "aws_cloudwatch_log_group" "service" {
  for_each = merge(local.services, { api-gateway = { port = local.gateway_port, schema = null } })

  name              = "/ecs/${local.name}/${each.key}"
  retention_in_days = var.log_retention_days
}

locals {
  # ADOT collector sidecar: receives OTLP spans from the app and forwards them to X-Ray.
  otel_sidecar = {
    name      = "aws-otel-collector"
    image     = "public.ecr.aws/aws-observability/aws-otel-collector:latest"
    essential = false
    command   = ["--config=/etc/ecs/ecs-default-config.yaml"]
  }
}

resource "aws_ecs_task_definition" "backend" {
  for_each = local.services

  family                   = "${local.name}-${each.key}"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.service_cpu
  memory                   = var.service_memory
  execution_role_arn       = aws_iam_role.execution.arn
  task_role_arn            = aws_iam_role.task.arn

  container_definitions = jsonencode([
    {
      name      = each.key
      image     = "${aws_ecr_repository.service[each.key].repository_url}:${var.image_tag}"
      essential = true

      portMappings = [{
        name          = each.key
        containerPort = each.value.port
        protocol      = "tcp"
        appProtocol   = "http"
      }]

      environment = [
        { name = "SERVER_PORT", value = tostring(each.value.port) },
        { name = "DB_SCHEMA", value = each.value.schema },
        { name = "TRACING_ENABLED", value = "true" },
        { name = "OTEL_EXPORTER_OTLP_ENDPOINT", value = "http://localhost:4318/v1/traces" },
        { name = "JAVA_TOOL_OPTIONS", value = "-XX:MaxRAMPercentage=75" },
      ]

      secrets = [
        { name = "SPRING_DATASOURCE_URL", valueFrom = aws_ssm_parameter.datasource_url[each.key].arn },
        { name = "TRACING_SAMPLING_PROBABILITY", valueFrom = aws_ssm_parameter.tracing_sampling.arn },
        {
          name      = "SPRING_DATASOURCE_USERNAME",
          valueFrom = "${aws_secretsmanager_secret.service_db[each.key].arn}:username::"
        },
        {
          name      = "SPRING_DATASOURCE_PASSWORD",
          valueFrom = "${aws_secretsmanager_secret.service_db[each.key].arn}:password::"
        },
      ]

      healthCheck = {
        command     = ["CMD-SHELL", "wget -q -O - http://localhost:${each.value.port}/actuator/health | grep -q UP"]
        interval    = 15
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.service[each.key].name
          awslogs-region        = var.region
          awslogs-stream-prefix = "ecs"
        }
      }
    },
    merge(local.otel_sidecar, {
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.service[each.key].name
          awslogs-region        = var.region
          awslogs-stream-prefix = "otel"
        }
      }
    }),
  ])
}

resource "aws_ecs_service" "backend" {
  for_each = local.services

  name            = each.key
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.backend[each.key].arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  enable_execute_command             = var.environment != "prod"
  health_check_grace_period_seconds  = 0
  deployment_minimum_healthy_percent = 100
  deployment_maximum_percent         = 200

  network_configuration {
    subnets         = aws_subnet.private[*].id
    security_groups = [aws_security_group.service.id]
  }

  service_connect_configuration {
    enabled   = true
    namespace = aws_service_discovery_http_namespace.main.arn

    service {
      port_name      = each.key
      discovery_name = each.key

      client_alias {
        port     = each.value.port
        dns_name = each.key
      }
    }
  }
}

resource "aws_ecs_task_definition" "gateway" {
  family                   = "${local.name}-api-gateway"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.service_cpu
  memory                   = var.service_memory
  execution_role_arn       = aws_iam_role.execution.arn
  task_role_arn            = aws_iam_role.task.arn

  container_definitions = jsonencode([
    {
      name      = "api-gateway"
      image     = "${aws_ecr_repository.service["api-gateway"].repository_url}:${var.image_tag}"
      essential = true

      portMappings = [{
        name          = "api-gateway"
        containerPort = local.gateway_port
        protocol      = "tcp"
        appProtocol   = "http"
      }]

      environment = concat(
        [
          { name = "SERVER_PORT", value = tostring(local.gateway_port) },
          { name = "TRACING_ENABLED", value = "true" },
          { name = "OTEL_EXPORTER_OTLP_ENDPOINT", value = "http://localhost:4318/v1/traces" },
          { name = "ALLOWED_ORIGINS", value = "https://${var.frontend_domain_name}" },
        ],
        [for name, service in local.services : {
          name  = "${upper(replace(name, "-service", ""))}_SERVICE_URL"
          value = "http://${name}:${service.port}"
        }],
      )

      secrets = [
        { name = "TRACING_SAMPLING_PROBABILITY", valueFrom = aws_ssm_parameter.tracing_sampling.arn },
      ]

      healthCheck = {
        command     = ["CMD-SHELL", "wget -q -O - http://localhost:${local.gateway_port}/actuator/health | grep -q UP"]
        interval    = 15
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.service["api-gateway"].name
          awslogs-region        = var.region
          awslogs-stream-prefix = "ecs"
        }
      }
    },
    merge(local.otel_sidecar, {
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.service["api-gateway"].name
          awslogs-region        = var.region
          awslogs-stream-prefix = "otel"
        }
      }
    }),
  ])
}

resource "aws_ecs_service" "gateway" {
  name            = "api-gateway"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.gateway.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  enable_execute_command             = var.environment != "prod"
  health_check_grace_period_seconds  = 120
  deployment_minimum_healthy_percent = 100
  deployment_maximum_percent         = 200

  network_configuration {
    subnets         = aws_subnet.private[*].id
    security_groups = [aws_security_group.service.id]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.gateway.arn
    container_name   = "api-gateway"
    container_port   = local.gateway_port
  }

  service_connect_configuration {
    enabled   = true
    namespace = aws_service_discovery_http_namespace.main.arn
  }

  depends_on = [aws_lb_listener.https]
}

resource "aws_appautoscaling_target" "service" {
  for_each = merge(local.services, { api-gateway = { port = local.gateway_port, schema = null } })

  service_namespace  = "ecs"
  resource_id        = "service/${aws_ecs_cluster.main.name}/${each.key}"
  scalable_dimension = "ecs:service:DesiredCount"
  min_capacity       = var.desired_count
  max_capacity       = var.desired_count * 5

  depends_on = [aws_ecs_service.backend, aws_ecs_service.gateway]
}

resource "aws_appautoscaling_policy" "cpu" {
  for_each = aws_appautoscaling_target.service

  name               = "${each.key}-cpu"
  policy_type        = "TargetTrackingScaling"
  service_namespace  = each.value.service_namespace
  resource_id        = each.value.resource_id
  scalable_dimension = each.value.scalable_dimension

  target_tracking_scaling_policy_configuration {
    target_value = 60

    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
  }
}
