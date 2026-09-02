package com.apiautomation.support;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** One row of {@code testdata/login-negative-cases.json}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginCase(
        String caseId,
        String description,
        String email,
        String password,
        int expectedStatus,
        String expectedError) {

    @Override
    public String toString() {
        return caseId + " — " + description;
    }
}
