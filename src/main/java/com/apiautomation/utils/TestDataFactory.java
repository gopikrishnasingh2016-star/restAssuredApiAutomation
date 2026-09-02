package com.apiautomation.utils;

import com.apiautomation.models.request.AuthRequest;
import com.apiautomation.models.request.CreateUserRequest;
import net.datafaker.Faker;

import java.util.Locale;

/**
 * Generates realistic, unique payloads so tests never collide on shared data
 * and never assert against values they themselves hard-coded upstream.
 */
public final class TestDataFactory {

    private static final Faker FAKER = new Faker(Locale.ENGLISH);

    private TestDataFactory() {
    }

    public static CreateUserRequest randomUser() {
        return CreateUserRequest.of(FAKER.name().fullName(), FAKER.job().title());
    }

    public static CreateUserRequest randomUserWithJob(String job) {
        return CreateUserRequest.of(FAKER.name().fullName(), job);
    }

    public static AuthRequest randomCredentials() {
        return AuthRequest.of(FAKER.internet().emailAddress(), FAKER.internet().password(8, 16));
    }

    public static String randomJobTitle() {
        return FAKER.job().title();
    }
}
