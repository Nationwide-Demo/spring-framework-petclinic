package org.springframework.samples.petclinic.gateway.aggregate;

import java.time.LocalDate;

public record VisitDetails(Integer id, Integer petId, LocalDate date, String description) {
}
