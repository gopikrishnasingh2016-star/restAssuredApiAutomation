package com.apiautomation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Body of {@code PUT|PATCH /users/{id}}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateUserResponse(String name, String job, String updatedAt) {
}
