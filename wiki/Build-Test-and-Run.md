# Build, Test and Run

## Prerequisites

* JDK **17** or newer (the build enforces `requireJavaVersion` ≥ 17 and CI also
  builds on 21)
* Maven **3.8.4+** (enforced by `maven-enforcer-plugin`)
* A servlet container only if you deploy the WAR yourself (Jetty 11+ / Tomcat 11+);
  the Jetty Maven plugin is enough for local runs

## Commands

```bash
mvn -B verify           # full build → target/petclinic.war (finalName = petclinic)
mvn -B test             # unit + Spring integration tests (surefire includes **/*Tests.java)
mvn jetty:run-war       # run locally at http://localhost:8080
mvn generate-resources -P css   # regenerate petclinic.css from SCSS
mvn jib:build           # publish the Docker image (needs registry credentials)
```

`mvn` is preferred over `./mvnw` in this environment: the wrapper downloads Maven
3.8.4 from a rate-limited endpoint. The default Maven goal is `install`
(`build/defaultGoal`).

## Running against a real database

The default `H2` profile creates an in-memory database and re-runs
`db/h2/schema.sql` + `db/h2/data.sql` at every startup, so no external service is
needed. For persistent databases:

```bash
docker run -e MYSQL_USER=petclinic -e MYSQL_PASSWORD=petclinic \
           -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=petclinic \
           -p 3306:3306 -d mysql:5.7.8
mvn jetty:run-war -P MySQL

docker run --name postgres-petclinic -e POSTGRES_PASSWORD=petclinic \
           -e POSTGRES_DB=petclinic -p 5432:5432 -d postgres:9.6.0
mvn jetty:run-war -P PostgreSQL
```

Vendor-specific setup notes are in `db/mysql/petclinic_db_setup_mysql.txt` and
`db/postgresql/petclinic_db_setup_postgresql.txt`.

Switch the persistence implementation independently of the vendor:

```bash
mvn jetty:run-war -Dspring.profiles.active=jdbc            # or spring-data-jpa
```

The Maven database profile and the Spring persistence profile are independent:
for example, `-P PostgreSQL -Dspring.profiles.active=jdbc` uses PostgreSQL with
the JDBC repositories.

## Build plugins worth knowing

| Plugin | Role |
|--------|------|
| `maven-compiler-plugin` | Java 17 target, `-parameters` enabled (required since Spring Framework 6.1) |
| `maven-surefire-plugin` | runs `**/*Tests.java` only |
| `maven-war-plugin` | WAR without `web.xml` (`failOnMissingWebXml=false`) |
| `jacoco-maven-plugin` | coverage report (XML) at `prepare-package`, consumed by SonarCloud |
| `maven-enforcer-plugin` | minimum Maven 3.8.4 / Java 17 |
| `jetty-maven-plugin` | `jetty:run-war` for local runs |
| `jib-maven-plugin` | container image on `jetty:11.0-jdk17`, pushed as `springcommunity/spring-framework-petclinic` |
| `libsass-maven-plugin` + `maven-dependency-plugin` | `css` profile: SCSS → CSS with the Bootstrap WebJar as include path |

## Working in an IDE

Import `pom.xml` as a Maven project. Run `mvn generate-resources` once so
`src/main/webapp/resources/css/petclinic.css` exists, then either run
`jetty:run-war` or configure a Jetty/Tomcat run configuration deploying
`target/petclinic.war`. A dev container definition is available under
`.devcontainer/`.

## Load testing

`src/test/jmeter/petclinic_test_plan.jmx` is a JMeter plan for the main pages.
