package nl.guuslieben.circle.persistence;

import org.springframework.data.repository.CrudRepository;

public interface ResponseRepository extends CrudRepository<PersistentResponse, Long> {
}
