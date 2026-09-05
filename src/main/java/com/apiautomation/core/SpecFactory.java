package com.apiautomation.core;

import com.apiautomation.auth.TokenManager;
import com.apiautomation.config.ConfigReader;
import com.apiautomation.core.filters.CorrelationIdFilter;
import com.apiautomation.core.filters.LoggingFilter;
import com.apiautomation.utils.JsonUtils;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.lessThan;

/**
 * The specification layer: one place that knows how a request to this API must look.
 *
 * <p>A spec carries the base URI from configuration, JSON content negotiation, the API key, the
 * bearer token from {@link TokenManager}, a correlation id for tracing, timeouts, and the logging
 * and reporting filters. Tests and services never assemble any of that themselves.
 *
 * <p>Specs are built per call rather than cached in a static field, because the base URI is not
 * known until runtime in the {@code mock} environment (WireMock picks a free port) and because a
 * cached spec would pin the bearer token that was valid when the suite started.
 */
public final class SpecFactory {

    private static final ConfigReader CONFIG = ConfigReader.get();

    private SpecFactory() {
    }

    /**
     * The request specification used by every service and test: host, headers, auth, tracing,
     * timeouts, logging and Allure reporting.
     */
    public static RequestSpecification request() {
        RequestSpecBuilder builder = baseBuilder();
        if (CONFIG.authEnabled()) {
            builder.addHeader("Authorization", TokenManager.get().bearerHeader());
        }
        return builder.build();
    }

    /**
     * The same specification without the bearer header. Used by {@link TokenManager} itself
     * (asking for a token cannot require a token) and by tests that assert on unauthenticated access.
     */
    public static RequestSpecification unauthenticatedRequest() {
        return baseBuilder().build();
    }

    /** Response expectations that hold for every JSON endpoint: status, content type and SLA. */
    public static ResponseSpecification jsonResponse(int expectedStatusCode) {
        return new ResponseSpecBuilder()
                .expectStatusCode(expectedStatusCode)
                .expectContentType(ContentType.JSON)
                .expectResponseTime(lessThan(CONFIG.slaResponseTime().toMillis()), TimeUnit.MILLISECONDS)
                .log(LogDetail.STATUS)
                .build();
    }

    /** Response expectations for endpoints that answer without a body, such as {@code DELETE}. */
    public static ResponseSpecification emptyResponse(int expectedStatusCode) {
        return new ResponseSpecBuilder()
                .expectStatusCode(expectedStatusCode)
                .expectResponseTime(lessThan(CONFIG.slaResponseTime().toMillis()), TimeUnit.MILLISECONDS)
                .build();
    }

    private static RequestSpecBuilder baseBuilder() {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(CONFIG.baseUri())
                .setBasePath(CONFIG.basePath())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setConfig(httpConfig())
                .addFilter(new CorrelationIdFilter())
                .addFilter(new AllureRestAssured());

        if (!CONFIG.apiKey().isBlank()) {
            builder.addHeader(CONFIG.apiKeyHeader(), CONFIG.apiKey());
        }
        if (CONFIG.logRequests()) {
            builder.addFilter(new LoggingFilter());
        }
        return builder;
    }

    private static RestAssuredConfig httpConfig() {
        return RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", (int) CONFIG.connectTimeout().toMillis())
                        .setParam("http.socket.timeout", (int) CONFIG.responseTimeout().toMillis()))
                // Serialisation and deserialisation both go through the framework's mapper, so
                // request payloads honour @JsonInclude and responses honour @JsonProperty.
                .objectMapperConfig(ObjectMapperConfig.objectMapperConfig()
                        .jackson2ObjectMapperFactory((type, charset) -> JsonUtils.mapper()));
    }
}
