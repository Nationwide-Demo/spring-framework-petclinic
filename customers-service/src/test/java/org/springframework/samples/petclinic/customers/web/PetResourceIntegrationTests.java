package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.PostgresTestContainer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestContainer.class)
@Transactional
class PetResourceIntegrationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void shouldListPetTypesAlphabetically() throws Exception {
        mvc.perform(get("/petTypes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(6)))
            .andExpect(jsonPath("$[0].name").value("bird"));
    }

    @Test
    void shouldFindPetById() throws Exception {
        mvc.perform(get("/pets/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Leo"))
            .andExpect(jsonPath("$.type").value("cat"))
            .andExpect(jsonPath("$.ownerId").value(1))
            .andExpect(jsonPath("$.ownerName").value("George Franklin"));
    }

    @Test
    void shouldCreatePetForOwner() throws Exception {
        mvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Rosy","birthDate":"2011-04-17","typeId":2}"""))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.type").value("dog"));

        mvc.perform(get("/owners/1"))
            .andExpect(jsonPath("$.pets", hasSize(2)));
    }

    @Test
    void shouldRejectPetWithUnknownType() throws Exception {
        mvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Rosy","birthDate":"2011-04-17","typeId":999}"""))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdatePet() throws Exception {
        mvc.perform(put("/owners/1/pets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Leo the 2nd","birthDate":"2010-09-07","typeId":1}"""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Leo the 2nd"));
    }

    @Test
    void shouldRejectUpdateWhenPetBelongsToAnotherOwner() throws Exception {
        mvc.perform(put("/owners/2/pets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Leo","birthDate":"2010-09-07","typeId":1}"""))
            .andExpect(status().isNotFound());
    }

}
