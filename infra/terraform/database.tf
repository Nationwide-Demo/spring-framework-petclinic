resource "aws_db_subnet_group" "main" {
  name       = local.name
  subnet_ids = aws_subnet.private[*].id
}

resource "aws_rds_cluster" "main" {
  cluster_identifier = local.name
  engine             = "aurora-postgresql"
  engine_mode        = "provisioned"
  engine_version     = "16.4"
  database_name      = "petclinic"

  master_username             = "petclinic_admin"
  manage_master_user_password = true

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.database.id]

  storage_encrypted               = true
  backup_retention_period         = 7
  preferred_backup_window         = "03:00-04:00"
  enabled_cloudwatch_logs_exports = ["postgresql"]
  skip_final_snapshot             = var.environment != "prod"
  final_snapshot_identifier       = var.environment == "prod" ? "${local.name}-final" : null

  serverlessv2_scaling_configuration {
    min_capacity = var.aurora_min_capacity
    max_capacity = var.aurora_max_capacity
  }
}

resource "aws_rds_cluster_instance" "writer" {
  identifier         = "${local.name}-writer"
  cluster_identifier = aws_rds_cluster.main.id
  instance_class     = "db.serverless"
  engine             = aws_rds_cluster.main.engine
  engine_version     = aws_rds_cluster.main.engine_version
}

# One credential per service. The roles and per-schema grants themselves are applied once
# with infra/terraform/bootstrap/roles.sql, which reads these secrets.
resource "random_password" "service_db" {
  for_each = local.services

  length  = 32
  special = false
}

resource "aws_secretsmanager_secret" "service_db" {
  for_each = local.services

  name                    = "${local.name}/${each.key}/database"
  recovery_window_in_days = var.environment == "prod" ? 30 : 0
}

resource "aws_secretsmanager_secret_version" "service_db" {
  for_each = local.services

  secret_id = aws_secretsmanager_secret.service_db[each.key].id

  secret_string = jsonencode({
    username = each.value.schema
    password = random_password.service_db[each.key].result
  })
}
