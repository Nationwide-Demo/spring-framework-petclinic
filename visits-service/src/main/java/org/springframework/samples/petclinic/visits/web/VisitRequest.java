package org.springframework.samples.petclinic.visits.web;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VisitRequest(
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
    @NotBlank @Size(max = 255) String description) {
}
