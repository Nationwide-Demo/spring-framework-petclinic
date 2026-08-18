# Testing

Tests use JUnit 5 (Jupiter 6.x), the Spring TestContext framework, Mockito, AssertJ
and Hamcrest. Surefire only picks up classes named `*Tests`.

## Layout

| Test | Scope | Wiring |
|------|-------|--------|
| `model/OwnerTests`, `model/PetTests`, `model/VetTests` | entity behaviour (sorting, back-references, specialty counts) | plain unit tests |
| `model/ValidatorTests` | Bean Validation constraints | `LocalValidatorFactoryBean` |
| `service/AbstractClinicServiceTests` | the whole service + repository stack against the seeded in-memory database | abstract base with all assertions |
| `service/ClinicServiceJpaTests` | JPA implementation | `@SpringJUnitConfig("classpath:spring/business-config.xml")` + `@ActiveProfiles("jpa")` |
| `service/ClinicServiceJdbcTests` | JDBC implementation | same config, `@ActiveProfiles("jdbc")` |
| `service/ClinicServiceSpringDataJpaTests` | Spring Data implementation | same config, `@ActiveProfiles("spring-data-jpa")` |
| `web/OwnerControllerTests`, `PetControllerTests`, `VisitControllerTests`, `VetControllerTests`, `CrashControllerTests` | controllers via MockMvc | `@SpringJUnitWebConfig({"classpath:spring/mvc-test-config.xml", "classpath:spring/mvc-core-config.xml"})` |
| `web/PetTypeFormatterTests` | formatter parse/print | Mockito mock of `ClinicService` |

The three `ClinicService*Tests` subclasses are the reason the repository interfaces
exist: the same assertions must hold for all three persistence implementations.

## Web test configuration

`src/test/resources/spring/mvc-test-config.xml` replaces `ClinicService` with a
Mockito mock (created through `Mockito.mock` as a factory method and wrapped in a
`ProxyFactoryBean`), so controller tests exercise the real Spring MVC
infrastructure — real formatters, validators, view resolution — with a stubbed
service layer.

Test resources are configured so Spring config files can sit next to their test
class (`src/test/java` is declared as a `testResource` directory).

## Running

```bash
mvn -B test                                  # everything
mvn -B test -Dtest=OwnerControllerTests      # single class
mvn -B verify                                # tests + WAR + JaCoCo XML report
```
