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
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link OwnerController}
 *
 * @author Colin But
 */
@WebMvcTest(OwnerController.class)
class OwnerControllerTests {

    private static final int TEST_OWNER_ID = 1;

    private static final String GEORGE_JSON = """
        {
          "firstName": "George",
          "lastName": "Franklin",
          "address": "110 W. Liberty St.",
          "city": "Madison",
          "telephone": "6085551023"
        }
        """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClinicService clinicService;

    private Owner george;

    @BeforeEach
    void setup() {
        george = new Owner();
        george.setId(TEST_OWNER_ID);
        george.setFirstName("George");
        george.setLastName("Franklin");
        george.setAddress("110 W. Liberty St.");
        george.setCity("Madison");
        george.setTelephone("6085551023");
        given(this.clinicService.findOwnerById(TEST_OWNER_ID)).willReturn(george);
    }

    @Test
    void shouldListOwnersByLastName() throws Exception {
        given(this.clinicService.findOwnerByLastName("Franklin")).willReturn(List.of(george));

        mockMvc.perform(get("/api/owners").param("lastName", "Franklin"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(TEST_OWNER_ID))
            .andExpect(jsonPath("$[0].lastName").value("Franklin"));
    }

    @Test
    void shouldListAllOwnersWhenNoLastNameGiven() throws Exception {
        given(this.clinicService.findOwnerByLastName("")).willReturn(List.of(george, new Owner()));

        mockMvc.perform(get("/api/owners"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldGetOwner() throws Exception {
        mockMvc.perform(get("/api/owners/{ownerId}", TEST_OWNER_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("George"))
            .andExpect(jsonPath("$.lastName").value("Franklin"))
            .andExpect(jsonPath("$.address").value("110 W. Liberty St."))
            .andExpect(jsonPath("$.city").value("Madison"))
            .andExpect(jsonPath("$.telephone").value("6085551023"));
    }

    @Test
    void shouldReturnNotFoundForUnknownOwner() throws Exception {
        mockMvc.perform(get("/api/owners/{ownerId}", 42))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateOwner() throws Exception {
        mockMvc.perform(post("/api/owners")
            .contentType(MediaType.APPLICATION_JSON)
            .content(GEORGE_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.lastName").value("Franklin"));
    }

    @Test
    void shouldRejectInvalidOwner() throws Exception {
        mockMvc.perform(post("/api/owners")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"firstName": "Joe", "lastName": "Bloggs", "city": "London"}
                """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[*].field", hasItems("address", "telephone")));
    }

    @Test
    void shouldUpdateOwner() throws Exception {
        mockMvc.perform(put("/api/owners/{ownerId}", TEST_OWNER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(GEORGE_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(TEST_OWNER_ID));
    }

    @Test
    void shouldNotUpdateUnknownOwner() throws Exception {
        mockMvc.perform(put("/api/owners/{ownerId}", 42)
            .contentType(MediaType.APPLICATION_JSON)
            .content(GEORGE_JSON))
            .andExpect(status().isNotFound());
    }

}
