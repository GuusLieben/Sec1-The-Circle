package nl.guuslieben.circle.persistence;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "events")
@AllArgsConstructor
@NoArgsConstructor
@JsonSerialize
public class PersistentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty
    private long id;

    @JsonProperty
    private String timestamp;
    @JsonProperty
    private String title;
    @JsonProperty
    @JsonInclude(Include.NON_NULL)
    private String json;
    @JsonProperty
    private String actor;

    public PersistentEvent(String timestamp, String title, String json, String actor) {
        this.timestamp = timestamp;
        this.title = title;
        this.json = json;
        this.actor = actor;
    }
}
