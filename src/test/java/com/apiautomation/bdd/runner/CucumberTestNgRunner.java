package com.apiautomation.bdd.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * TestNG entry point for the BDD suite.
 *
 * <p>Filter scenarios with a tag expression from the command line:
 * {@code mvn test -Dsuite=cucumber -Dtags="@smoke and @users"}.
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.apiautomation.bdd"},
        plugin = {
                "pretty",
                "summary",
                "html:target/cucumber-reports/cucumber.html",
                "json:target/cucumber-reports/cucumber.json",
                "junit:target/cucumber-reports/cucumber.xml",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true,
        publish = false)
public class CucumberTestNgRunner extends AbstractTestNGCucumberTests {

    /** Scenarios run in parallel; the thread count comes from the suite XML. */
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
