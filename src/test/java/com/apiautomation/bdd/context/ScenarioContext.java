package com.apiautomation.bdd.context;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * State shared between the step-definition classes of a single scenario.
 *
 * <p>PicoContainer creates one instance per scenario and injects it into every step class that
 * declares it as a constructor parameter, so steps stay stateless and scenarios stay isolated
 * even when they run in parallel — no static fields, no leakage between scenarios.
 */
public class ScenarioContext {

    private final Map<String, Object> attributes = new HashMap<>();
    private Response lastResponse;

    public Response lastResponse() {
        if (lastResponse == null) {
            throw new IllegalStateException("No response recorded yet — a When step must run first");
        }
        return lastResponse;
    }

    public void recordResponse(Response response) {
        this.lastResponse = response;
    }

    public void put(String key, Object value) {
        attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value == null) {
            throw new IllegalStateException("No scenario attribute named '" + key + "'");
        }
        return (T) type.cast(value);
    }
}
