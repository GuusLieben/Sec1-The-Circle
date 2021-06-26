package nl.guuslieben.circle.common.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonSerialize
@AllArgsConstructor
@NoArgsConstructor
public class TestObject {

    @JsonProperty
    @Getter
    private String name;

}
