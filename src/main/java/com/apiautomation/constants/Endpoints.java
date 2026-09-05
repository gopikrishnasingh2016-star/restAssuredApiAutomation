package com.apiautomation.constants;

/**
 * Every path the framework knows about lives here — tests and services never
 * hard-code URLs, which keeps an API rename a one-line change.
 */
public final class Endpoints {

    public static final String USERS = "/users";
    public static final String USER_BY_ID = "/users/{id}";
    public static final String RESOURCES = "/unknown";
    public static final String RESOURCE_BY_ID = "/unknown/{id}";
    public static final String LOGIN = "/login";
    public static final String REGISTER = "/register";

    private Endpoints() {
    }
}
