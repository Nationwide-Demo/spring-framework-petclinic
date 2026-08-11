resource "aws_lb" "main" {
  name               = local.name
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = aws_subnet.public[*].id
}

resource "aws_lb_target_group" "gateway" {
  name        = "${local.name}-gateway"
  port        = local.gateway_port
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = aws_vpc.main.id

  health_check {
    path                = "/actuator/health/readiness"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    interval            = 15
    timeout             = 5
    matcher             = "200"
  }

  deregistration_delay = 30
}

resource "aws_acm_certificate" "api" {
  domain_name       = var.api_domain_name
  validation_method = "DNS"

  lifecycle {
    create_before_destroy = true
  }
}

# Only the gateway is exposed: it is the single entry point for /api/*, and the backend
# services are reachable only through Service Connect inside the VPC.
resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.main.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-2021-06"
  certificate_arn   = aws_acm_certificate.api.arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.gateway.arn
  }
}
