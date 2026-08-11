package org.springframework.samples.petclinic.customers.web;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/owners")
class OwnerResource {

    private final OwnerRepository owners;

    OwnerResource(OwnerRepository owners) {
        this.owners = owners;
    }

    @GetMapping
    public List<OwnerDetails> findOwners(@RequestParam(required = false) String lastName) {
        List<Owner> found = lastName == null || lastName.isBlank()
            ? owners.findAllWithPets()
            : owners.findByLastName(lastName);
        return found.stream().map(OwnerDetails::of).toList();
    }

    @GetMapping("/{ownerId}")
    public OwnerDetails findOwner(@PathVariable int ownerId) {
        return OwnerDetails.of(load(ownerId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OwnerDetails createOwner(@Valid @RequestBody OwnerRequest request) {
        return OwnerDetails.of(owners.save(apply(new Owner(), request)));
    }

    @PutMapping("/{ownerId}")
    public OwnerDetails updateOwner(@PathVariable int ownerId, @Valid @RequestBody OwnerRequest request) {
        return OwnerDetails.of(owners.save(apply(load(ownerId), request)));
    }

    private Owner load(int ownerId) {
        return owners.findWithPetsById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("Owner " + ownerId + " not found"));
    }

    private Owner apply(Owner owner, OwnerRequest request) {
        owner.setFirstName(request.firstName());
        owner.setLastName(request.lastName());
        owner.setAddress(request.address());
        owner.setCity(request.city());
        owner.setTelephone(request.telephone());
        return owner;
    }

}
