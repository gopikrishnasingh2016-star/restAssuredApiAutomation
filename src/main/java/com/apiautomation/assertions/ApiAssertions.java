package com.apiautomation.assertions;

import io.restassured.response.Response;
import org.assertj.core.api.Assertions;

/**
 * The single entry point for assertions in this project.
 *
 * <p>It extends AssertJ's {@link Assertions}, so one static import
 * ({@code import static com.apiautomation.assertions.ApiAssertions.assertThat;}) gives tests both
 * the API-aware {@link ResponseAssert} and the whole AssertJ vocabulary for plain objects.
 */
public class ApiAssertions extends Assertions {

    protected ApiAssertions() {
    }

    public static ResponseAssert assertThat(Response response) {
        return new ResponseAssert(response);
    }
}
