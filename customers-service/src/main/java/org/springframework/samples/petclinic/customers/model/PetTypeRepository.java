package org.springframework.samples.petclinic.customers.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PetTypeRepository extends JpaRepository<PetType, Integer> {

    List<PetType> findAllByOrderByNameAsc();

}
