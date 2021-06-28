package nl.guuslieben.circle.persistence;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "responses")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PersistentResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private PersistentUser author;

    @ManyToOne
    @JoinColumn(name = "topicId")
    private PersistentTopic topic;

    private String content;

    public PersistentResponse(PersistentUser author, PersistentTopic topic, String content) {
        this.author = author;
        this.topic = topic;
        this.content = content;
    }
}
