package org.springframework.samples.petclinic.customers.model;

import java.util.Locale;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;

class OwnerTests {

    private LocalValidatorFactoryBean validatorFactory;

    @BeforeEach
    void setUp() {
        validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.afterPropertiesSet();
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldReturnPetsSortedByNameIgnoringCase() {
        Owner owner = new Owner();
        owner.addPet(pet("buddy"));
        owner.addPet(pet("Alpha"));
        owner.addPet(pet("ZEPHYR"));

        assertThat(owner.getPets()).extracting(Pet::getName).containsExactly("Alpha", "buddy", "ZEPHYR");
    }

    @Test
    void shouldReturnEmptyUnmodifiableListWhenNoPets() {
        assertThat(new Owner().getPets()).isEmpty();
        assertThat(new Owner().getPets()).isUnmodifiable();
    }

    @Test
    void shouldLinkPetBackToOwner() {
        Owner owner = new Owner();
        Pet pet = pet("Leo");
        owner.addPet(pet);

        assertThat(pet.getOwner()).isSameAs(owner);
    }

    @Test
    void shouldNotValidateWhenFirstNameEmpty() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        Owner owner = new Owner();
        owner.setFirstName("");
        owner.setLastName("smith");
        owner.setAddress("110 W. Liberty St.");
        owner.setCity("Madison");
        owner.setTelephone("6085551023");

        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<Owner>> violations = validator.validate(owner);

        assertThat(violations).hasSize(1);
        ConstraintViolation<Owner> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath()).hasToString("firstName");
        assertThat(violation.getMessage()).isEqualTo("must not be blank");
    }

    @Test
    void shouldNotValidateWhenTelephoneIsNotNumeric() {
        Owner owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Smith");
        owner.setAddress("110 W. Liberty St.");
        owner.setCity("Madison");
        owner.setTelephone("not-a-phone");

        Set<ConstraintViolation<Owner>> violations = validatorFactory.getValidator().validate(owner);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).containsExactly("telephone");
    }

    private static Pet pet(String name) {
        Pet pet = new Pet();
        pet.setName(name);
        return pet;
    }

}
