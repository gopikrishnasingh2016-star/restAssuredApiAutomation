package com.apiautomation.services;

import com.apiautomation.constants.Endpoints;
import com.apiautomation.models.request.AuthRequest;
import com.apiautomation.models.response.AuthResponse;
import io.qameta.allure.Step;
import io.restassured.response.Response;

/** Login and registration endpoints. */
public class AuthService extends BaseService {

    @Step("Log in as {payload.email}")
    public Response login(AuthRequest payload) {
        return post(payload, Endpoints.LOGIN);
    }

    @Step("Register {payload.email}")
    public Response register(AuthRequest payload) {
        return post(payload, Endpoints.REGISTER);
    }

    /** Convenience for tests that need a token and do not care about the raw response. */
    @Step("Obtain an auth token for {payload.email}")
    public String tokenFor(AuthRequest payload) {
        AuthResponse response = login(payload).as(AuthResponse.class);
        if (response.token() == null || response.token().isBlank()) {
            throw new IllegalStateException("Login did not return a token: " + response);
        }
        return response.token();
    }
}
