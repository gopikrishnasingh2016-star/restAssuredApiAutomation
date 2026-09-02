package com.apiautomation.tests.auth;

import com.apiautomation.auth.Token;
import com.apiautomation.auth.TokenManager;
import com.apiautomation.base.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;

import static com.apiautomation.assertions.ApiAssertions.assertThat;

@Epic("Authentication")
@Feature("Token management")
public class TokenManagerTests extends BaseTest {

    @Test(groups = {"smoke", "auth"},
            description = "The token endpoint issues a usable token with a future expiry")
    @Story("Acquire a token")
    @Severity(SeverityLevel.BLOCKER)
    public void shouldIssueUsableToken() {
        Token token = TokenManager.get().token();

        assertThat(token.value()).isNotBlank();
        assertThat(token.expiresAt()).isAfter(Instant.now());
        assertThat(token.asBearerHeader()).startsWith("Bearer ");
    }

    @Test(groups = {"regression", "auth"},
            description = "A cached token is reused instead of calling the auth service again")
    @Story("Cache a token")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Guards the caching contract: hitting the auth service once per test would make "
            + "the suite slower than the system it tests and would rate-limit the auth service.")
    public void shouldReuseCachedToken() {
        Token first = TokenManager.get().token();
        Token second = TokenManager.get().token();

        assertThat(second).isSameAs(first);
    }

    @Test(groups = {"regression", "auth"},
            description = "An invalidated token is replaced on the next call")
    @Story("Refresh a token")
    public void shouldReAuthenticateAfterInvalidation() {
        Token first = TokenManager.get().token();

        TokenManager.get().invalidate();
        Token second = TokenManager.get().token();

        assertThat(second).isNotSameAs(first);
        assertThat(second.value()).isNotBlank();
    }

    @Test(groups = {"regression", "auth"},
            description = "A token inside the refresh skew is treated as expiring")
    @Story("Refresh a token")
    public void shouldTreatTokenInsideSkewAsExpiring() {
        Token almostExpired = Token.lasting("s3cr3t-token", Duration.ofSeconds(5));
        Token fresh = Token.lasting("s3cr3t-token", Duration.ofHours(1));

        assertThat(almostExpired.isExpiringWithin(Duration.ofSeconds(60))).isTrue();
        assertThat(fresh.isExpiringWithin(Duration.ofSeconds(60))).isFalse();
        assertThat(almostExpired.toString())
                .as("a token must never leak its value into a log or report")
                .doesNotContain("s3cr3t-token");
    }
}
