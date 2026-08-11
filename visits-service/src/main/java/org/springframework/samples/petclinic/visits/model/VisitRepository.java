package org.springframework.samples.petclinic.visits.model;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRepository extends JpaRepository<Visit, Integer> {

    List<Visit> findByPetIdOrderByDateDesc(int petId);

    List<Visit> findByPetIdInOrderByDateDesc(Collection<Integer> petIds);

}
