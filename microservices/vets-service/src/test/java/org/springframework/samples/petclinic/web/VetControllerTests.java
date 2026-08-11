package org.springframework.samples.petclinic.web;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.service.VetService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.xml.HasXPath.hasXPath;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the {@link VetController}
 */
@WebMvcTest(VetController.class)
class VetControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VetService vetService;

    @BeforeEach
    void setup() {
        Vet james = new Vet();
        james.setFirstName("James");
        james.setLastName("Carter");
        james.setId(1);
        Vet helen = new Vet();
        helen.setFirstName("Helen");
        helen.setLastName("Leary");
        helen.setId(2);
        Specialty radiology = new Specialty();
        radiology.setId(1);
        radiology.setName("radiology");
        helen.addSpecialty(radiology);
        given(this.vetService.findVets()).willReturn(Lists.newArrayList(james, helen));
    }

    @Test
    void testShowVetListHtml() throws Exception {
        mockMvc.perform(get("/vets"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("vets"))
            .andExpect(view().name("vets/vetList"));
    }

    @Test
    void testShowResourcesVetList() throws Exception {
        ResultActions actions = mockMvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        actions.andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.vetList[0].id").value(1));
    }

    @Test
    void testShowVetListXml() throws Exception {
        mockMvc.perform(get("/vets.xml").accept(MediaType.APPLICATION_XML))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_XML_VALUE))
            .andExpect(content().node(hasXPath("/vets/vet[id=1]/id")));
    }

}
