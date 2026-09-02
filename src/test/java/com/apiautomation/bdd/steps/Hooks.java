package com.apiautomation.bdd.steps;

import com.apiautomation.bdd.context.ScenarioContext;
import com.apiautomation.support.TestEnvironment;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lifecycle for the BDD suite. {@code @BeforeAll} brings up the same environment the TestNG suite
 * uses — one stub server, one specification layer, one cached token — so both entry points behave
 * identically.
 */
public class Hooks {

    private static final Logger LOG = LoggerFactory.getLogger(Hooks.class);

    private final ScenarioContext context;

    public Hooks(ScenarioContext context) {
        this.context = context;
    }

    @BeforeAll
    public static void startEnvironment() {
        TestEnvironment.start();
    }

    @AfterAll
    public static void stopEnvironment() {
        TestEnvironment.stop();
    }

    @Before
    public void logScenarioStart(Scenario scenario) {
        LOG.info("START  scenario: {}", scenario.getName());
    }

    @After
    public void attachEvidence(Scenario scenario) {
        if (scenario.isFailed()) {
            try {
                Allure.addAttachment("Last response", "application/json",
                        context.lastResponse().asPrettyString(), ".json");
                scenario.log("Last response: " + context.lastResponse().asString());
            } catch (IllegalStateException noResponseYet) {
                scenario.log("Scenario failed before any response was recorded");
            }
        }
        LOG.info("{}   scenario: {}", scenario.isFailed() ? "FAIL" : "PASS", scenario.getName());
    }
}
