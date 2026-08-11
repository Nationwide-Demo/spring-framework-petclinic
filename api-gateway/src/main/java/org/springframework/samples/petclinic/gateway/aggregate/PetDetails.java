package org.springframework.samples.petclinic.gateway.aggregate;

import java.time.LocalDate;
import java.util.List;

public record PetDetails(Integer id, String name, LocalDate birthDate, Integer typeId, String type,
                         List<VisitDetails> visits) {

    PetDetails withVisits(List<VisitDetails> visits) {
        return new PetDetails(id, name, birthDate, typeId, type, visits);
    }

}
