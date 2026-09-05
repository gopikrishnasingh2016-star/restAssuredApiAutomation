package com.apiautomation.tests.auth;

import com.apiautomation.base.BaseTest;
import com.apiautomation.constants.StatusCode;
import com.apiautomation.models.request.AuthRequest;
import com.apiautomation.models.response.AuthResponse;
import com.apiautomation.support.LoginCase;
import com.apiautomation.support.TestData;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.apiautomation.assertions.ApiAssertions.assertThat;

@Epic("Authentication")
@Feature("Login")
public class LoginTests extends BaseTest {

    private static final AuthRequest VALID_CREDENTIALS =
            AuthRequest.of("eve.holt@reqres.in", "cityslicka");

    @Test(groups = {"smoke", "auth"},
            description = "Valid credentials return a session token")
    @Story("Successful login")
    @Severity(SeverityLevel.BLOCKER)
    public void shouldReturnTokenForValidCredentials() {
        Response response = auth.login(VALID_CREDENTIALS);

        assertThat(response)
                .hasStatusCode(StatusCode.OK)
                .isJson()
                .matchesSchema("schemas/auth-success.json");

        AuthResponse body = response.as(AuthResponse.class);
        assertThat(body.token()).isNotBlank();
        assertThat(body.error()).isNull();
    }

    @Test(groups = {"regression", "auth"},
            description = "The service helper returns a usable token")
    @Story("Successful login")
    public void shouldExposeTokenThroughServiceHelper() {
        String token = auth.tokenFor(VALID_CREDENTIALS);

        assertThat(token).isNotBlank();
    }

    @Test(groups = {"regression", "auth", "negative"},
            dataProvider = "invalidLogins",
            description = "Incomplete credentials are rejected with a descriptive error")
    @Story("Rejected login")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Data-driven from testdata/login-negative-cases.json — adding a case needs no new code.")
    public void shouldRejectIncompleteCredentials(LoginCase testCase) {
        AuthRequest payload = new AuthRequest(testCase.email(), testCase.password());

        Response response = auth.login(payload);

        assertThat(response)
                .hasStatusCode(testCase.expectedStatus())
                .isJson()
                .matchesSchema("schemas/error.json")
                .hasField("error", testCase.expectedError());
    }

    @DataProvider(name = "invalidLogins")
    public Object[][] invalidLogins() {
        return TestData.asDataProvider(TestData.loginNegativeCases());
    }
}
