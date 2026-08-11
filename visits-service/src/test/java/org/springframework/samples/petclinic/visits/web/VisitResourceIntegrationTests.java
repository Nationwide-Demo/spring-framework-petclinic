package org.springframework.samples.petclinic.visits.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.visits.PostgresTestContainer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestContainer.class)
@Transactional
class VisitResourceIntegrationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void shouldFindVisitsOfPetMostRecentFirst() throws Exception {
        mvc.perform(get("/pets/8/visits"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].description").value("neutered"))
            .andExpect(jsonPath("$[0].petId").value(8));
    }

    @Test
    void shouldReturnEmptyListForPetWithoutVisits() throws Exception {
        mvc.perform(get("/pets/1/visits"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldBatchVisitsByPetId() throws Exception {
        mvc.perform(get("/pets/visits").param("petId", "7", "8"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.7", hasSize(2)))
            .andExpect(jsonPath("$.8", hasSize(2)));
    }

    @Test
    void shouldCreateVisit() throws Exception {
        mvc.perform(post("/pets/1/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"date":"2024-03-04","description":"rabies shot"}"""))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.petId").value(1));

        mvc.perform(get("/pets/1/visits"))
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldRejectVisitWithoutDescription() throws Exception {
        mvc.perform(post("/pets/1/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"date":"2024-03-04","description":" "}"""))
            .andExpect(status().isBadRequest());
    }

}
