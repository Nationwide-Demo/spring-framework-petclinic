# Configuration and Profiles

There are two independent profile mechanisms in this project: **Spring profiles**
choose the persistence implementation, **Maven profiles** choose the database vendor
(and supply the JDBC settings + driver dependency).

## Spring profiles

| Profile | Effect | Beans / scanning |
|---------|--------|------------------|
| `jpa` (default) | Plain JPA repositories | scan `repository.jpa`, `entityManagerFactory`, `JpaTransactionManager` |
| `spring-data-jpa` | Spring Data derived repositories | `jpa:repositories` on `repository.springdatajpa`, same JPA infrastructure |
| `jdbc` | SQL repositories | scan `repository.jdbc`, `JdbcClient`, `NamedParameterJdbcTemplate`, `DataSourceTransactionManager` |
| `javaee` | Container-managed DataSource | `dataSource` becomes a JNDI lookup of `java:comp/env/jdbc/petclinic` |

The default is set in code (`PetclinicInitializer.SPRING_PROFILE = "jpa"`); override
at runtime:

```bash
mvn jetty:run-war -Dspring.profiles.active=jdbc
```

## Maven profiles (database vendor)

Each profile sets `db.script`, `jpa.database`, `jdbc.*` properties and adds the
matching JDBC driver:

| Profile | Database | JDBC URL | Credentials |
|---------|----------|----------|-------------|
| `H2` (active by default) | H2 in-memory | `jdbc:h2:mem:petclinic` | `sa` / *(empty)* |
| `HSQLDB` | HSQLDB in-memory | `jdbc:hsqldb:mem:petclinic` | `sa` / *(empty)* |
| `MySQL` | MySQL | `jdbc:mysql://localhost:3306/petclinic?useUnicode=true` | `petclinic` / `petclinic` |
| `PostgreSQL` | PostgreSQL | `jdbc:postgresql://localhost:5432/petclinic` | `postgres` / `petclinic` |
| `css` | — | — | recompiles `petclinic.css` from SCSS (unpacks the Bootstrap WebJar, runs libsass) |

```bash
mvn jetty:run-war -P MySQL
mvn generate-resources -P css
```

## Property resolution

`spring/data-access.properties` is loaded by `context:property-placeholder` with
`system-properties-mode="OVERRIDE"`, and its values are themselves Maven-filtered
(`src/main/resources` has `filtering` enabled), so the chain is:

```
Maven profile properties → resource filtering → data-access.properties
    → ${...} placeholders in datasource-config.xml / business-config.xml
    → overridable by JVM system properties (-Djdbc.url=..., etc.)
```

It also sets `jdbc.initLocation` / `jdbc.dataLocation` to
`classpath:db/${db.script}/{schema,data}.sql` and `jpa.showSql=true` (SQL is echoed
on startup by design in this sample).

## Other configuration files

| File | Purpose |
|------|---------|
| `src/main/resources/logback.xml` | Logback configuration |
| `src/main/resources/messages/messages*.properties` | i18n bundles: default, `en`, `de`, `es` |
| `src/main/webapp/WEB-INF/jetty-web.xml` | Jetty-specific context settings |
| `src/main/webapp/WEB-INF/no-spring-config-files-there.txt` | marker reminding that Spring config lives on the classpath, not in `WEB-INF` |
| `.editorconfig` | editor/formatting conventions (see the Contributing section of `readme.md`) |
| `.devcontainer/` | VS Code / Codespaces dev container definition |

## Key dependency versions (`pom.xml`)

Project version **7.0.3**; Spring Framework **7.0.8**, Spring Data BOM **2025.1.5**,
Hibernate ORM **7.4.0.Final**, Hibernate Validator **9.1.0.Final**, Jakarta Servlet
API **6.1.0**, JPA API **3.2.0**, Tomcat (Jasper/JDBC pool) **11.0.18**, Jackson
**3.2.1** (`tools.jackson`), Caffeine **3.2.4**, H2 **2.4.240**, HSQLDB **2.7.4**,
PostgreSQL driver **42.7.13**, MySQL driver **8.1.0**, AspectJ **1.9.25.1**,
Logback **1.6.1**, SLF4J **2.0.17**, JUnit Jupiter **6.1.2**, Mockito **5.23.0**,
AssertJ **3.27.7**, Jetty Maven plugin **11.0.26**.

Dependency updates land as individual Dependabot commits on `main`, so treat the
`pom.xml` properties block as the source of truth.
