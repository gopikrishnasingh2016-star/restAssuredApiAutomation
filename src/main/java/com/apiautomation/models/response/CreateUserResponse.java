package com.apiautomation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Body of {@code POST /users}. The API echoes the payload and adds server-side fields. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateUserResponse(String name, String job, String id, String createdAt) {
}
