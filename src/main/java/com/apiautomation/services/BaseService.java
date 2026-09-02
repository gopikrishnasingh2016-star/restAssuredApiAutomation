package com.apiautomation.services;

import com.apiautomation.core.SpecFactory;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

/**
 * Root of the service layer — the API equivalent of a {@code BasePage} in a UI Page Object Model.
 *
 * <p>It owns the transport concerns (specification, verbs, query and path parameters) so that
 * concrete services describe <em>what</em> the API offers and tests describe <em>what</em> the
 * business expects. No test in this project ever touches {@code RestAssured} directly.
 */
public abstract class BaseService {

    /** A fresh request specification, pre-loaded with host, headers, timeouts and filters. */
    protected RequestSpecification request() {
        return RestAssured.given().spec(SpecFactory.request());
    }

    protected RequestSpecification request(Map<String, ?> queryParams) {
        return request().queryParams(queryParams);
    }

    protected Response get(String path, Object... pathParams) {
        return request().get(path, pathParams);
    }

    protected Response get(Map<String, ?> queryParams, String path, Object... pathParams) {
        return request(queryParams).get(path, pathParams);
    }

    protected Response post(Object body, String path, Object... pathParams) {
        return request().body(body).post(path, pathParams);
    }

    protected Response put(Object body, String path, Object... pathParams) {
        return request().body(body).put(path, pathParams);
    }

    protected Response patch(Object body, String path, Object... pathParams) {
        return request().body(body).patch(path, pathParams);
    }

    protected Response delete(String path, Object... pathParams) {
        return request().delete(path, pathParams);
    }
}
