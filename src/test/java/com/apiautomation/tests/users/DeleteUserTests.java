package com.apiautomation.tests.users;

import com.apiautomation.base.BaseTest;
import com.apiautomation.constants.StatusCode;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static com.apiautomation.assertions.ApiAssertions.assertThat;

@Epic("Users API")
@Feature("Delete users")
public class DeleteUserTests extends BaseTest {

    @Test(groups = {"smoke", "users"},
            description = "Deleting a user answers 204 with no body")
    @Story("Delete a user")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldDeleteUser() {
        Response response = users.deleteUser(2);

        assertThat(response)
                .hasStatusCode(StatusCode.NO_CONTENT)
                .hasEmptyBody();
    }

    @Test(groups = {"regression", "users"},
            description = "Deleting the same user twice stays idempotent")
    @Story("Delete a user")
    @Severity(SeverityLevel.NORMAL)
    public void shouldBeIdempotent() {
        users.deleteUser(3);
        Response secondAttempt = users.deleteUser(3);

        assertThat(secondAttempt).hasStatusCode(StatusCode.NO_CONTENT);
    }
}
