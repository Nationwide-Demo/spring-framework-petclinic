# CI and Docker

## GitHub Actions

| Workflow | Trigger | Steps |
|----------|---------|-------|
| `.github/workflows/maven-build-main.yml` | push to `main` | JDK 17 and 21 matrix (`adopt`, Maven cache) → `./mvnw -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar` with `-Dsonar.projectKey=spring-petclinic_spring-framework-petclinic -Dsonar.organization=spring-petclinic` |
| `.github/workflows/maven-build-pull-request.yml` | PR opened / synchronize / reopened | same JDK 17 + 21 matrix → `./mvnw -B verify` |

Both need `GITHUB_TOKEN`; the main-branch workflow additionally needs `SONAR_TOKEN`.
Quality gate and coverage badges in `readme.md` point at SonarCloud
(`sonar.host.url = https://sonarcloud.io`, organization `spring-petclinic`);
coverage comes from the JaCoCo XML report produced during `prepare-package`.

## Dependency updates

Dependabot maintains dependency versions — recent history on `main` is a stream of
version bumps (Jackson, JUnit, Logback, PostgreSQL driver, …). Because versions are
centralised in the `pom.xml` `<properties>` block, those PRs touch a single file.

## Container image

`jib-maven-plugin` builds a WAR-based image without a Dockerfile:

* base image `jetty:11.0-jdk17`
* entrypoint `java -jar /usr/local/jetty/start.jar`
* target `docker.io/springcommunity/spring-framework-petclinic`, tagged with the
  project version and `latest`

```bash
mvn jib:build                 # build and push (requires registry credentials)
docker run -p 8080:8080 springcommunity/spring-framework-petclinic
```

The image runs with the default `H2` profile, so the container is self-contained.
