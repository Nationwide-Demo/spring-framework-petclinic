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

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestContainer.class)
@Transactional
class OwnerResourceIntegrationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void shouldFindOwnersByLastNameIgnoringCase() throws Exception {
        mvc.perform(get("/owners").param("lastName", "davis"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].lastName", everyItem(is("Davis"))));
    }

    @Test
    void shouldReturnAllOwnersWhenNoLastNameGiven() throws Exception {
        mvc.perform(get("/owners"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(10)));
    }

    @Test
    void shouldFindOwnerWithPets() throws Exception {
        mvc.perform(get("/owners/6"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lastName").value("Coleman"))
            .andExpect(jsonPath("$.pets", hasSize(2)))
            .andExpect(jsonPath("$.pets[0].name").value("Max"))
            .andExpect(jsonPath("$.pets[0].type").value("cat"));
    }

    @Test
    void shouldReturnNotFoundForUnknownOwner() throws Exception {
        mvc.perform(get("/owners/9999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateOwner() throws Exception {
        mvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"firstName":"Sam","lastName":"Schultz","address":"4, Evans Street",
                     "city":"Wollongong","telephone":"4444444444"}"""))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.lastName").value("Schultz"));

        mvc.perform(get("/owners").param("lastName", "Schultz"))
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldRejectOwnerWithoutRequiredFields() throws Exception {
        mvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"firstName":"","lastName":"Schultz","address":"4, Evans Street",
                     "city":"Wollongong","telephone":"letters"}"""))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateOwner() throws Exception {
        mvc.perform(put("/owners/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"firstName":"George","lastName":"Franklin the 2nd","address":"110 W. Liberty St.",
                     "city":"Madison","telephone":"6085551023"}"""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lastName").value("Franklin the 2nd"));

        mvc.perform(get("/owners/1"))
            .andExpect(jsonPath("$.lastName").value("Franklin the 2nd"));
    }

}
