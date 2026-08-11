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
package org.springframework.samples.petclinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the PetClinic REST service.
 * <p>
 * Component scanning, transaction management, the datasource, the JPA
 * {@code EntityManagerFactory} and the Spring Data JPA repositories are all provided by
 * Spring Boot auto-configuration, driven by {@code application.properties}.
 */
@SpringBootApplication
public class PetClinicApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetClinicApplication.class, args);
    }

}
