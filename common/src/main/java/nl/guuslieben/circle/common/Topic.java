package nl.guuslieben.circle.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonSerialize
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Topic {

    @JsonProperty
    @JsonInclude(Include.NON_DEFAULT)
    private long id;

    @JsonProperty
    private String name;
    @JsonProperty
    private UserData author;

    @JsonProperty
    private List<Response> responses;

}
