package com.apiautomation.tests.users;

import com.apiautomation.base.BaseTest;
import com.apiautomation.constants.StatusCode;
import com.apiautomation.models.request.CreateUserRequest;
import com.apiautomation.models.response.CreateUserResponse;
import com.apiautomation.support.CreateUserCase;
import com.apiautomation.support.TestData;
import com.apiautomation.utils.TestDataFactory;
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

@Epic("Users API")
@Feature("Create users")
public class CreateUserTests extends BaseTest {

    @Test(groups = {"smoke", "users"},
            description = "Creating a user returns 201 with a server-generated id and timestamp")
    @Story("Create a user")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Payload is generated at runtime so the assertion cannot pass on stale data.")
    public void shouldCreateUser() {
        CreateUserRequest payload = TestDataFactory.randomUser();

        Response response = users.createUser(payload);

        assertThat(response)
                .hasStatusCode(StatusCode.CREATED)
                .isJson()
                .matchesSchema("schemas/created-user.json")
                .hasNonBlankField("id")
                .hasNonBlankField("createdAt");

        CreateUserResponse created = response.as(CreateUserResponse.class);
        assertThat(created.name()).isEqualTo(payload.name());
        assertThat(created.job()).isEqualTo(payload.job());
    }

    @Test(groups = {"regression", "users"},
            dataProvider = "createUserCases",
            description = "The endpoint accepts the documented range of names and job titles")
    @Story("Create a user")
    public void shouldCreateUserForEachDataCase(CreateUserCase testCase) {
        Response response = users.createUser(CreateUserRequest.of(testCase.name(), testCase.job()));

        assertThat(response)
                .hasStatusCode(StatusCode.CREATED)
                .hasField("name", testCase.name())
                .hasField("job", testCase.job());
    }

    @DataProvider(name = "createUserCases")
    public Object[][] createUserCases() {
        return TestData.asDataProvider(TestData.createUserCases());
    }

    @Test(groups = {"regression", "users"},
            description = "Two creations of the same payload are independent resources")
    @Story("Create a user")
    @Severity(SeverityLevel.NORMAL)
    public void shouldTreatRepeatedCreationsAsSeparateRequests() {
        CreateUserRequest payload = TestDataFactory.randomUserWithJob("QA Automation Engineer");

        CreateUserResponse first = users.createUser(payload).as(CreateUserResponse.class);
        CreateUserResponse second = users.createUser(payload).as(CreateUserResponse.class);

        assertThat(first.createdAt()).isNotBlank();
        assertThat(second.createdAt()).isNotBlank();
        assertThat(second.name()).isEqualTo(first.name());
    }
}
