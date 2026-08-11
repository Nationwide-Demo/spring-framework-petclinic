package org.springframework.samples.petclinic.customers.web;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PetRequest(
    @NotBlank String name,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate birthDate,
    @NotNull Integer typeId) {
}
