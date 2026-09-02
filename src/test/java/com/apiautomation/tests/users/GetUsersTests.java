package com.apiautomation.tests.users;

import com.apiautomation.base.BaseTest;
import com.apiautomation.constants.StatusCode;
import com.apiautomation.models.response.SingleUserResponse;
import com.apiautomation.models.response.UserData;
import com.apiautomation.models.response.UserListResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;

import static com.apiautomation.assertions.ApiAssertions.assertThat;

@Epic("Users API")
@Feature("Read users")
public class GetUsersTests extends BaseTest {

    @Test(groups = {"smoke", "users"},
            description = "A user that exists is returned with the documented payload")
    @Story("Fetch a single user")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verifies status, content type, schema and the individual fields of GET /users/{id}.")
    public void shouldReturnAnExistingUser() {
        Response response = users.getUser(2);

        assertThat(response)
                .hasStatusCode(StatusCode.OK)
                .isJson()
                .matchesSchema("schemas/single-user.json")
                .hasField("data.id", 2);

        UserData user = response.as(SingleUserResponse.class).data();
        assertThat(user.email()).isEqualTo("janet.weaver@reqres.in");
        assertThat(user.firstName()).isEqualTo("Janet");
        assertThat(user.lastName()).isEqualTo("Weaver");
        assertThat(user.avatar()).startsWith("https://");
        assertThat(user.fullName()).isEqualTo("Janet Weaver");
    }

    @Test(groups = {"smoke", "users"},
            description = "A paginated page of users honours the requested page number")
    @Story("List users")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldReturnRequestedPageOfUsers() {
        Response response = users.getUsersPage(2);

        assertThat(response)
                .hasStatusCode(StatusCode.OK)
                .isJson()
                .matchesSchema("schemas/user-list.json");

        UserListResponse page = response.as(UserListResponse.class);
        assertThat(page.page()).isEqualTo(2);
        assertThat(page.data()).hasSize(page.perPage());
        assertThat(page.data()).extracting(UserData::email).allMatch(email -> email.contains("@"));
        assertThat(page.data()).extracting(UserData::id).doesNotHaveDuplicates();
        assertThat(page.totalPages()).isGreaterThanOrEqualTo(page.page());
    }

    @Test(groups = {"regression", "users"},
            dataProvider = "pages",
            description = "Every advertised page can be fetched and stays within the collection size")
    @Story("List users")
    public void shouldPaginateConsistently(int pageNumber) {
        UserListResponse page = users.getUsersPageAs(pageNumber);

        assertThat(page.page()).isEqualTo(pageNumber);
        assertThat(page.data().size()).isLessThanOrEqualTo(page.perPage());
        assertThat(page.total()).isGreaterThanOrEqualTo(page.data().size());
    }

    @DataProvider(name = "pages")
    public Object[][] pages() {
        return new Object[][]{{1}, {2}};
    }

    @Test(groups = {"regression", "users", "negative"},
            description = "An unknown user id is reported as 404 and not as an empty 200")
    @Story("Fetch a single user")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldReturnNotFoundForUnknownUser() {
        Response response = users.getUser(23);

        assertThat(response)
                .hasStatusCode(StatusCode.NOT_FOUND)
                .hasEmptyBody();
    }

    @Test(groups = {"regression", "users"},
            description = "A page beyond the last one returns an empty collection, not an error")
    @Story("List users")
    public void shouldReturnEmptyDataBeyondLastPage() {
        UserListResponse page = users.getUsersPageAs(99);

        assertThat(page.data()).isEmpty();
    }

    @Test(groups = {"regression", "users", "performance"},
            description = "A server-side delay is honoured and still completes inside the SLA")
    @Story("Non-functional expectations")
    @Severity(SeverityLevel.MINOR)
    public void shouldHonourServerSideDelayWithinSla() {
        Response response = users.getUsersWithDelay(1);

        assertThat(response)
                .hasStatusCode(StatusCode.OK)
                .respondsWithin(CONFIG.slaResponseTime());

        assertThat(response.timeIn(java.util.concurrent.TimeUnit.MILLISECONDS))
                .as("the server must actually apply the requested delay")
                .isGreaterThanOrEqualTo(Duration.ofSeconds(1).toMillis());
    }
}
