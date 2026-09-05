package com.apiautomation.support;

import com.apiautomation.config.ConfigReader;
import com.apiautomation.config.Environment;
import com.apiautomation.core.SpecFactory;
import com.apiautomation.core.filters.CorrelationIdFilter;
import com.apiautomation.constants.StatusCode;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Brings the run up once, whichever runner started it.
 *
 * <p>Both the TestNG {@code BaseTest} and the Cucumber hooks call {@link #start()}; it is
 * idempotent, so a mixed run (API suite plus BDD suite in one JVM) starts a single stub server
 * and builds a single set of specifications.
 */
public final class TestEnvironment {

    private static final Logger LOG = LoggerFactory.getLogger(TestEnvironment.class);
    private static final ConfigReader CONFIG = ConfigReader.get();

    private static RequestSpecification requestSpec;
    private static ResponseSpecification okSpec;
    private static boolean started;

    private TestEnvironment() {
    }

    public static synchronized void start() {
        if (started) {
            return;
        }
        if (CONFIG.environment() == Environment.MOCK) {
            MockApiServer.start(CONFIG.mockPort());
        }

        // The spec layer, assembled once for the suite: base URI, JSON content type, API key,
        // bearer token from the TokenManager, correlation id and the logging/reporting filters.
        requestSpec = SpecFactory.request();
        okSpec = SpecFactory.jsonResponse(StatusCode.OK);
        started = true;

        LOG.info("Environment={} | baseUri={}{} | auth={} | runId={}",
                CONFIG.environment(), CONFIG.baseUri(), CONFIG.basePath(),
                CONFIG.authEnabled() ? "enabled" : "disabled", CorrelationIdFilter.runId());
    }

    public static synchronized void stop() {
        if (MockApiServer.isRunning()) {
            MockApiServer.stop();
        }
        requestSpec = null;
        okSpec = null;
        started = false;
    }

    /** The suite-scoped request specification. */
    public static RequestSpecification requestSpec() {
        start();
        return requestSpec;
    }

    /** The response specification every 200-with-JSON endpoint must satisfy. */
    public static ResponseSpecification okSpec() {
        start();
        return okSpec;
    }
}
