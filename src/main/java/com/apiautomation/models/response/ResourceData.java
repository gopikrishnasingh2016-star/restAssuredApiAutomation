package com.apiautomation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** A colour-of-the-year resource returned by {@code GET /unknown}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ResourceData(
        int id,
        String name,
        int year,
        String color,
        @JsonProperty("pantone_value") String pantoneValue) {
}
