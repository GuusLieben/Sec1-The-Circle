package com.example.application.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonSerialize
@NoArgsConstructor
@Getter
public class Message<T> {

    @JsonProperty
    private String hash;
    @JsonProperty
    private String timestamp;
    @JsonProperty
    private T content;

    public Message(T content) {
        this.content = content;

    }
}
