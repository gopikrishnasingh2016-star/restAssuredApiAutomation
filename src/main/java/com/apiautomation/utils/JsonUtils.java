package com.apiautomation.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;

/** Thin Jackson wrapper: object mapping plus loading fixtures from the classpath. */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private JsonUtils() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to serialise " + value, e);
        }
    }

    public static String toPrettyJson(String rawJson) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(MAPPER.readTree(rawJson));
        } catch (Exception e) {
            // Not JSON (empty body, HTML error page, ...) — return it untouched for the log.
            return rawJson;
        }
    }

    /** Reads a JSON fixture from {@code src/test/resources} into the requested type. */
    public static <T> T readFromClasspath(String resource, TypeReference<T> type) {
        try (InputStream stream = JsonUtils.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalArgumentException("Test data file not found on classpath: " + resource);
            }
            return MAPPER.readValue(stream, type);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read test data file: " + resource, e);
        }
    }
}
