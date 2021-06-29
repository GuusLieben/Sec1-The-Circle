package nl.guuslieben.circle.persistence;

import org.springframework.data.repository.CrudRepository;

public interface EventRepository extends CrudRepository<PersistentEvent, Long> {
}
