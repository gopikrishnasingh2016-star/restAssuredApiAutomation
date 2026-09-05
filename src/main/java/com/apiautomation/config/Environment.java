package com.apiautomation.config;

import java.util.Arrays;

/**
 * Deployment target for a run, selected with {@code -Denv=<name>} and backed by
 * {@code config/config-<name>.properties}.
 *
 * <p>Adding a new environment is a new properties file plus a constant here — never a code change
 * inside a test. This is what lets Jenkins promote the same jar from SIT to UAT to production.
 */
public enum Environment {

    /** System Integration Test — the first deployed environment a build reaches. */
    SIT,

    /** User Acceptance Test — business sign-off environment. */
    UAT,

    /** The public production API. */
    LIVE,

    /** A local WireMock server that replays the production contract; used by CI and offline work. */
    MOCK;

    public static Environment from(String value) {
        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown environment '" + value + "'. Supported: " + Arrays.toString(values())));
    }

    public String key() {
        return name().toLowerCase();
    }
}
