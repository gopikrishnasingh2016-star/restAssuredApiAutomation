package com.apiautomation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Marketing block the public API attaches to every payload. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Support(String url, String text) {
}
