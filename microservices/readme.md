# Spring PetClinic Microservices

Two independently deployable Spring Boot services extracted from the Spring Framework PetClinic WAR,
behind an API gateway:

| Module           | Port | Owns                                                | Endpoints                       |
|------------------|------|-----------------------------------------------------|---------------------------------|
| `vets-service`   | 8081 | `vets`, `specialties`, `vet_specialties`             | `/vets`, `/vets.json`, `/vets.xml` |
| `clinic-service` | 8082 | `owners`, `pets`, `types`, `visits`                  | `/`, `/owners/**`, `/oups`      |
| `api-gateway`    | 8080 | routing only                                        | everything above                |

Each service owns its own DataSource and schema/data scripts (`src/main/resources/db/{h2,mysql,postgresql}`);
no foreign key crosses the service boundary. The owner/pet/visit aggregate stays in a single service so the
`fk_pets_owners`, `fk_pets_types` and `fk_visits_pets` constraints and the JPA cascades are preserved.

The Spring XML application contexts of the monolith (`business-config.xml`, `datasource-config.xml`,
`mvc-*-config.xml`, `tools-config.xml`) are replaced by Boot auto-configuration plus a per-service
`application.yml`; only the beans Boot does not auto-configure are kept as `@Configuration` classes
(`WebConfig`, `MonitoringConfig`). Only the JPA repository implementations were carried over.

## Build

```
cd microservices
./../mvnw -B install
```

## Run

```
java -jar vets-service/target/vets-service.war
java -jar clinic-service/target/clinic-service.war
java -jar api-gateway/target/api-gateway.jar
```

Then browse [http://localhost:8080](http://localhost:8080). The gateway routes `/vets`, `/vets.json` and
`/vets.xml` to vets-service and everything else to clinic-service, so the shared navigation links
(`Home`, `Find owners`, `Veterinarians`) keep working.

The gateway targets can be overridden with the `VETS_SERVICE_URL` and `CLINIC_SERVICE_URL` environment variables.

## Persistent databases

Both services default to an in-memory H2 database populated at startup. To use MySQL or PostgreSQL, build
with the matching Maven profile (adds the JDBC driver) and activate the matching Spring profile:

```
./../mvnw -B install -P MySQL
SPRING_PROFILES_ACTIVE=mysql java -jar vets-service/target/vets-service.war
```

Review the corresponding section of each service's `application.yml` for the URL and credentials.

## Docker

```
./../mvnw -B package jib:dockerBuild -DskipTests
docker compose up
```

This produces `springcommunity/spring-petclinic-vets-service`,
`springcommunity/spring-petclinic-clinic-service` and `springcommunity/spring-petclinic-api-gateway`.
