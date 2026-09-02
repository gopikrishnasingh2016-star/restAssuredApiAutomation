package com.apiautomation.bdd.steps;

import com.apiautomation.bdd.context.ScenarioContext;
import com.apiautomation.models.request.CreateUserRequest;
import com.apiautomation.models.response.UserListResponse;
import com.apiautomation.services.UserService;
import com.apiautomation.utils.TestDataFactory;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static com.apiautomation.assertions.ApiAssertions.assertThat;

/**
 * Steps for the {@code /users} resource. They delegate to {@link UserService} — the same service
 * object the TestNG tests use — so a change to the endpoint is fixed in exactly one place.
 */
public class UserSteps {

    private static final String CREATED_USER_NAME = "createdUserName";

    private final ScenarioContext context;
    private final UserService users = new UserService();

    public UserSteps(ScenarioContext context) {
        this.context = context;
    }

    @Given("the users API is reachable")
    public void theUsersApiIsReachable() {
        assertThat(users.getUsersPage(1)).hasStatusCode(200);
    }

    @When("I request the user with id {int}")
    public void iRequestTheUserWithId(int id) {
        context.recordResponse(users.getUser(id));
    }

    @When("I request page {int} of users")
    public void iRequestPageOfUsers(int page) {
        context.recordResponse(users.getUsersPage(page));
    }

    @When("I create a user named {string} with the job {string}")
    public void iCreateAUserNamedWithTheJob(String name, String job) {
        context.put(CREATED_USER_NAME, name);
        context.recordResponse(users.createUser(CreateUserRequest.of(name, job)));
    }

    @When("I create a user with generated details")
    public void iCreateAUserWithGeneratedDetails() {
        CreateUserRequest payload = TestDataFactory.randomUser();
        context.put(CREATED_USER_NAME, payload.name());
        context.recordResponse(users.createUser(payload));
    }

    @When("I replace user {int} with the job {string}")
    public void iReplaceUserWithTheJob(int id, String job) {
        context.recordResponse(users.replaceUser(id, TestDataFactory.randomUserWithJob(job)));
    }

    @When("I delete the user with id {int}")
    public void iDeleteTheUserWithId(int id) {
        context.recordResponse(users.deleteUser(id));
    }

    @Then("the returned user should have the email {string}")
    public void theReturnedUserShouldHaveTheEmail(String expectedEmail) {
        assertThat(context.lastResponse()).hasField("data.email", expectedEmail);
    }

    @Then("the page should contain {int} users")
    public void thePageShouldContainUsers(int expectedCount) {
        UserListResponse page = context.lastResponse().as(UserListResponse.class);
        assertThat(page.data()).hasSize(expectedCount);
    }

    @Then("the created user should echo the name I sent")
    public void theCreatedUserShouldEchoTheNameISent() {
        assertThat(context.lastResponse())
                .hasField("name", context.get(CREATED_USER_NAME, String.class))
                .hasNonBlankField("id")
                .hasNonBlankField("createdAt");
    }
}
