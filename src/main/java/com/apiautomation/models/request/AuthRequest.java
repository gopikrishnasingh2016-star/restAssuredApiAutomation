package com.apiautomation.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Payload for {@code POST /login} and {@code POST /register}.
 * Nulls are omitted on serialisation so negative tests can send genuinely
 * incomplete payloads rather than explicit {@code null} values.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthRequest(String email, String password) {

    public static AuthRequest of(String email, String password) {
        return new AuthRequest(email, password);
    }

    public static AuthRequest withoutPassword(String email) {
        return new AuthRequest(email, null);
    }

    public static AuthRequest withoutEmail(String password) {
        return new AuthRequest(null, password);
    }
}
