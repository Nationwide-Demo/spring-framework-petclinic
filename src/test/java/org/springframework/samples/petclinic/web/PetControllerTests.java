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
package org.springframework.samples.petclinic.web;

import static org.hamcrest.Matchers.hasItems;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for the {@link PetController}
 *
 * @author Colin But
 */
@WebMvcTest(PetController.class)
class PetControllerTests {

    private static final int TEST_OWNER_ID = 1;
    private static final int TEST_PET_ID = 1;

    private static final String BETTY_JSON = """
        {"name": "Betty", "birthDate": "2015-02-12", "type": {"name": "hamster"}}
        """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClinicService clinicService;

    @BeforeEach
    void setup() {
        PetType hamster = new PetType();
        hamster.setId(3);
        hamster.setName("hamster");
        given(this.clinicService.findPetTypes()).willReturn(List.of(hamster));
        given(this.clinicService.findOwnerById(TEST_OWNER_ID)).willReturn(new Owner());

        Pet pet = new Pet();
        pet.setId(TEST_PET_ID);
        pet.setName("Leo");
        pet.setBirthDate(LocalDate.of(2010, 9, 7));
        pet.setType(hamster);
        given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(pet);
    }

    @Test
    void shouldListPetTypes() throws Exception {
        mockMvc.perform(get("/api/pettypes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("hamster"));
    }

    @Test
    void shouldListPetsOfOwner() throws Exception {
        mockMvc.perform(get("/api/owners/{ownerId}/pets", TEST_OWNER_ID))
            .andExpect(status().isOk());
    }

    @Test
    void shouldGetPet() throws Exception {
        mockMvc.perform(get("/api/pets/{petId}", TEST_PET_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Leo"))
            .andExpect(jsonPath("$.birthDate").value("2010-09-07"))
            .andExpect(jsonPath("$.type.name").value("hamster"));
    }

    @Test
    void shouldReturnNotFoundForUnknownPet() throws Exception {
        mockMvc.perform(get("/api/pets/{petId}", 42))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreatePet() throws Exception {
        mockMvc.perform(post("/api/owners/{ownerId}/pets", TEST_OWNER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(BETTY_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Betty"))
            .andExpect(jsonPath("$.type.id").value(3));
    }

    @Test
    void shouldNotCreatePetForUnknownOwner() throws Exception {
        mockMvc.perform(post("/api/owners/{ownerId}/pets", 42)
            .contentType(MediaType.APPLICATION_JSON)
            .content(BETTY_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectPetWithoutTypeAndBirthDate() throws Exception {
        mockMvc.perform(post("/api/owners/{ownerId}/pets", TEST_OWNER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name": "Betty"}
                """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[*].field", hasItems("type", "birthDate")));
    }

    @Test
    void shouldRejectUnknownPetType() throws Exception {
        mockMvc.perform(post("/api/owners/{ownerId}/pets", TEST_OWNER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name": "Betty", "birthDate": "2015-02-12", "type": {"name": "dragon"}}
                """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].field").value("type"))
            .andExpect(jsonPath("$.errors[0].code").value("notFound"));
    }

    @Test
    void shouldUpdatePet() throws Exception {
        mockMvc.perform(put("/api/pets/{petId}", TEST_PET_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(BETTY_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(TEST_PET_ID))
            .andExpect(jsonPath("$.name").value("Betty"));
    }

    @Test
    void shouldRejectPetUpdateWithoutName() throws Exception {
        mockMvc.perform(put("/api/pets/{petId}", TEST_PET_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"birthDate": "2015-02-12", "type": {"name": "hamster"}}
                """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

}
