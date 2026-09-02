package com.apiautomation.support;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** One row of {@code testdata/create-user-cases.json}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateUserCase(String caseId, String description, String name, String job) {

    @Override
    public String toString() {
        return caseId + " — " + description;
    }
}
