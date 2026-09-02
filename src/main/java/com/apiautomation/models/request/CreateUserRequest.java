package com.apiautomation.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Payload for {@code POST /users} and {@code PUT|PATCH /users/{id}}. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateUserRequest(String name, String job) {

    public static CreateUserRequest of(String name, String job) {
        return new CreateUserRequest(name, job);
    }

    public CreateUserRequest withJob(String newJob) {
        return new CreateUserRequest(name, newJob);
    }
}
