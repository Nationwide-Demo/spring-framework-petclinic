# Domain Model

Package `org.springframework.samples.petclinic.model`. Entities are annotated with
Jakarta Persistence and Jakarta Bean Validation (Hibernate ORM 7.x, Hibernate
Validator 9.x).

## Inheritance hierarchy

```
BaseEntity      @MappedSuperclass  id (IDENTITY), isNew()
 ├── NamedEntity  @MappedSuperclass  name
 │    ├── Pet       @Entity @Table("pets")
 │    ├── PetType   @Entity @Table("types")
 │    └── Specialty @Entity @Table("specialties")
 └── Person       @MappedSuperclass  firstName, lastName  (@NotEmpty)
      ├── Owner     @Entity @Table("owners")
      └── Vet       @Entity @Table("vets")
```

`Vets` is not an entity: it is a JAXB-annotated wrapper holding a `List<Vet>`, used
so the vet list can be marshalled to JSON/XML as a single root object.

## Entities

| Entity | Fields | Associations | Validation |
|--------|--------|--------------|------------|
| `Owner` | `address`, `city`, `telephone` | `@OneToMany(cascade = ALL, mappedBy = "owner") Set<Pet>` | `@NotEmpty` on address/city/telephone, `@Digits(fraction = 0, integer = 10)` on telephone |
| `Pet` | `birthDate` (`LocalDate`, `@DateTimeFormat("yyyy/MM/dd")`) | `@ManyToOne PetType type`, `@ManyToOne Owner owner`, `@OneToMany(cascade = ALL, fetch = EAGER) Set<Visit> visits` | enforced by `web/PetValidator` (name, type, birth date required) |
| `Visit` | `date` (stored as `visit_date`), `description` | `@ManyToOne Pet pet` | `@NotEmpty` description |
| `Vet` | inherited names | `@ManyToMany(fetch = EAGER)` to `Specialty` via join table `vet_specialties` | — |
| `PetType`, `Specialty` | `name` only | — | — |

### Collection idioms

Collections are exposed as sorted, unmodifiable lists while the mutable `Set` stays
`protected` for the persistence provider:

* `Owner.getPets()` → sorted by pet name, case-insensitive; mutate with `addPet(Pet)`
  (which also sets the back-reference).
* `Pet.getVisits()` → sorted by visit date, most recent first; mutate with `addVisit(Visit)`.
* `Owner.getPet(String name[, boolean ignoreNew])` is used by `PetController` to reject
  duplicate pet names for the same owner.
* `Vet.getSpecialties()` → sorted by specialty name, plus `getNrOfSpecialties()`
  (used by the vets JSP).

## Database schema

Schema and seed data live in `src/main/resources/db/<vendor>/{schema.sql,data.sql}`
for `h2`, `hsqldb`, `mysql` and `postgresql`; the vendor directory is selected by the
Maven profile property `db.script` and executed at startup by
`jdbc:initialize-database` in `datasource-config.xml`.

```
owners(id, first_name, last_name, address, city, telephone)
types(id, name)
pets(id, name, birth_date, type_id → types, owner_id → owners)
visits(id, pet_id → pets, visit_date, description)
vets(id, first_name, last_name)
specialties(id, name)
vet_specialties(vet_id → vets, specialty_id → specialties)
```

Seed data (`db/<vendor>/data.sql`): 6 vets, 3 specialties (`radiology`, `surgery`,
`dentistry`), 6 pet types (`cat`, `dog`, `lizard`, `snake`, `bird`, `hamster`),
10 owners, 13 pets and 4 visits. `EntityUtils.getById(...)` is a small helper used
mostly by tests and JDBC mapping code to look up an entity in a collection by id.
