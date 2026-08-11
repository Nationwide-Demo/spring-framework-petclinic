package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VetTests {

    private Vet vet;

    @BeforeEach
    void setUp() {
        vet = new Vet();
        vet.setFirstName("James");
        vet.setLastName("Carter");
    }

    @Test
    void shouldHaveNoSpecialtiesInitially() {
        assertThat(vet.getSpecialties()).isEmpty();
        assertThat(vet.getNrOfSpecialties()).isZero();
    }

    @Test
    void shouldReturnSpecialtiesSortedByName() {
        vet.addSpecialty(specialty("surgery"));
        vet.addSpecialty(specialty("dentistry"));
        vet.addSpecialty(specialty("radiology"));

        assertThat(vet.getSpecialties()).extracting(Specialty::getName)
            .containsExactly("dentistry", "radiology", "surgery");
        assertThat(vet.getNrOfSpecialties()).isEqualTo(3);
    }

    private static Specialty specialty(String name) {
        Specialty specialty = new Specialty();
        specialty.setName(name);
        return specialty;
    }

}
