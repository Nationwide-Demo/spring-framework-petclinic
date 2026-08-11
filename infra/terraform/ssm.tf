# Replaces the Spring Cloud Config Server: non-secret configuration lives in Parameter
# Store and is injected into the tasks as environment variables.
resource "aws_ssm_parameter" "service_url" {
  for_each = local.services

  name  = "/${local.name}/${each.key}/url"
  type  = "String"
  value = "http://${each.key}.${aws_service_discovery_http_namespace.main.name}:${each.value.port}"
}

resource "aws_ssm_parameter" "datasource_url" {
  for_each = local.services

  name = "/${local.name}/${each.key}/datasource-url"
  type = "String"
  value = format(
    "jdbc:postgresql://%s:%s/%s?currentSchema=%s",
    aws_rds_cluster.main.endpoint,
    aws_rds_cluster.main.port,
    aws_rds_cluster.main.database_name,
    each.value.schema,
  )
}

resource "aws_ssm_parameter" "tracing_sampling" {
  name  = "/${local.name}/tracing-sampling-probability"
  type  = "String"
  value = var.environment == "prod" ? "0.1" : "1.0"
}
