/*
 * Copyright 2002-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.util.EntityUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for {@link ClinicService}, booted with Spring Boot on the default
 * (H2) profile. Test methods that change the database state are annotated with
 * {@link Transactional @Transactional} so their transaction is rolled back afterwards.
 *
 * @author Ken Krebs
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
@SpringBootTest
class ClinicServiceTests {

    @Autowired
    protected ClinicService clinicService;

    @Test
    void shouldFindOwnersByLastName() {
        Collection<Owner> owners = this.clinicService.findOwnerByLastName("Davis");
        assertThat(owners).hasSize(2);

        owners = this.clinicService.findOwnerByLastName("Daviss");
        assertThat(owners).isEmpty();
    }

    @Test
    void shouldFindSingleOwnerWithPet() {
        Owner owner = this.clinicService.findOwnerById(1);
        assertThat(owner.getLastName()).startsWith("Franklin");
        assertThat(owner.getPets()).hasSize(1);
        assertThat(owner.getPets().get(0).getType()).isNotNull();
        assertThat(owner.getPets().get(0).getType().getName()).isEqualTo("cat");
    }

    @Test
    @Transactional
    void shouldInsertOwner() {
        Collection<Owner> owners = this.clinicService.findOwnerByLastName("Schultz");
        int found = owners.size();

        Owner owner = new Owner();
        owner.setFirstName("Sam");
        owner.setLastName("Schultz");
        owner.setAddress("4, Evans Street");
        owner.setCity("Wollongong");
        owner.setTelephone("4444444444");
        this.clinicService.saveOwner(owner);
        assertThat(owner.getId().longValue()).isNotZero();

        owners = this.clinicService.findOwnerByLastName("Schultz");
        assertThat(owners).hasSize(found + 1);
    }

    @Test
    @Transactional
    void shouldUpdateOwner() {
        Owner owner = this.clinicService.findOwnerById(1);
        String oldLastName = owner.getLastName();
        String newLastName = oldLastName + "X";

        owner.setLastName(newLastName);
        this.clinicService.saveOwner(owner);

        // retrieving new name from database
        owner = this.clinicService.findOwnerById(1);
        assertThat(owner.getLastName()).isEqualTo(newLastName);
    }

    @Test
    void shouldFindPetWithCorrectId() {
        Pet pet7 = this.clinicService.findPetById(7);
        assertThat(pet7.getName()).startsWith("Samantha");
        assertThat(pet7.getOwner().getFirstName()).isEqualTo("Jean");
    }

    @Test
    void shouldFindAllPetTypes() {
        Collection<PetType> petTypes = this.clinicService.findPetTypes();

        PetType petType1 = EntityUtils.getById(petTypes, PetType.class, 1);
        assertThat(petType1.getName()).isEqualTo("cat");
        PetType petType4 = EntityUtils.getById(petTypes, PetType.class, 4);
        assertThat(petType4.getName()).isEqualTo("snake");
    }

    @Test
    @Transactional
    void shouldInsertPetIntoDatabaseAndGenerateId() {
        Owner owner6 = this.clinicService.findOwnerById(6);
        int found = owner6.getPets().size();

        Pet pet = new Pet();
        pet.setName("bowser");
        Collection<PetType> types = this.clinicService.findPetTypes();
        pet.setType(EntityUtils.getById(types, PetType.class, 2));
        pet.setBirthDate(LocalDate.now());
        owner6.addPet(pet);
        assertThat(owner6.getPets()).hasSize(found + 1);

        this.clinicService.savePet(pet);
        this.clinicService.saveOwner(owner6);

        owner6 = this.clinicService.findOwnerById(6);
        assertThat(owner6.getPets()).hasSize(found + 1);
        // checks that id has been generated
        assertThat(pet.getId()).isNotNull();
    }

    @Test
    @Transactional
    void shouldUpdatePetName() {
        Pet pet7 = this.clinicService.findPetById(7);
        String oldName = pet7.getName();

        String newName = oldName + "X";
        pet7.setName(newName);
        this.clinicService.savePet(pet7);

        pet7 = this.clinicService.findPetById(7);
        assertThat(pet7.getName()).isEqualTo(newName);
    }

    @Test
    void shouldFindVets() {
        Collection<Vet> vets = this.clinicService.findVets();

        Vet vet = EntityUtils.getById(vets, Vet.class, 3);
        assertThat(vet.getLastName()).isEqualTo("Douglas");
        assertThat(vet.getNrOfSpecialties()).isEqualTo(2);
        assertThat(vet.getSpecialties().get(0).getName()).isEqualTo("dentistry");
        assertThat(vet.getSpecialties().get(1).getName()).isEqualTo("surgery");
    }

    @Test
    @Transactional
    void shouldAddNewVisitForPet() {
        Pet pet7 = this.clinicService.findPetById(7);
        int found = pet7.getVisits().size();
        Visit visit = new Visit();
        pet7.addVisit(visit);
        visit.setDescription("test");
        this.clinicService.saveVisit(visit);
        this.clinicService.savePet(pet7);

        pet7 = this.clinicService.findPetById(7);
        assertThat(pet7.getVisits()).hasSize(found + 1);
        assertThat(visit.getId()).isNotNull();
    }

    @Test
    void shouldFindVisitsByPetId() {
        Collection<Visit> visits = this.clinicService.findVisitsByPetId(7);
        assertThat(visits).hasSize(2);
        Visit[] visitArr = visits.toArray(new Visit[0]);
        assertThat(visitArr[0].getPet()).isNotNull();
        assertThat(visitArr[0].getDate()).isNotNull();
        assertThat(visitArr[0].getPet().getId()).isEqualTo(7);
    }

}
