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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link VisitController}
 *
 * @author Colin But
 */
@WebMvcTest(VisitController.class)
class VisitControllerTests {

    private static final int TEST_PET_ID = 1;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClinicService clinicService;

    @BeforeEach
    void setup() {
        Pet pet = new Pet();
        pet.setId(TEST_PET_ID);
        given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(pet);

        Visit visit = new Visit();
        visit.setDescription("Visit Description");
        pet.addVisit(visit);
        given(this.clinicService.findVisitsByPetId(TEST_PET_ID)).willReturn(List.of(visit));
    }

    @Test
    void shouldListVisitsOfPet() throws Exception {
        mockMvc.perform(get("/api/pets/{petId}/visits", TEST_PET_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].description").value("Visit Description"))
            .andExpect(jsonPath("$[0].petId").value(TEST_PET_ID));
    }

    @Test
    void shouldReturnNotFoundForUnknownPet() throws Exception {
        mockMvc.perform(get("/api/pets/{petId}/visits", 42))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateVisit() throws Exception {
        mockMvc.perform(post("/api/pets/{petId}/visits", TEST_PET_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"date": "2026-01-15", "description": "rabies shot"}
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.description").value("rabies shot"))
            .andExpect(jsonPath("$.petId").value(TEST_PET_ID));
    }

    @Test
    void shouldRejectVisitWithoutDescription() throws Exception {
        mockMvc.perform(post("/api/pets/{petId}/visits", TEST_PET_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"date": "2026-01-15"}
                """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].field").value("description"));
    }

}
