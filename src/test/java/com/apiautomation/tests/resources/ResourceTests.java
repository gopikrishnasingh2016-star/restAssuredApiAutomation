package com.apiautomation.tests.resources;

import com.apiautomation.base.BaseTest;
import com.apiautomation.constants.StatusCode;
import com.apiautomation.core.SpecFactory;
import com.apiautomation.models.response.ResourceData;
import com.apiautomation.models.response.ResourceListResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static com.apiautomation.assertions.ApiAssertions.assertThat;

@Epic("Resources API")
@Feature("Read resources")
public class ResourceTests extends BaseTest {

    @Test(groups = {"smoke", "resources"},
            description = "The resource collection is returned with well-formed items")
    @Story("List resources")
    @Severity(SeverityLevel.NORMAL)
    public void shouldListResources() {
        Response response = resources.getResources();

        assertThat(response)
                .hasStatusCode(StatusCode.OK)
                .matchesSchema("schemas/resource-list.json");

        ResourceListResponse body = response.as(ResourceListResponse.class);
        assertThat(body.data()).isNotEmpty();
        assertThat(body.data()).extracting(ResourceData::color).allMatch(color -> color.matches("^#[0-9a-fA-F]{6}$"));
        assertThat(body.data()).extracting(ResourceData::year).allMatch(year -> year > 1900);
    }

    @Test(groups = {"regression", "resources"},
            description = "A single resource satisfies the shared JSON response specification")
    @Story("Fetch a resource")
    public void shouldReturnSingleResource() {
        // Demonstrates the reusable response specification: status, content type and SLA in one line.
        resources.getResource(2)
                .then()
                .spec(SpecFactory.jsonResponse(StatusCode.OK))
                .body("data.id", org.hamcrest.Matchers.equalTo(2))
                .body("data.name", org.hamcrest.Matchers.not(org.hamcrest.Matchers.emptyString()));
    }

    @Test(groups = {"regression", "resources", "negative"},
            description = "An unknown resource id is reported as 404")
    @Story("Fetch a resource")
    public void shouldReturnNotFoundForUnknownResource() {
        assertThat(resources.getResource(23))
                .hasStatusCode(StatusCode.NOT_FOUND)
                .hasEmptyBody();
    }
}
