package com.apiautomation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Body of {@code GET /users?page=n} — a paginated collection. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserListResponse(
        int page,
        @JsonProperty("per_page") int perPage,
        int total,
        @JsonProperty("total_pages") int totalPages,
        List<UserData> data,
        Support support) {
}
