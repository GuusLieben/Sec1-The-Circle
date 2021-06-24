package nl.guuslieben.circle.common.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;

@JsonSerialize
@AllArgsConstructor
public class TestObject {

    @JsonProperty
    private final String name;

}
