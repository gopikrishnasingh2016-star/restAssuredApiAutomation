package com.apiautomation.auth;

import com.apiautomation.config.ConfigReader;
import com.apiautomation.core.SpecFactory;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Owns authentication for the whole run.
 *
 * <p>Authenticating inside each test would multiply load on the auth service by the number of
 * tests and make the suite slower and flakier than the thing it is testing. Instead the token is
 * fetched once, cached in memory, and re-fetched only when it is inside the configured refresh
 * skew of its expiry. The fetch is guarded by a lock, so a parallel suite performs exactly one
 * round trip even when twenty threads ask for a token at the same moment.
 *
 * <p>Two grant types are supported out of the box:
 * <ul>
 *   <li>{@code client_credentials} — form-encoded OAuth2, reads {@code access_token}/{@code expires_in}</li>
 *   <li>{@code password_json} — a JSON login endpoint that answers with {@code token}</li>
 * </ul>
 */
public final class TokenManager {

    private static final Logger LOG = LoggerFactory.getLogger(TokenManager.class);
    private static final ConfigReader CONFIG = ConfigReader.get();
    private static final TokenManager INSTANCE = new TokenManager();

    private final ReentrantLock lock = new ReentrantLock();
    private volatile Token token;

    private TokenManager() {
    }

    public static TokenManager get() {
        return INSTANCE;
    }

    /** The cached token, refreshed transparently when it is about to expire. */
    public Token token() {
        Token current = token;
        if (isUsable(current)) {
            return current;
        }
        lock.lock();
        try {
            // Re-check: another thread may have refreshed while this one waited for the lock.
            if (!isUsable(token)) {
                token = requestToken();
                LOG.info("Access token acquired via {} grant, valid until {}",
                        CONFIG.grantType(), token.expiresAt());
            }
            return token;
        } finally {
            lock.unlock();
        }
    }

    public String bearerHeader() {
        return token().asBearerHeader();
    }

    /** Drops the cached token; the next caller re-authenticates. */
    public void invalidate() {
        lock.lock();
        try {
            token = null;
        } finally {
            lock.unlock();
        }
    }

    private boolean isUsable(Token candidate) {
        return candidate != null && !candidate.isExpiringWithin(CONFIG.tokenRefreshSkew());
    }

    private Token requestToken() {
        Response response = "client_credentials".equalsIgnoreCase(CONFIG.grantType())
                ? clientCredentialsCall()
                : jsonLoginCall();

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(
                    "Authentication failed with status %d against %s%s%s: %s".formatted(
                            response.statusCode(), CONFIG.baseUri(), CONFIG.basePath(),
                            CONFIG.tokenEndpoint(), response.asString()));
        }

        String value = firstNonBlank(
                response.jsonPath().getString("access_token"),
                response.jsonPath().getString("token"));
        if (value == null) {
            throw new IllegalStateException(
                    "Token endpoint returned no access_token/token field: " + response.asString());
        }

        Integer expiresIn = response.jsonPath().get("expires_in");
        Duration ttl = expiresIn == null ? CONFIG.tokenTtl() : Duration.ofSeconds(expiresIn);
        return Token.lasting(value, ttl);
    }

    /** RFC 6749 §4.4 — form-encoded client credentials. */
    private Response clientCredentialsCall() {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", "client_credentials");
        form.put("client_id", CONFIG.clientId());
        form.put("client_secret", CONFIG.clientSecret());

        return RestAssured.given()
                .spec(SpecFactory.unauthenticatedRequest())
                .contentType(ContentType.URLENC)
                .formParams(form)
                .post(CONFIG.tokenEndpoint());
    }

    /** A JSON login endpoint, the shape most internal services expose. */
    private Response jsonLoginCall() {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("email", CONFIG.authUsername());
        body.put("password", CONFIG.authPassword());

        return RestAssured.given()
                .spec(SpecFactory.unauthenticatedRequest())
                .body(body)
                .post(CONFIG.tokenEndpoint());
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
