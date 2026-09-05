package com.apiautomation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** A single user as returned by the API. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserData(
        int id,
        String email,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        String avatar) {

    public String fullName() {
        return firstName + " " + lastName;
    }
}
