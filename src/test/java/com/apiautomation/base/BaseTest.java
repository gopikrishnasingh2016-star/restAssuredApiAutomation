package com.apiautomation.base;

import com.apiautomation.config.ConfigReader;
import com.apiautomation.services.AuthService;
import com.apiautomation.services.ResourceService;
import com.apiautomation.services.UserService;
import com.apiautomation.support.TestEnvironment;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * Shared fixture for every TestNG class.
 *
 * <p>{@code @BeforeSuite} builds the specification layer once: a {@link RequestSpecification} with
 * the base URI, JSON content type, bearer header, correlation id and logging filters, and a
 * {@link ResponseSpecification} carrying the checks every successful response must satisfy. Tests
 * then contain only what is unique to their scenario.
 *
 * <p>Two styles are available and both are used in this project:
 * <ul>
 *   <li>{@code given().spec(reqSpec)…then().spec(okSpec)} — direct, for thin contract checks;</li>
 *   <li>the service objects ({@link #users}, {@link #auth}, {@link #resources}) — the Page Object
 *       Model applied to APIs, for anything with business meaning.</li>
 * </ul>
 * The services build their spec per call through {@code SpecFactory}, so a long-running suite
 * picks up a refreshed token automatically rather than reusing the one captured at suite start.
 */
public abstract class BaseTest {

    protected static final ConfigReader CONFIG = ConfigReader.get();

    /** Suite-scoped request specification — everything common to every call. */
    protected static RequestSpecification reqSpec;

    /** Suite-scoped response specification — status 200, JSON content type, response-time SLA. */
    protected static ResponseSpecification okSpec;

    protected final UserService users = new UserService();
    protected final AuthService auth = new AuthService();
    protected final ResourceService resources = new ResourceService();

    @BeforeSuite(alwaysRun = true)
    public void prepareEnvironment() {
        TestEnvironment.start();
        reqSpec = TestEnvironment.requestSpec();
        okSpec = TestEnvironment.okSpec();
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownEnvironment() {
        TestEnvironment.stop();
    }
}
