package nl.guuslieben.circle.users;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nl.guuslieben.circle.common.User;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PersistentUser {

    @Id
    private String email;
    private String name;
    private String password;
    private String cert;

    public static PersistentUser of(User user, String cert) {
        return new PersistentUser(
                user.getEmail(),
                user.getName(),
                user.getPassword(),
                cert
        );
    }

}
