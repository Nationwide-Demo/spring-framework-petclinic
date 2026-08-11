package org.springframework.samples.petclinic.vets.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.samples.petclinic.vets.PostgresTestContainer;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestContainer.class)
class VetResourceIntegrationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void shouldListAllVetsWithSpecialties() throws Exception {
        mvc.perform(get("/vets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(6)))
            .andExpect(jsonPath("$[0].lastName").value("Carter"))
            .andExpect(jsonPath("$[0].specialties", hasSize(0)));
    }

    @Test
    void shouldFindVetWithSortedSpecialties() throws Exception {
        mvc.perform(get("/vets/3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lastName").value("Douglas"))
            .andExpect(jsonPath("$.specialties[0]").value("dentistry"))
            .andExpect(jsonPath("$.specialties[1]").value("surgery"));
    }

    @Test
    void shouldReturnNotFoundForUnknownVet() throws Exception {
        mvc.perform(get("/vets/9999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldListSpecialties() throws Exception {
        mvc.perform(get("/specialties"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0]").value("dentistry"));
    }

}
