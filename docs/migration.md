# Monolith → microservices on AWS

## What the monolith was

A single WAR: Spring Framework 7 with XML container config, JSP/JSTL views, a `ClinicService`
facade over one relational schema, three interchangeable persistence implementations
(JDBC / plain JPA / Spring Data JPA), Jetty or Tomcat, H2 in-memory by default.

## What replaced it

| Before | After | Why |
|---|---|---|
| One WAR | Four Spring Boot 3 services (`customers`, `vets`, `visits`, `api-gateway`) | Independent deploys; each bounded context owns its data |
| XML container config | Boot auto-configuration + `application.yml` | Container config is not per-environment config; env vars and SSM are |
| JSP/JSTL + custom tags | React + TypeScript SPA on S3/CloudFront | Server-rendered views cannot be split across services; the services became pure JSON APIs |
| `ClinicService` facade | Per-service REST resources | The facade was the seam that hid the coupling; splitting it exposed the real boundaries |
| Three persistence implementations | Spring Data JPA only | The duplicates existed to demo config swapping, not to be run in production |
| `schema.sql` + `data.sql` per vendor | Flyway migrations per service | Schema ownership follows service ownership, and migrations are the deploy mechanism |
| `visits.pet_id` foreign key | `pet_id` as a plain column | Pets live in another service; referential integrity moved to the application boundary |
| `CallMonitoringAspect` | Actuator + Micrometer + OTLP spans to X-Ray | Managed observability, no hand-rolled instrumentation |
| Jetty/Tomcat on a host | ECS Fargate | No servers or Kubernetes control plane to operate for four services |
| H2 / MySQL / PostgreSQL profiles | Aurora Serverless v2 PostgreSQL, schema + role per service | One cluster is cheaper than three, and schema-scoped roles keep the services isolated |
| Credentials in Maven profiles | Secrets Manager, injected by the ECS execution role | Credentials never live in the repo or the image |

## Where the joins went

The monolith rendered the owner page from a single query graph (`owner → pets → visits`). Pets and
visits are now owned by different services, so `api-gateway` composes them at
`/api/gateway/owners/{ownerId}`: one call to `customers-service`, then one *batched* call to
`visits-service` (`/pets/visits?petId=…&petId=…`) rather than one call per pet. That call is wrapped
in a Resilience4j circuit breaker with a 2s time limiter — if `visits-service` is down, the owner and
their pets still render with empty visit lists instead of the page failing.

## AWS services chosen, and the ones rejected

Chosen: ECS Fargate, ALB, ECS Service Connect (Cloud Map), SSM Parameter Store, Secrets Manager,
Aurora Serverless v2, ECR, S3 + CloudFront, CloudWatch Logs and Container Insights, X-Ray via an
ADOT sidecar, Terraform, GitHub Actions with OIDC.

Rejected, deliberately:

- **Lambda** — a long-lived JVM request path fits Fargate better than per-request cold starts.
- **EKS** — Kubernetes operational overhead is not justified for four services.
- **DynamoDB** — the domain is relational and the existing seed data stays reusable.
- **Eureka / Spring Cloud Config Server / Zipkin** — Service Connect, Parameter Store and X-Ray
  cover the same needs without services to run and patch.
- **API Gateway (HTTP API) in front of the ALB** — worth adding only when usage plans or edge JWT
  authorization are needed; the ALB alone is cheaper for this traffic shape.

## What was intentionally dropped

- The JDBC and plain-JPA persistence implementations, and the `spring-data-jpa` / `jdbc` / `jpa`
  Maven profiles that selected them.
- The JSP views, custom tags, and the `webjars`/`wro4j` CSS pipeline. The compiled
  `petclinic.css` and its fonts were kept as static SPA assets so the UI still looks the same.
- `CallMonitoringAspect` and the JMX/monitoring XML wiring, replaced by Actuator metrics.
- The SonarCloud step in CI, which pointed at the upstream project key and cannot report for this
  fork; add a Nationwide project key to restore it.

## Not done here

- No AWS resources were applied: the Terraform is authored, formatted and `terraform validate`-clean,
  but never planned against an account.
- Per-service database roles need `infra/terraform/bootstrap/roles.sql` run once after the first
  apply; Terraform generates the passwords but cannot grant them.
- DNS records (Route 53) for the CloudFront and ALB aliases are left to whoever owns the zone.
- No authentication: the monolith had none, and the SPA/API keep that behaviour. Adding Cognito or
  an ALB OIDC action would be the natural next step.
