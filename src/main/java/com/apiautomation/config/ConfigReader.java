package com.apiautomation.config;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

/**
 * Single source of truth for framework configuration.
 *
 * <p>Values are resolved with the following precedence (first match wins):
 * <ol>
 *   <li>JVM system property — {@code -Dbase.uri=...}</li>
 *   <li>Environment variable — {@code BASE_URI=...}</li>
 *   <li>{@code config/config-<env>.properties} (environment overlay)</li>
 *   <li>{@code config/config.properties} (shared defaults)</li>
 * </ol>
 *
 * <p>The resolution is intentionally done on every read so that values set at runtime
 * (for example the port of a dynamically started stub server) are picked up immediately.
 */
public final class ConfigReader {

    private static final String DEFAULTS = "config/config.properties";
    private static final ConfigReader INSTANCE = new ConfigReader();

    private final Properties defaults;
    private final Properties overlay;
    private final Environment environment;

    private ConfigReader() {
        this.defaults = load(DEFAULTS, true);
        this.environment = Environment.from(
                firstNonBlank(System.getProperty("env"), System.getenv("ENV"), defaults.getProperty("env", "live")));
        this.overlay = load("config/config-" + environment.key() + ".properties", false);
    }

    public static ConfigReader get() {
        return INSTANCE;
    }

    public Environment environment() {
        return environment;
    }

    public String baseUri() {
        return required("base.uri");
    }

    public String basePath() {
        return value("base.path", "");
    }

    public String apiKeyHeader() {
        return value("api.key.header", "x-api-key");
    }

    public String apiKey() {
        return value("api.key", "");
    }

    public Duration connectTimeout() {
        return Duration.ofMillis(longValue("http.connect.timeout.ms", 10_000));
    }

    public Duration responseTimeout() {
        return Duration.ofMillis(longValue("http.response.timeout.ms", 30_000));
    }

    /** Maximum acceptable response time asserted by the shared response specification. */
    public Duration slaResponseTime() {
        return Duration.ofMillis(longValue("sla.response.time.ms", 5_000));
    }

    public boolean logRequests() {
        return booleanValue("logging.requests.enabled", true);
    }

    /** Number of extra attempts a failed test gets before it is reported as failed. */
    public int retryCount() {
        return (int) longValue("retry.count", 0);
    }

    public int mockPort() {
        return (int) longValue("mock.port", 0);
    }

    // ------------------------------------------------------------------
    // Authentication (consumed by TokenManager)
    // ------------------------------------------------------------------

    /** When false the framework sends no bearer token — useful for open or stubbed endpoints. */
    public boolean authEnabled() {
        return booleanValue("auth.enabled", false);
    }

    /** Path of the token endpoint, relative to {@link #baseUri()} + {@link #basePath()}. */
    public String tokenEndpoint() {
        return value("auth.token.endpoint", "/login");
    }

    /** {@code client_credentials} for a standard OAuth2 flow, {@code password_json} for a JSON login. */
    public String grantType() {
        return value("auth.grant.type", "password_json");
    }

    public String clientId() {
        return value("auth.client.id", "");
    }

    public String clientSecret() {
        return value("auth.client.secret", "");
    }

    public String authUsername() {
        return value("auth.username", "");
    }

    public String authPassword() {
        return value("auth.password", "");
    }

    /** Lifetime assumed when the token endpoint does not return {@code expires_in}. */
    public Duration tokenTtl() {
        return Duration.ofSeconds(longValue("auth.token.ttl.seconds", 900));
    }

    /** How long before expiry the cached token is proactively refreshed. */
    public Duration tokenRefreshSkew() {
        return Duration.ofSeconds(longValue("auth.token.refresh.skew.seconds", 60));
    }

    public String value(String key, String fallback) {
        String resolved = firstNonBlank(
                System.getProperty(key),
                System.getenv(toEnvKey(key)),
                overlay.getProperty(key),
                defaults.getProperty(key));
        return resolved == null ? fallback : resolved.trim();
    }

    public String required(String key) {
        String resolved = value(key, null);
        if (resolved == null || resolved.isBlank()) {
            throw new IllegalStateException(
                    "Missing required configuration key '" + key + "' for environment " + environment);
        }
        return resolved;
    }

    private long longValue(String key, long fallback) {
        String raw = value(key, null);
        return raw == null ? fallback : Long.parseLong(raw);
    }

    private boolean booleanValue(String key, boolean fallback) {
        String raw = value(key, null);
        return raw == null ? fallback : Boolean.parseBoolean(raw);
    }

    private static String toEnvKey(String key) {
        return key.toUpperCase().replace('.', '_');
    }

    private static String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }
        return null;
    }

    private static Properties load(String resource, boolean mandatory) {
        Properties properties = new Properties();
        try (InputStream stream = ConfigReader.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                if (mandatory) {
                    throw new IllegalStateException("Configuration file not found on classpath: " + resource);
                }
                return properties;
            }
            properties.load(stream);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read configuration file: " + resource, e);
        }
    }
}
