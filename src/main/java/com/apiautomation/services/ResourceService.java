package com.apiautomation.services;

import com.apiautomation.constants.Endpoints;
import com.apiautomation.models.response.ResourceListResponse;
import io.qameta.allure.Step;
import io.restassured.response.Response;

/** The {@code /unknown} resource collection (colours of the year). */
public class ResourceService extends BaseService {

    @Step("List resources")
    public Response getResources() {
        return get(Endpoints.RESOURCES);
    }

    @Step("List resources (typed)")
    public ResourceListResponse getResourcesAs() {
        return getResources().as(ResourceListResponse.class);
    }

    @Step("Fetch resource {id}")
    public Response getResource(int id) {
        return get(Endpoints.RESOURCE_BY_ID, id);
    }
}
