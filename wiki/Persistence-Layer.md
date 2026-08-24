# Persistence Layer

Four technology-agnostic interfaces live in
`org.springframework.samples.petclinic.repository`:

| Interface | Methods |
|-----------|---------|
| `OwnerRepository` | `Collection<Owner> findByLastName(String)`, `Owner findById(int)`, `void save(Owner)` |
| `PetRepository` | `List<PetType> findPetTypes()`, `Pet findById(int)`, `void save(Pet)` |
| `VetRepository` | `Collection<Vet> findAll()` |
| `VisitRepository` | `void save(Visit)`, `List<Visit> findByPetId(Integer)` |

Exactly one implementation set is active at runtime, selected by Spring profile in
`spring/business-config.xml`.

## `jpa` (default) — `repository/jpa`

Plain Jakarta Persistence: each `Jpa*RepositoryImpl` is a `@Repository` taking an
`EntityManager` through its constructor and issuing JPQL, e.g.

```java
em.createQuery("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets "
             + "WHERE owner.lastName LIKE :lastName");
em.createQuery("SELECT distinct vet FROM Vet vet left join fetch vet.specialties "
             + "ORDER BY vet.lastName, vet.firstName");
```

`save(...)` checks whether `getId() == null` (the same new-entity convention
represented by `BaseEntity.isNew()`) to choose `persist` vs `merge`. The
`LocalContainerEntityManagerFactoryBean` (persistence unit `petclinic`,
`packagesToScan = org.springframework.samples.petclinic`) plus
`JpaTransactionManager` are declared for the `jpa` and `spring-data-jpa` profiles;
`PersistenceExceptionTranslationPostProcessor` maps provider exceptions into
Spring's `DataAccessException` hierarchy.

## `spring-data-jpa` — `repository/springdatajpa`

Interfaces only — no implementation code. Each one extends both the PetClinic
interface and Spring Data's `Repository<T, Integer>`, so Spring Data derives
implementations from method names, with `@Query` used where a join fetch is needed:

```java
public interface SpringDataOwnerRepository extends OwnerRepository, Repository<Owner, Integer> {
    @Query("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName%")
    Collection<Owner> findByLastName(@Param("lastName") String lastName);
    ...
}
```

`SpringDataVetRepository` and `SpringDataVisitRepository` are empty: `findAll()` and
`findByPetId(...)` are query-derived. Enabled by `<jpa:repositories base-package=".../springdatajpa"/>`.

## `jdbc` — `repository/jdbc`

Hand-written SQL over `JdbcClient` (Spring Framework 6.1+ fluent API) with
`SimpleJdbcInsert` for inserts; the profile also declares a
`NamedParameterJdbcTemplate` bean.

Supporting types:

* `JdbcPet` — `Pet` subclass carrying the raw `type_id` / `owner_id` foreign keys
  before they are resolved into objects.
* `JdbcPetRowMapper`, `JdbcVisitRowMapper` — `RowMapper` implementations.
* `JdbcPetVisitExtractor` / `OneToManyResultSetExtractor` — build the pet→visits
  one-to-many graph from a single joined result set, which is how
  `JdbcOwnerRepositoryImpl.loadPetsAndVisits(...)` avoids N+1 queries when an owner
  is loaded.
* `JdbcVetRepositoryImpl.findAll()` loads vets, then all specialties, then the
  `vet_specialties` rows per vet.
* Owner and pet saves insert new rows or update existing rows; visit saves only
  insert new visits and throw `UnsupportedOperationException` for updates.

Transactions in this profile are managed by `DataSourceTransactionManager`.

## DataSource

`spring/datasource-config.xml` defines `dataSource` as
`org.apache.tomcat.jdbc.pool.DataSource`, configured from `jdbc.driverClassName`,
`jdbc.url`, `jdbc.username`, `jdbc.password` (resolved from
`spring/data-access.properties`, whose values come from the active Maven profile and
can be overridden by system properties). `jdbc:initialize-database` then runs
`classpath:db/${db.script}/schema.sql` followed by `data.sql` on every startup.

Under the `javaee` profile the `dataSource` is instead a JNDI lookup of
`java:comp/env/jdbc/petclinic`.
