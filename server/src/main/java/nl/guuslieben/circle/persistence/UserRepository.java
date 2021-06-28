package nl.guuslieben.circle.persistence;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<PersistentUser, String> {
}
