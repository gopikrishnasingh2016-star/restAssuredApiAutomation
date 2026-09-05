package com.apiautomation.auth;

import java.time.Duration;
import java.time.Instant;

/**
 * An access token plus the instant it stops being usable.
 *
 * @param value    the raw token as issued by the authorisation server
 * @param expiresAt absolute expiry, derived from {@code expires_in} or from configuration
 */
public record Token(String value, Instant expiresAt) {

    public static Token lasting(String value, Duration ttl) {
        return new Token(value, Instant.now().plus(ttl));
    }

    /** True when the token is already expired or will expire inside {@code skew}. */
    public boolean isExpiringWithin(Duration skew) {
        return Instant.now().plus(skew).isAfter(expiresAt);
    }

    public String asBearerHeader() {
        return "Bearer " + value;
    }

    /** Never let a token reach a log or a report. */
    @Override
    public String toString() {
        return "Token[value=***, expiresAt=" + expiresAt + "]";
    }
}
