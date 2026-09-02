package com.apiautomation.tests.spec;

import com.apiautomation.base.BaseTest;
import com.apiautomation.constants.Endpoints;
import com.apiautomation.core.filters.CorrelationIdFilter;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicReference;

import static com.apiautomation.assertions.ApiAssertions.assertThat;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;

/**
 * The specification style in its purest form: the shared {@code reqSpec} and {@code okSpec} carry
 * everything common, so each test is one path parameter or body plus the matchers that are unique
 * to the scenario.
 */
@Epic("Users API")
@Feature("Specification-driven checks")
public class SpecDrivenUserTests extends BaseTest {

    @Test(groups = {"smoke", "users"},
            description = "The shared request and response specifications cover a single-user read")
    @Story("Fetch a single user")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldReadUserThroughSharedSpecs() {
        given()
                .spec(reqSpec)
                .pathParam("id", 2)
        .when()
                .get(Endpoints.USER_BY_ID)
        .then()
                .spec(okSpec)
                .body("data.id", equalTo(2))
                .body("data.email", not(emptyString()));
    }

    @Test(groups = {"regression", "users"},
            description = "A collection read only adds its own matchers on top of the shared specs")
    @Story("List users")
    public void shouldReadCollectionThroughSharedSpecs() {
        given()
                .spec(reqSpec)
                .queryParam("page", 1)
        .when()
                .get(Endpoints.USERS)
        .then()
                .spec(okSpec)
                .body("page", equalTo(1))
                .body("data", hasSize(6));
    }

    @Test(groups = {"regression", "users"},
            description = "Every request carries a correlation id for server-side tracing")
    @Story("Traceability")
    @Severity(SeverityLevel.MINOR)
    public void shouldSendCorrelationIdOnEveryRequest() {
        AtomicReference<String> sentHeader = new AtomicReference<>();

        given()
                .spec(reqSpec)
                // Added after the spec filters, so it observes the header the framework injected.
                .filter((request, responseSpec, context) -> {
                    sentHeader.set(request.getHeaders().getValue(CorrelationIdFilter.HEADER));
                    return context.next(request, responseSpec);
                })
                .queryParam("page", 1)
        .when()
                .get(Endpoints.USERS)
        .then()
                .spec(okSpec);

        assertThat(sentHeader.get())
                .as("every request must be traceable back to this run")
                .isNotNull()
                .startsWith(CorrelationIdFilter.runId() + "-");
    }
}
