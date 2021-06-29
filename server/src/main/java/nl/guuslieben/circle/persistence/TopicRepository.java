package nl.guuslieben.circle.persistence;

import org.springframework.data.repository.CrudRepository;

public interface TopicRepository extends CrudRepository<PersistentTopic, Long> {
}
