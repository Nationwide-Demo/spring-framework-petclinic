/*
 * Copyright 2002-2022 the original author or authors.
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

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.util.EntityUtils;

/**
 * Integration tests of the vets service on top of its own schema.
 */
@SpringBootTest
class VetServiceTests {

    @Autowired
    private VetService vetService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldFindVets() {
        Collection<Vet> vets = this.vetService.findVets();

        Vet vet = EntityUtils.getById(vets, Vet.class, 3);
        assertThat(vet.getLastName()).isEqualTo("Douglas");
        assertThat(vet.getNrOfSpecialties()).isEqualTo(2);
        assertThat(vet.getSpecialties().get(0).getName()).isEqualTo("dentistry");
        assertThat(vet.getSpecialties().get(1).getName()).isEqualTo("surgery");
    }

    @Test
    void shouldCacheVets() {
        Collection<Vet> vets = this.vetService.findVets();

        assertThat(this.cacheManager.getCache("vets").get(SimpleKey.EMPTY).get()).isEqualTo(vets);
    }

}
