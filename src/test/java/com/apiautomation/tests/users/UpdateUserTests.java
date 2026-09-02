package com.apiautomation.tests.users;

import com.apiautomation.base.BaseTest;
import com.apiautomation.constants.StatusCode;
import com.apiautomation.models.request.CreateUserRequest;
import com.apiautomation.models.response.UpdateUserResponse;
import com.apiautomation.utils.TestDataFactory;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static com.apiautomation.assertions.ApiAssertions.assertThat;

@Epic("Users API")
@Feature("Update users")
public class UpdateUserTests extends BaseTest {

    private static final int EXISTING_USER_ID = 2;

    @Test(groups = {"smoke", "users"},
            description = "A full replacement (PUT) echoes the new representation with a timestamp")
    @Story("Replace a user")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldReplaceUser() {
        CreateUserRequest payload = TestDataFactory.randomUserWithJob("Team Lead");

        Response response = users.replaceUser(EXISTING_USER_ID, payload);

        assertThat(response)
                .hasStatusCode(StatusCode.OK)
                .isJson()
                .matchesSchema("schemas/updated-user.json");

        UpdateUserResponse updated = response.as(UpdateUserResponse.class);
        assertThat(updated.name()).isEqualTo(payload.name());
        assertThat(updated.job()).isEqualTo(payload.job());
        assertThat(updated.updatedAt()).isNotBlank();
    }

    @Test(groups = {"regression", "users"},
            description = "A partial update (PATCH) applies the changed field")
    @Story("Patch a user")
    public void shouldPatchUser() {
        CreateUserRequest payload = TestDataFactory.randomUserWithJob(TestDataFactory.randomJobTitle());

        Response response = users.updateUser(EXISTING_USER_ID, payload);

        assertThat(response)
                .hasStatusCode(StatusCode.OK)
                .hasField("job", payload.job())
                .hasNonBlankField("updatedAt");
    }
}
