output "frontend_bucket" {
  description = "S3 bucket the SPA bundle is uploaded to"
  value       = aws_s3_bucket.frontend.bucket
}

output "cloudfront_distribution_id" {
  description = "Distribution to invalidate after a frontend deploy"
  value       = aws_cloudfront_distribution.main.id
}

output "cloudfront_domain_name" {
  description = "CloudFront domain to point the frontend DNS record at"
  value       = aws_cloudfront_distribution.main.domain_name
}

output "alb_dns_name" {
  description = "ALB domain to point the API DNS record at"
  value       = aws_lb.main.dns_name
}

output "ecr_repository_urls" {
  description = "Where CI pushes the service images"
  value       = { for name, repository in aws_ecr_repository.service : name => repository.repository_url }
}

output "database_endpoint" {
  description = "Aurora writer endpoint"
  value       = aws_rds_cluster.main.endpoint
}

output "database_master_secret_arn" {
  description = "Secret holding the master credentials, needed to run bootstrap/roles.sql"
  value       = aws_rds_cluster.main.master_user_secret[0].secret_arn
}
