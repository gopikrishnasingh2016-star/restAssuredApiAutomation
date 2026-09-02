package com.apiautomation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Body of {@code GET /unknown}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ResourceListResponse(
        int page,
        @JsonProperty("per_page") int perPage,
        int total,
        @JsonProperty("total_pages") int totalPages,
        List<ResourceData> data,
        Support support) {
}
