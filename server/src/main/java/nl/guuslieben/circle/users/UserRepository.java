package nl.guuslieben.circle.users;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<PersistentUser, String> {
}
