package org.springframework.samples.petclinic.customers.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OwnerRepository extends JpaRepository<Owner, Integer> {

    @Query("SELECT DISTINCT o FROM Owner o LEFT JOIN FETCH o.pets ORDER BY o.lastName, o.firstName")
    List<Owner> findAllWithPets();

    @Query("SELECT DISTINCT o FROM Owner o LEFT JOIN FETCH o.pets "
        + "WHERE LOWER(o.lastName) LIKE LOWER(CONCAT(:lastName, '%')) ORDER BY o.lastName, o.firstName")
    List<Owner> findByLastName(@Param("lastName") String lastName);

    @Query("SELECT o FROM Owner o LEFT JOIN FETCH o.pets WHERE o.id = :id")
    Optional<Owner> findWithPetsById(@Param("id") int id);

}
