# Spring PetClinic REST Service

[![Java CI with Maven](https://github.com/spring-petclinic/spring-framework-petclinic/actions/workflows/maven-build.yml/badge.svg)](https://github.com/spring-petclinic/spring-framework-petclinic/actions/workflows/maven-build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=spring-petclinic_spring-framework-petclinic&metric=alert_status)](https://sonarcloud.io/dashboard?id=spring-petclinic_spring-framework-petclinic)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=spring-petclinic_spring-framework-petclinic&metric=coverage)](https://sonarcloud.io/dashboard?id=spring-petclinic_spring-framework-petclinic)

This repository is a fork of the [spring-projects/spring-petclinic](https://github.com/spring-projects/spring-petclinic).
It is now a **Spring Boot REST service** packaged as a runnable JAR: an API-only modular monolith that keeps the
3-layer architecture (web --> service --> repository) so it can later be decomposed into `owners`, `visits` and
`vets` services.

## Running petclinic locally

### With the Maven wrapper

```
git clone https://github.com/Nationwide-Demo/spring-framework-petclinic.git
cd spring-framework-petclinic
./mvnw spring-boot:run
# For Windows : ./mvnw.cmd spring-boot:run
```

### As a runnable JAR

```
./mvnw package
java -jar target/petclinic.jar
```

### With Docker

```
docker run -p 8080:8080 springcommunity/spring-framework-petclinic
```

The service listens on [http://localhost:8080/](http://localhost:8080/) and starts with an in-memory H2 database
populated with the sample data.

## REST API

All resources are served as JSON under `/api`.

| Method | Path                            | Description                                                        |
|--------|---------------------------------|--------------------------------------------------------------------|
| GET    | `/api/owners?lastName={prefix}` | List owners, optionally filtered by last name prefix (empty = all) |
| GET    | `/api/owners/{ownerId}`         | Get a single owner with its pets, `404` when unknown               |
| POST   | `/api/owners`                   | Create an owner, `201` with the created resource                    |
| PUT    | `/api/owners/{ownerId}`         | Update an owner, `404` when unknown                                 |
| GET    | `/api/owners/{ownerId}/pets`    | List the pets of an owner                                           |
| POST   | `/api/owners/{ownerId}/pets`    | Add a pet to an owner, `201` with the created resource              |
| GET    | `/api/pets/{petId}`             | Get a single pet, `404` when unknown                                |
| PUT    | `/api/pets/{petId}`             | Update a pet, `404` when unknown                                    |
| GET    | `/api/pettypes`                 | List the available pet types                                        |
| GET    | `/api/pets/{petId}/visits`      | List the visits of a pet                                            |
| POST   | `/api/pets/{petId}/visits`      | Add a visit to a pet, `201` with the created resource               |
| GET    | `/api/vets`                     | List the vets with their specialties                                |
| GET    | `/api/oups`                     | Always fails: showcases the JSON error response                     |

Example:

```
curl http://localhost:8080/api/owners/1

curl -X POST http://localhost:8080/api/owners/1/pets \
  -H 'Content-Type: application/json' \
  -d '{"name": "Rex", "birthDate": "2020-01-02", "type": {"name": "dog"}}'
```

A pet type may be referenced either by `id` or by `name`; unknown types are rejected.

### Error responses

Bean validation (`@Valid`) failures are returned as `400` with the rejected fields:

```json
{
  "message": "Validation failed",
  "errors": [
    {"field": "lastName", "code": "NotEmpty", "message": "must not be empty"}
  ]
}
```

Any other failure is returned as `{"message": "..."}` with the matching status code, see
[RestExceptionHandler.java](src/main/java/org/springframework/samples/petclinic/web/RestExceptionHandler.java).

### Actuator

Health and info endpoints are exposed for readiness/liveness probes:

```
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/health/readiness
curl http://localhost:8080/actuator/health/liveness
```

## Database configuration

The datasource is configured through Spring profiles, one properties file per database:

| Profile              | Properties file                                                                            | Schema and data scripts        |
|----------------------|--------------------------------------------------------------------------------------------|--------------------------------|
| `h2` (default)       | [application-h2.properties](src/main/resources/application-h2.properties)                   | `src/main/resources/db/h2`     |
| `mysql`              | [application-mysql.properties](src/main/resources/application-mysql.properties)             | `src/main/resources/db/mysql`  |
| `postgresql`         | [application-postgresql.properties](src/main/resources/application-postgresql.properties)   | `src/main/resources/db/postgresql` |

```
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
java -jar target/petclinic.jar --spring.profiles.active=postgresql
```

Credentials are read from the environment so they never have to be committed. `MYSQL_URL`, `MYSQL_USER` and
`MYSQL_PASSWORD` are used by the `mysql` profile, `POSTGRES_URL`, `POSTGRES_USER` and `POSTGRES_PASSWORD` by the
`postgresql` profile; each falls back to the local development default. Any of them may also be overridden directly
with the standard `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` variables.

You could start MySQL or PostgreSQL locally with whatever installer works for your OS, or with docker:

```
docker run -e MYSQL_USER=petclinic -e MYSQL_PASSWORD=petclinic -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=petclinic -p 3306:3306 mysql:8

docker run --name postgres-petclinic -e POSTGRES_PASSWORD=petclinic -e POSTGRES_DB=petclinic -p 5432:5432 -d postgres:16
```

## Persistence layer

The service standardizes on the Spring Data JPA implementation of the repository interfaces
([springdatajpa folder](src/main/java/org/springframework/samples/petclinic/repository/springdatajpa)); the former
JDBC and plain JPA implementations have been removed. Transactions and the `vets` cache are declared on
[ClinicServiceImpl.java](src/main/java/org/springframework/samples/petclinic/service/ClinicServiceImpl.java).

## Tests

```
./mvnw verify
```

`@WebMvcTest` slices with `MockMvc` cover the REST controllers, and
[ClinicServiceTests](src/test/java/org/springframework/samples/petclinic/service/ClinicServiceTests.java) boots the
application with `@SpringBootTest` against H2.

## Working with Petclinic in your IDE

### Prerequisites

* Java 17 or newer (full JDK not a JRE)
* git command line tool (https://help.github.com/articles/set-up-git)
* Your prefered IDE: Eclipse with the m2e plugin, [Spring Tools Suite](https://spring.io/tools) or IntelliJ IDEA

### Steps

1) On the command line:

```
git clone https://github.com/Nationwide-Demo/spring-framework-petclinic.git
```

2) Import the [pom.xml](pom.xml) as an existing Maven project, then run
`org.springframework.samples.petclinic.PetClinicApplication` as a Java application.

## Publishing a Docker image

This application uses [Google Jib](https://github.com/GoogleContainerTools/jib) to build an optimized Docker image
of the runnable JAR on top of `eclipse-temurin:17-jre`, with
`org.springframework.samples.petclinic.PetClinicApplication` as entry point. The [pom.xml](pom.xml) publishes it under
the `springcommunity/spring-framework-petclinic` image name:

```
./mvnw jib:build
```

## Looking for something in particular?

| Layer                | Files                                                                                                                                                                                                                                          |
|----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Bootstrap and config | [PetClinicApplication.java](src/main/java/org/springframework/samples/petclinic/PetClinicApplication.java), [application.properties](src/main/resources/application.properties)                                                                 |
| REST controllers     | [web folder](src/main/java/org/springframework/samples/petclinic/web)                                                                                                                                                                          |
| Transactions, cache  | [ClinicServiceImpl.java](src/main/java/org/springframework/samples/petclinic/service/ClinicServiceImpl.java), [CacheConfiguration.java](src/main/java/org/springframework/samples/petclinic/CacheConfiguration.java)                             |
| Spring Data JPA      | [springdatajpa folder](src/main/java/org/springframework/samples/petclinic/repository/springdatajpa)                                                                                                                                            |

## In case you find a bug/suggested improvement for Spring Petclinic

Our issue tracker is available here: https://github.com/spring-petclinic/spring-framework-petclinic/issues

# Contributing

The [issue tracker](/issues) is the preferred channel for bug reports, features requests and submitting pull requests.

For pull requests, editor preferences are available in the [editor config](.editorconfig) for easy use in common text editors. Read more and download plugins at <http://editorconfig.org>.
