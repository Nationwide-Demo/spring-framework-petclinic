# Web Layer

Package `org.springframework.samples.petclinic.web`, component-scanned by
`spring/mvc-core-config.xml`. Views are JSPs under `src/main/webapp/WEB-INF/jsp`,
composed with JSP tag files under `WEB-INF/tags`.

## URL map

| Method & path | Handler | Result |
|---------------|---------|--------|
| `GET /` | `mvc:view-controller` in `mvc-core-config.xml` | `welcome.jsp` |
| `GET /owners/find` | `OwnerController.initFindForm` | `owners/findOwners.jsp` |
| `GET /owners` | `OwnerController.processFindForm` | search by last name: none → form with `notFound` error, one → redirect to owner, many → `owners/ownersList.jsp` |
| `GET /owners/new` | `OwnerController.initCreationForm` | `owners/createOrUpdateOwnerForm.jsp` |
| `POST /owners/new` | `OwnerController.processCreationForm` | `redirect:/owners/{id}` |
| `GET /owners/{ownerId}` | `OwnerController.showOwner` | `owners/ownerDetails.jsp` |
| `GET /owners/{ownerId}/edit` | `OwnerController.initUpdateOwnerForm` | edit form |
| `POST /owners/{ownerId}/edit` | `OwnerController.processUpdateOwnerForm` | `redirect:/owners/{ownerId}` |
| `GET /owners/{ownerId}/pets/new` | `PetController.initCreationForm` | `pets/createOrUpdatePetForm.jsp` |
| `POST /owners/{ownerId}/pets/new` | `PetController.processCreationForm` | `redirect:/owners/{ownerId}` |
| `GET /owners/{ownerId}/pets/{petId}/edit` | `PetController.initUpdateForm` | pet form |
| `POST /owners/{ownerId}/pets/{petId}/edit` | `PetController.processUpdateForm` | `redirect:/owners/{ownerId}` |
| `GET /owners/*/pets/{petId}/visits/new` | `VisitController.initNewVisitForm` | `pets/createOrUpdateVisitForm.jsp` |
| `POST /owners/{ownerId}/pets/{petId}/visits/new` | `VisitController.processNewVisitForm` | `redirect:/owners/{ownerId}` |
| `GET /owners/*/pets/{petId}/visits` | `VisitController.showVisits` | `visitList` view — note there is currently no `WEB-INF/jsp/visitList.jsp`, so this path does not render |
| `GET /vets` | `VetController.showVetList` | `vets/vetList.jsp` |
| `GET /vets.json` | `VetController.showJsonVetList` | `Vets` serialized to JSON (Jackson 3) |
| `GET /vets.xml` | `VetController.showXmlVetList` | `Vets` serialized to XML (JAXB `MarshallingView` bean `vets/vetList.xml`) |
| `GET /oups` | `CrashController.triggerException` | throws `RuntimeException` → `exception.jsp` |

`PetController` is mapped at class level to `/owners/{ownerId}` and exposes
`@ModelAttribute("owner")` (loaded from the path variable) and
`@ModelAttribute("types")` (all pet types) to its views. `VisitController` uses
`@ModelAttribute("visit")` to pre-load the pet and attach a new `Visit` before the
handler runs.

## Binding, formatting, validation

* `@InitBinder` in `OwnerController`, `PetController` (for `owner`) and
  `VisitController` calls `setDisallowedFields("id")` to block id tampering.
* `PetValidator` is registered per-request via `@InitBinder("pet")`; it requires
  name, type (for new pets) and birth date. Duplicate pet names for the same owner
  are rejected in `PetController.processCreationForm` (`duplicate` error code).
* Bean Validation annotations on the entities cover owner/visit fields.
* `PetTypeFormatter` (registered in the `conversionService`) converts between a
  `PetType` and its name, resolving names through `ClinicService.findPetTypes()`.
* Both `PetValidator` and `PetTypeFormatter` are annotated `@NullMarked` (JSpecify).

## Views and static resources

* `WEB-INF/tags`: `layout.tag` (page skeleton), `menu.tag` / `menuItem.tag`,
  `htmlHeader.tag`, `bodyHeader.tag`, `footer.tag`, `inputField.tag`,
  `selectField.tag`, `localDate.tag`, `pivotal.tag`.
* `mvc:resources` maps `/resources/**` to `src/main/webapp/resources/` and
  `/webjars/**` to WebJar content on the classpath (Bootstrap 5.3.8,
  Font Awesome 4.7.0, flatpickr 4.6.13 — declared in `pom.xml`).
* Error rendering goes through `SimpleMappingExceptionResolver` with
  `defaultErrorView = exception` and `warnLogCategory = warn`.
* Content negotiation is configured in `mvc-view-config.xml`
  (`use-not-acceptable="true"`), which is what makes the `.json` / `.xml` vet
  endpoints work alongside the HTML views.
