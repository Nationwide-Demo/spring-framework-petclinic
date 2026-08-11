resource "aws_security_group" "alb" {
  name        = "${local.name}-alb"
  description = "Public entry point for the PetClinic API"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "HTTPS from CloudFront"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${local.name}-alb" }
}

resource "aws_security_group" "service" {
  name        = "${local.name}-service"
  description = "PetClinic Fargate tasks"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "Gateway traffic from the load balancer"
    from_port       = local.gateway_port
    to_port         = local.gateway_port
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  ingress {
    description = "Service Connect traffic between services"
    from_port   = 8081
    to_port     = 8083
    protocol    = "tcp"
    self        = true
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${local.name}-service" }
}

resource "aws_security_group" "database" {
  name        = "${local.name}-database"
  description = "Aurora cluster, reachable only from the tasks"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.service.id]
  }

  tags = { Name = "${local.name}-database" }
}
