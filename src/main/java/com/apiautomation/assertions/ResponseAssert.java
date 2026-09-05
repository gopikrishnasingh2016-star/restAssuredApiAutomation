package com.apiautomation.assertions;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.assertj.core.api.AbstractAssert;

import java.time.Duration;

/**
 * Fluent, API-aware assertions built on AssertJ.
 *
 * <p>Failures read like a bug report rather than {@code expected:<200> but was:<404>}: every
 * message carries the request that produced it and the body that came back.
 *
 * <pre>{@code
 * assertThat(response)
 *     .hasStatusCode(200)
 *     .isJson()
 *     .matchesSchema("schemas/single-user.json")
 *     .respondsWithin(Duration.ofSeconds(3));
 * }</pre>
 */
public class ResponseAssert extends AbstractAssert<ResponseAssert, Response> {

    public ResponseAssert(Response actual) {
        super(actual, ResponseAssert.class);
    }

    public static ResponseAssert assertThat(Response actual) {
        return new ResponseAssert(actual);
    }

    public ResponseAssert hasStatusCode(int expected) {
        isNotNull();
        int actualCode = actual.statusCode();
        if (actualCode != expected) {
            failWithMessage("Expected status code <%d> but was <%d>.%n%s", expected, actualCode, context());
        }
        return this;
    }

    public ResponseAssert isJson() {
        isNotNull();
        String contentType = actual.contentType();
        if (contentType == null || !contentType.toLowerCase().contains("json")) {
            failWithMessage("Expected a JSON content type but was <%s>.%n%s", contentType, context());
        }
        return this;
    }

    public ResponseAssert hasEmptyBody() {
        isNotNull();
        String body = actual.asString();
        if (body != null && !body.isBlank() && !"{}".equals(body.trim())) {
            failWithMessage("Expected an empty body but was <%s>.%n%s", body, context());
        }
        return this;
    }

    public ResponseAssert respondsWithin(Duration limit) {
        isNotNull();
        long actualMillis = actual.timeIn(java.util.concurrent.TimeUnit.MILLISECONDS);
        if (actualMillis > limit.toMillis()) {
            failWithMessage("Expected a response within <%d ms> but it took <%d ms>.%n%s",
                    limit.toMillis(), actualMillis, context());
        }
        return this;
    }

    public ResponseAssert hasHeader(String name, String expectedValue) {
        isNotNull();
        String actualValue = actual.header(name);
        if (actualValue == null || !actualValue.contains(expectedValue)) {
            failWithMessage("Expected header <%s> to contain <%s> but was <%s>.%n%s",
                    name, expectedValue, actualValue, context());
        }
        return this;
    }

    /** Validates the body against a JSON schema stored on the classpath, e.g. {@code schemas/user.json}. */
    public ResponseAssert matchesSchema(String classpathSchema) {
        isNotNull();
        try {
            actual.then().assertThat().body(JsonSchemaValidator.matchesJsonSchemaInClasspath(classpathSchema));
        } catch (AssertionError error) {
            failWithMessage("Body does not match schema <%s>:%n%s%n%s", classpathSchema, error.getMessage(), context());
        }
        return this;
    }

    /** Asserts a value addressed by a GPath/JSONPath expression, e.g. {@code data.first_name}. */
    public ResponseAssert hasField(String jsonPath, Object expectedValue) {
        isNotNull();
        Object actualValue = actual.jsonPath().get(jsonPath);
        if (actualValue == null || !actualValue.toString().equals(String.valueOf(expectedValue))) {
            failWithMessage("Expected field <%s> to be <%s> but was <%s>.%n%s",
                    jsonPath, expectedValue, actualValue, context());
        }
        return this;
    }

    public ResponseAssert hasNonBlankField(String jsonPath) {
        isNotNull();
        Object actualValue = actual.jsonPath().get(jsonPath);
        if (actualValue == null || actualValue.toString().isBlank()) {
            failWithMessage("Expected field <%s> to be present and non-blank but was <%s>.%n%s",
                    jsonPath, actualValue, context());
        }
        return this;
    }

    private String context() {
        return """
                --- response ---
                status : %s
                time   : %d ms
                body   : %s""".formatted(
                actual.statusLine(),
                actual.timeIn(java.util.concurrent.TimeUnit.MILLISECONDS),
                abbreviate(actual.asString()));
    }

    private static String abbreviate(String body) {
        if (body == null || body.isBlank()) {
            return "<empty>";
        }
        return body.length() <= 1_500 ? body : body.substring(0, 1_500) + "... [truncated]";
    }
}
