package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.Causality;
import java.util.List;
import java.util.Optional;

public interface CausalityRepository {
    Optional<Causality> findById(String id);
    List<Causality> findByOntologyId(String ontologyId);
    List<Causality> findByCauseEventId(String causeEventId);
    List<Causality> findByEffectEventId(String effectEventId);
    Causality save(Causality entity);
    void deleteById(String id);
}
