package com.apiautomation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Body of {@code GET /users/{id}}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SingleUserResponse(UserData data, Support support) {
}
