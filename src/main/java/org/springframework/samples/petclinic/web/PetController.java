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

import java.net.URI;
import java.util.Collection;
import java.util.Objects;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for {@link Pet} resources and their {@link PetType}s.
 *
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@RestController
@RequestMapping("/api")
public class PetController {

    private final ClinicService clinicService;

    public PetController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @InitBinder("pet")
    public void initPetBinder(WebDataBinder dataBinder) {
        dataBinder.setValidator(new PetValidator());
    }

    @GetMapping("/pettypes")
    public ResponseEntity<Collection<PetType>> listPetTypes() {
        return ResponseEntity.ok(this.clinicService.findPetTypes());
    }

    @GetMapping("/owners/{ownerId}/pets")
    public ResponseEntity<Collection<Pet>> listPets(@PathVariable int ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(owner.getPets());
    }

    @GetMapping("/pets/{petId}")
    public ResponseEntity<Pet> getPet(@PathVariable int petId) {
        Pet pet = this.clinicService.findPetById(petId);
        if (pet == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pet);
    }

    @PostMapping("/owners/{ownerId}/pets")
    public ResponseEntity<Object> createPet(@PathVariable int ownerId, @Valid @RequestBody Pet pet,
                                            BindingResult result) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return ResponseEntity.notFound().build();
        }

        pet.setId(null);
        resolvePetType(pet, result);
        if (StringUtils.hasLength(pet.getName()) && owner.getPet(pet.getName(), true) != null) {
            result.rejectValue("name", "duplicate", "already exists");
        }
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(ValidationErrorResponse.from(result));
        }

        owner.addPet(pet);
        this.clinicService.savePet(pet);
        return ResponseEntity.created(URI.create("/api/pets/" + pet.getId())).body(pet);
    }

    @PutMapping("/pets/{petId}")
    public ResponseEntity<Object> updatePet(@PathVariable int petId, @Valid @RequestBody Pet pet,
                                            BindingResult result) {
        Pet existing = this.clinicService.findPetById(petId);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        resolvePetType(pet, result);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(ValidationErrorResponse.from(result));
        }

        pet.setId(petId);
        Owner owner = existing.getOwner();
        if (owner != null) {
            owner.addPet(pet);
        }
        this.clinicService.savePet(pet);
        return ResponseEntity.ok(pet);
    }

    /**
     * The pet type may be submitted either by id or by name: replace it with the
     * persistent instance so the request cannot reference an unknown type.
     */
    private void resolvePetType(Pet pet, BindingResult result) {
        PetType submitted = pet.getType();
        if (submitted == null) {
            return;
        }

        for (PetType type : this.clinicService.findPetTypes()) {
            if (Objects.equals(type.getId(), submitted.getId())
                || Objects.equals(type.getName(), submitted.getName())) {
                pet.setType(type);
                return;
            }
        }
        result.rejectValue("type", "notFound", "not found");
    }

}
