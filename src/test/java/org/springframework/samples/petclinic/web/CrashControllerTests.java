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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link CrashController}
 *
 * @author Colin But
 */
@WebMvcTest(CrashController.class)
class CrashControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnInternalServerErrorAsJson() throws Exception {
        mockMvc.perform(get("/api/oups"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message", containsString("Expected: controller used to showcase")));
    }

    @Test
    void shouldReturnNotFoundForUnknownPath() throws Exception {
        mockMvc.perform(get("/api/does-not-exist"))
            .andExpect(status().isNotFound());
    }

}
