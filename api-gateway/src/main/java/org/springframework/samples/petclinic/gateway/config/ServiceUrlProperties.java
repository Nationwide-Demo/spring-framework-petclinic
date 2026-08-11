package org.springframework.samples.petclinic.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Upstream base URLs. On ECS these resolve through Service Connect (Cloud Map) DNS names,
 * which is why no client-side service registry is needed.
 */
@ConfigurationProperties(prefix = "petclinic.services")
public record ServiceUrlProperties(String customers, String vets, String visits) {
}
