package com.apiautomation.support;

import com.apiautomation.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/** Loads the JSON fixtures under {@code src/test/resources/testdata} into typed cases. */
public final class TestData {

    private TestData() {
    }

    public static List<CreateUserCase> createUserCases() {
        return JsonUtils.readFromClasspath("testdata/create-user-cases.json", new TypeReference<>() {
        });
    }

    public static List<LoginCase> loginNegativeCases() {
        return JsonUtils.readFromClasspath("testdata/login-negative-cases.json", new TypeReference<>() {
        });
    }

    /** Turns a list of cases into the {@code Object[][]} shape TestNG data providers expect. */
    public static Object[][] asDataProvider(List<?> cases) {
        return cases.stream().map(testCase -> new Object[]{testCase}).toArray(Object[][]::new);
    }
}
