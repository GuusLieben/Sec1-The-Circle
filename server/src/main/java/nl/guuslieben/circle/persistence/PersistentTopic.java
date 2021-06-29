package nl.guuslieben.circle.persistence;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "topics")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PersistentTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false)
    private PersistentUser author;

    @OneToMany(mappedBy = "topic")
    private List<PersistentResponse> responses;

    private String name;

    public PersistentTopic(PersistentUser author, String name) {
        this.author = author;
        this.responses = new ArrayList<>();
        this.name = name;
    }
}
