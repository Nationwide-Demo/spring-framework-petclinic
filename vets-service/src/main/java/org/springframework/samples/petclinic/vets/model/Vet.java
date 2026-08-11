package org.springframework.samples.petclinic.vets.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "vets")
public class Vet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name")
    @NotBlank
    private String firstName;

    @Column(name = "last_name")
    @NotBlank
    private String lastName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "vet_specialties", joinColumns = @JoinColumn(name = "vet_id"),
        inverseJoinColumns = @JoinColumn(name = "specialty_id"))
    private final Set<Specialty> specialties = new HashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Specialty> getSpecialties() {
        List<Specialty> sorted = new ArrayList<>(specialties);
        sorted.sort(Comparator.comparing(Specialty::getName, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(sorted);
    }

    public int getNrOfSpecialties() {
        return specialties.size();
    }

    public void addSpecialty(Specialty specialty) {
        specialties.add(specialty);
    }

    @Override
    public String toString() {
        return "Vet{id=%s, firstName='%s', lastName='%s'}".formatted(id, firstName, lastName);
    }

}
