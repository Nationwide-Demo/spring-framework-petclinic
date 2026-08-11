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
package org.springframework.samples.petclinic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.web.PetTypeFormatter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

/**
 * Replacement for the web tier beans of the former mvc-core-config.xml that Spring Boot does not
 * auto-configure. The JSP view resolution, the message source and the resource handlers are
 * configured through application.yml instead.
 */
@Configuration(proxyBeanMethods = false)
public class WebConfig implements WebMvcConfigurer {

    private final ClinicService clinicService;

    public WebConfig(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("welcome");
    }

    @Bean
    PetTypeFormatter petTypeFormatter() {
        return new PetTypeFormatter(this.clinicService);
    }

    /**
     * Resolves any unhandled exception to the 'exception' view, as the monolith did.
     */
    @Bean
    SimpleMappingExceptionResolver simpleMappingExceptionResolver() {
        SimpleMappingExceptionResolver resolver = new SimpleMappingExceptionResolver();
        resolver.setDefaultErrorView("exception");
        resolver.setWarnLogCategory("warn");
        return resolver;
    }

}
