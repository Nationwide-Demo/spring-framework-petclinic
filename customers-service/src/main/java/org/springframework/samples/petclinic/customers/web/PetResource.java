package org.springframework.samples.petclinic.customers.web;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetRepository;
import org.springframework.samples.petclinic.customers.model.PetType;
import org.springframework.samples.petclinic.customers.model.PetTypeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PetResource {

    private final PetRepository pets;

    private final PetTypeRepository petTypes;

    private final OwnerRepository owners;

    PetResource(PetRepository pets, PetTypeRepository petTypes, OwnerRepository owners) {
        this.pets = pets;
        this.petTypes = petTypes;
        this.owners = owners;
    }

    @GetMapping("/petTypes")
    public List<PetType> findPetTypes() {
        return petTypes.findAllByOrderByNameAsc();
    }

    @GetMapping("/pets/{petId}")
    public PetDetails findPet(@PathVariable int petId) {
        return PetDetails.of(load(petId));
    }

    @PostMapping("/owners/{ownerId}/pets")
    @ResponseStatus(HttpStatus.CREATED)
    public PetDetails createPet(@PathVariable int ownerId, @Valid @RequestBody PetRequest request) {
        Owner owner = owners.findById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("Owner " + ownerId + " not found"));
        Pet pet = new Pet();
        owner.addPet(pet);
        return PetDetails.of(pets.save(apply(pet, request)));
    }

    @PutMapping("/owners/{ownerId}/pets/{petId}")
    public PetDetails updatePet(@PathVariable int ownerId, @PathVariable int petId,
                                @Valid @RequestBody PetRequest request) {
        Pet pet = load(petId);
        if (pet.getOwner() == null || pet.getOwner().getId() != ownerId) {
            throw new ResourceNotFoundException("Pet " + petId + " does not belong to owner " + ownerId);
        }
        return PetDetails.of(pets.save(apply(pet, request)));
    }

    private Pet load(int petId) {
        return pets.findById(petId)
            .orElseThrow(() -> new ResourceNotFoundException("Pet " + petId + " not found"));
    }

    private Pet apply(Pet pet, PetRequest request) {
        pet.setName(request.name());
        pet.setBirthDate(request.birthDate());
        pet.setType(petTypes.findById(request.typeId())
            .orElseThrow(() -> new ResourceNotFoundException("Pet type " + request.typeId() + " not found")));
        return pet;
    }

}
