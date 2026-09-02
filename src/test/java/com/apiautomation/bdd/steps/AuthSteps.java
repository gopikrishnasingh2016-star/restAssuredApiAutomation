package com.apiautomation.bdd.steps;

import com.apiautomation.bdd.context.ScenarioContext;
import com.apiautomation.models.request.AuthRequest;
import com.apiautomation.models.response.AuthResponse;
import com.apiautomation.services.AuthService;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static com.apiautomation.assertions.ApiAssertions.assertThat;

/** Steps for login and registration, including the incomplete-payload cases. */
public class AuthSteps {

    private final ScenarioContext context;
    private final AuthService auth = new AuthService();

    public AuthSteps(ScenarioContext context) {
        this.context = context;
    }

    @When("I log in with the email {string} and the password {string}")
    public void iLogInWith(String email, String password) {
        context.recordResponse(auth.login(AuthRequest.of(email, password)));
    }

    @When("I log in with the email {string} and no password")
    public void iLogInWithoutPassword(String email) {
        context.recordResponse(auth.login(AuthRequest.withoutPassword(email)));
    }

    @When("I log in with the password {string} and no email")
    public void iLogInWithoutEmail(String password) {
        context.recordResponse(auth.login(AuthRequest.withoutEmail(password)));
    }

    @When("I register with the email {string} and the password {string}")
    public void iRegisterWith(String email, String password) {
        context.recordResponse(auth.register(AuthRequest.of(email, password)));
    }

    @When("I register with the email {string} and no password")
    public void iRegisterWithoutPassword(String email) {
        context.recordResponse(auth.register(AuthRequest.withoutPassword(email)));
    }

    @Then("a session token should be returned")
    public void aSessionTokenShouldBeReturned() {
        AuthResponse body = context.lastResponse().as(AuthResponse.class);
        assertThat(body.token()).as("the auth endpoint must return a usable token").isNotBlank();
        assertThat(body.error()).isNull();
    }

    @Then("the request should be rejected with the error {string}")
    public void theRequestShouldBeRejectedWith(String expectedError) {
        assertThat(context.lastResponse())
                .matchesSchema("schemas/error.json")
                .hasField("error", expectedError);
    }
}
