package com.apiautomation.bdd.steps;

import com.apiautomation.bdd.context.ScenarioContext;
import io.cucumber.java.en.Then;

import java.time.Duration;

import static com.apiautomation.assertions.ApiAssertions.assertThat;

/** Steps that apply to any endpoint: status, schema, timing and individual fields. */
public class CommonSteps {

    private final ScenarioContext context;

    public CommonSteps(ScenarioContext context) {
        this.context = context;
    }

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int expectedStatusCode) {
        assertThat(context.lastResponse()).hasStatusCode(expectedStatusCode);
    }

    @Then("the response should be JSON")
    public void theResponseShouldBeJson() {
        assertThat(context.lastResponse()).isJson();
    }

    @Then("the response should match the {string} schema")
    public void theResponseShouldMatchTheSchema(String schemaName) {
        assertThat(context.lastResponse()).matchesSchema("schemas/" + schemaName + ".json");
    }

    @Then("the response field {string} should be {string}")
    public void theResponseFieldShouldBe(String jsonPath, String expectedValue) {
        assertThat(context.lastResponse()).hasField(jsonPath, expectedValue);
    }

    @Then("the response field {string} should not be empty")
    public void theResponseFieldShouldNotBeEmpty(String jsonPath) {
        assertThat(context.lastResponse()).hasNonBlankField(jsonPath);
    }

    @Then("the response body should be empty")
    public void theResponseBodyShouldBeEmpty() {
        assertThat(context.lastResponse()).hasEmptyBody();
    }

    @Then("the response should arrive within {int} milliseconds")
    public void theResponseShouldArriveWithin(int milliseconds) {
        assertThat(context.lastResponse()).respondsWithin(Duration.ofMillis(milliseconds));
    }
}
