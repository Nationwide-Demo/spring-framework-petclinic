# Architecture

## Layers

```
 Browser
   │  HTTP
   ▼
 DispatcherServlet  ── JSP views (WEB-INF/jsp) + JSP tag files (WEB-INF/tags)
   │
 web/            @Controller  (OwnerController, PetController, VisitController,
   │                           VetController, CrashController)
   ▼
 service/        ClinicService / ClinicServiceImpl   @Transactional @Cacheable
   │
   ▼
 repository/     OwnerRepository, PetRepository, VetRepository, VisitRepository
   │             implemented by exactly one of:
   │               repository/jpa            (profile `jpa`, default)
   │               repository/jdbc           (profile `jdbc`)
   │               repository/springdatajpa  (profile `spring-data-jpa`)
   ▼
 DataSource (org.apache.tomcat.jdbc.pool.DataSource) → H2 / HSQLDB / MySQL / PostgreSQL
```

`ClinicService` is a façade: controllers never touch repositories directly, and
transaction and cache boundaries live on `ClinicServiceImpl`.

## Bootstrap: no `web.xml`, no Spring Boot

`PetclinicInitializer` (`src/main/java/.../PetclinicInitializer.java`) extends
`AbstractDispatcherServletInitializer` and is picked up by the servlet container
(Servlet 3.0+ `ServletContainerInitializer` mechanism). It:

1. creates the **root** context from `spring/business-config.xml` +
   `spring/tools-config.xml`, with default profile `jpa`;
2. creates the **servlet** context from `spring/mvc-core-config.xml`
   (which imports `spring/mvc-view-config.xml`);
3. maps the `DispatcherServlet` to `/`;
4. registers a `CharacterEncodingFilter` with UTF-8 forced.

The persistence implementation is chosen by the active Spring profile, e.g.
`-Dspring.profiles.active=jdbc`; when nothing is set, `jpa` is the default
profile declared in `PetclinicInitializer.SPRING_PROFILE`.

## Application contexts

| File | Context | Responsibility |
|------|---------|----------------|
| `spring/business-config.xml` | root | component-scan of `service`, `tx:annotation-driven`, profile-scoped beans (`entityManagerFactory`, transaction managers, `JdbcClient`, `NamedParameterJdbcTemplate`, Spring Data `jpa:repositories`), imports `datasource-config.xml` |
| `spring/datasource-config.xml` | root | `dataSource` bean (Tomcat JDBC pool) + `jdbc:initialize-database` running `schema.sql` and `data.sql`; `javaee` profile switches to a JNDI lookup of `java:comp/env/jdbc/petclinic` |
| `spring/tools-config.xml` | root | AspectJ auto-proxying with `CallMonitoringAspect`, JMX export, cache manager (Caffeine, caches `default` and `vets`) |
| `spring/mvc-core-config.xml` | servlet | component-scan of `web`, `mvc:annotation-driven`, static resource and webjar handlers, `/` → `welcome` view, `conversionService` with `PetTypeFormatter`, `messageSource`, `SimpleMappingExceptionResolver` → `exception` view |
| `spring/mvc-view-config.xml` | servlet | `ContentNegotiatingViewResolver` setup: JSP resolver (`/WEB-INF/jsp/`, `.jsp`), bean-name resolver, JAXB2 `MarshallingView` for `vets/vetList.xml` |

## Cross-cutting concerns

* **Transactions** — `@Transactional` on `ClinicServiceImpl` methods; read-only for
  finders. Manager bean differs per profile (`JpaTransactionManager` vs
  `DataSourceTransactionManager`).
* **Caching** — `@Cacheable("vets")` on `ClinicServiceImpl.findVets()`, backed by
  `CaffeineCacheManager`.
* **Monitoring** — `util/CallMonitoringAspect` wraps `@Repository` beans
  (`@Around("within(@org.springframework.stereotype.Repository *)")`) and exposes
  call count / average call time over JMX as `petclinic:type=CallMonitor`.
  Spring Data JPA's generated repository proxies are not annotated repository
  classes, so this monitor does not cover that implementation.
* **Exception handling** — `CrashController` (`GET /oups`) intentionally throws;
  `SimpleMappingExceptionResolver` renders `WEB-INF/jsp/exception.jsp`.
* **i18n** — `messages/messages*.properties` (default, `en`, `de`, `es`) via
  `ResourceBundleMessageSource`.
