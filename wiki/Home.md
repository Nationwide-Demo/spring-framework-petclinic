# Spring Framework PetClinic Wiki

Documentation for this repository, generated from the current state of the code
(`pom.xml` version **7.0.3**, Spring Framework **7.0.8**, Java **17** baseline).

PetClinic is a sample veterinary clinic management web application: it manages
pet **owners**, their **pets**, the **visits** of those pets, and the **vets**
(with their specialties) of the clinic.

Unlike the canonical [spring-projects/spring-petclinic](https://github.com/spring-projects/spring-petclinic)
(Spring Boot + Thymeleaf), this variant deliberately keeps:

* plain **Spring Framework** configuration (XML application contexts, no Spring Boot),
* a **3-layer architecture**: presentation (Spring MVC + JSP) → service → repository,
* a **WAR** packaging deployed on a servlet container (Jetty 11+ / Tomcat 11+),
* **three interchangeable persistence implementations** (JPA, JDBC, Spring Data JPA)
  selected by Spring profile.

## Pages

| Page | Contents |
|------|----------|
| [Architecture](Architecture) | Layers, bootstrap sequence, application contexts, cross-cutting concerns |
| [Domain Model](Domain-Model) | Entities, inheritance hierarchy, validation, database schema |
| [Web Layer](Web-Layer) | Controllers, URL map, views, formatters/validators, JSON & XML endpoints |
| [Persistence Layer](Persistence-Layer) | Repository interfaces and the JPA / JDBC / Spring Data JPA implementations |
| [Configuration and Profiles](Configuration-and-Profiles) | Spring XML files, profiles, Maven profiles, properties |
| [Build, Test and Run](Build-Test-and-Run) | Maven commands, databases, CSS generation, IDE setup |
| [Testing](Testing) | Test suite layout and how each test slice is wired |
| [CI and Docker](CI-and-Docker) | GitHub Actions workflows, SonarCloud, Jib image |

## Quick start

```bash
mvn jetty:run-war          # http://localhost:8080 , in-memory H2 seeded at startup
mvn -B test                # run the test suite
mvn -B verify              # build target/petclinic.war
```

Notes on this fork's environment: use `mvn` rather than `./mvnw` (the wrapper's
Maven 3.8.4 download is rate-limited). The default profile is `H2` (in-memory)
and the default persistence profile is `jpa`.
