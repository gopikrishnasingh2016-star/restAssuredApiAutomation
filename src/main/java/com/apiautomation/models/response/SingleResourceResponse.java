package com.apiautomation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Body of {@code GET /unknown/{id}}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SingleResourceResponse(ResourceData data, Support support) {
}
