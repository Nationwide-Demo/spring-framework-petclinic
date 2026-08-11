# PetClinic Microservices

PetClinic as four Spring Boot services, a React SPA, and Terraform that runs the whole thing on AWS.
This repository previously held the single-WAR, XML-configured [Spring Framework PetClinic](https://github.com/spring-petclinic/spring-framework-petclinic);
see [docs/migration.md](docs/migration.md) for what changed and why.

## Services

| Module | Port | Owns | Endpoints |
|---|---|---|---|
| `customers-service` | 8081 | owners, pets, pet types | `/owners`, `/owners/{id}`, `/owners/{id}/pets`, `/pets/{id}`, `/petTypes` |
| `vets-service` | 8082 | vets, specialties | `/vets`, `/vets/{id}`, `/specialties` |
| `visits-service` | 8083 | visits | `/pets/{petId}/visits`, `/pets/visits?petId=1&petId=2` |
| `api-gateway` | 8080 | edge routing, cross-service composition | `/api/customer/**`, `/api/vet/**`, `/api/visit/**`, `/api/gateway/owners/{id}` |
| `frontend` | 5173 | React + TypeScript SPA | — |

Each service owns its data and never reads another service's tables. The owner page needs
owners *and* visits, so the gateway composes them in `/api/gateway/owners/{id}`; if
`visits-service` is unavailable, a circuit breaker returns the owner with no visits instead of
failing the page.

## Running locally

Prerequisites: Java 17+, Node 20+, Docker.

```bash
# 1. build the services and the container images
./mvnw -DskipTests package jib:dockerBuild

# 2. start Postgres, the four services and the SPA behind nginx
docker compose up -d

# 3. open http://localhost:8000
```

For frontend work, run the SPA from Vite instead (it proxies `/api` to the gateway on 8080):

```bash
cd frontend && npm install && npm run dev   # http://localhost:5173
```

To run a single service from source against the compose Postgres:

```bash
docker compose up -d postgres
./mvnw -pl customers-service spring-boot:run
```

## Tests

```bash
./mvnw verify                 # unit tests + REST/JPA tests on a throwaway Postgres (Testcontainers)
cd frontend && npm run lint && npm run build
cd infra/terraform && terraform init -backend=false && terraform validate
```

Repository and REST tests use Testcontainers, so they exercise the same PostgreSQL and the same
Flyway migrations that run in AWS. Docker must be available.

## AWS deployment

`infra/terraform` provisions the target architecture:

| Concern | Resource |
|---|---|
| Runtime | ECS Fargate service per module, autoscaled on CPU |
| Ingress | CloudFront → (`/api/*`) ALB → `api-gateway`; only the gateway is exposed |
| Service discovery | ECS Service Connect over a Cloud Map namespace (no Eureka) |
| Configuration | SSM Parameter Store, injected as task environment (no Config Server) |
| Credentials | Secrets Manager, one credential per service, injected by the task execution role |
| Database | Aurora Serverless v2 (PostgreSQL), one schema and one role per service |
| SPA hosting | S3 (private, Origin Access Control) behind CloudFront |
| Images | ECR, pushed by CI with Jib |
| Observability | CloudWatch Logs + Container Insights, OTLP spans to X-Ray via an ADOT sidecar |

```bash
cd infra/terraform
cp terraform.tfvars.example terraform.tfvars   # domains and certificate ARN
terraform init
terraform apply
```

After the first apply, create the per-service database roles once — Terraform generates the
passwords but only the database can grant them:

```bash
export CUSTOMERS_PASSWORD=$(aws secretsmanager get-secret-value \
  --secret-id petclinic-dev/customers-service/database --query SecretString --output text | jq -r .password)
# ...same for vets and visits...
psql "$(terraform output -raw database_endpoint)" -U petclinic_admin -d petclinic -f bootstrap/roles.sql
```

CI (`.github/workflows/maven-build-main.yml`) pushes images to ECR and publishes the SPA to
S3/CloudFront once `AWS_DEPLOY_ROLE_ARN`, `AWS_REGION`, `ECR_REGISTRY`, `FRONTEND_BUCKET` and
`CLOUDFRONT_DISTRIBUTION_ID` are set as repository variables; without them it only builds and tests.

## Database configuration

Locally each service uses its own database (`customers`, `vets`, `visits`) on one Postgres
container. In AWS they share one Aurora cluster and are separated by schema and role, selected
with `DB_SCHEMA`. Both are covered by the same Flyway migrations in
`<service>/src/main/resources/db/migration`, which also seed the familiar demo data.

## Licence

The Spring PetClinic sample application is released under version 2.0 of the
[Apache License](https://www.apache.org/licenses/LICENSE-2.0).
