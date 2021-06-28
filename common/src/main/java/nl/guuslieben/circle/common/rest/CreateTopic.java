package nl.guuslieben.circle.common.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nl.guuslieben.circle.common.UserData;

@JsonSerialize
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateTopic {

    @JsonProperty
    @JsonInclude(Include.NON_DEFAULT)
    private long id;

    @JsonProperty
    private String name;
    @JsonProperty
    private UserData author;

}
