package com.apiautomation.constants;

/** HTTP status codes used across the suite, named so assertions read as prose. */
public final class StatusCode {

    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int NO_CONTENT = 204;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int NOT_FOUND = 404;

    private StatusCode() {
    }
}
