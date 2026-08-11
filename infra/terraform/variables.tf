variable "region" {
  description = "AWS region to deploy into"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Environment name, used as a name prefix"
  type        = string
  default     = "dev"
}

variable "vpc_cidr" {
  description = "CIDR block of the VPC"
  type        = string
  default     = "10.20.0.0/16"
}

variable "image_tag" {
  description = "Tag of the service images to run"
  type        = string
  default     = "1.0.0-SNAPSHOT"
}

variable "service_cpu" {
  description = "Fargate task CPU units per service"
  type        = number
  default     = 512
}

variable "service_memory" {
  description = "Fargate task memory (MiB) per service"
  type        = number
  default     = 1024
}

variable "desired_count" {
  description = "Number of tasks per service"
  type        = number
  default     = 2
}

variable "aurora_min_capacity" {
  description = "Aurora Serverless v2 minimum ACUs"
  type        = number
  default     = 0.5
}

variable "aurora_max_capacity" {
  description = "Aurora Serverless v2 maximum ACUs"
  type        = number
  default     = 4
}

variable "api_domain_name" {
  description = "Domain name served by the ALB (CloudFront's /api/* origin)"
  type        = string
}

variable "frontend_domain_name" {
  description = "Domain name served by CloudFront"
  type        = string
}

variable "frontend_certificate_arn" {
  description = "ACM certificate for the CloudFront alias, issued in us-east-1"
  type        = string
}

variable "log_retention_days" {
  description = "CloudWatch Logs retention"
  type        = number
  default     = 30
}

locals {
  name = "petclinic-${var.environment}"

  # Each backend service owns a schema and a database role inside the shared Aurora cluster.
  services = {
    customers-service = { port = 8081, schema = "customers" }
    vets-service      = { port = 8082, schema = "vets" }
    visits-service    = { port = 8083, schema = "visits" }
  }

  gateway_port = 8080
}
