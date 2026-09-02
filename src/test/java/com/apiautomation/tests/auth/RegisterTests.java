package com.apiautomation.tests.auth;

import com.apiautomation.base.BaseTest;
import com.apiautomation.constants.StatusCode;
import com.apiautomation.models.request.AuthRequest;
import com.apiautomation.models.response.AuthResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static com.apiautomation.assertions.ApiAssertions.assertThat;

@Epic("Authentication")
@Feature("Registration")
public class RegisterTests extends BaseTest {

    @Test(groups = {"smoke", "auth"},
            description = "A registered user receives an id and a token")
    @Story("Successful registration")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldRegisterKnownUser() {
        Response response = auth.register(AuthRequest.of("eve.holt@reqres.in", "pistol"));

        assertThat(response)
                .hasStatusCode(StatusCode.OK)
                .isJson()
                .matchesSchema("schemas/auth-success.json");

        AuthResponse body = response.as(AuthResponse.class);
        assertThat(body.id()).isNotNull();
        assertThat(body.token()).isNotBlank();
    }

    @Test(groups = {"regression", "auth", "negative"},
            description = "Registration without a password is rejected")
    @Story("Rejected registration")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldRejectRegistrationWithoutPassword() {
        Response response = auth.register(AuthRequest.withoutPassword("sydney@fife"));

        assertThat(response)
                .hasStatusCode(StatusCode.BAD_REQUEST)
                .hasField("error", "Missing password");
    }
}
