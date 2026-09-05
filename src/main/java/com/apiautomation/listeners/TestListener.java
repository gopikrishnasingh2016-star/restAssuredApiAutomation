package com.apiautomation.listeners;

import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.time.Duration;

/** Console/reporting listener: run banners, timings and failure context in the Allure report. */
public class TestListener implements ITestListener {

    private static final Logger LOG = LoggerFactory.getLogger(TestListener.class);

    @Override
    public void onStart(ITestContext context) {
        LOG.info("===== Starting suite: {} =====", context.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        LOG.info("START  {}.{}", result.getTestClass().getRealClass().getSimpleName(), result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOG.info("PASS   {} ({} ms)", result.getName(), duration(result).toMillis());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LOG.error("FAIL   {} after {} ms", result.getName(), duration(result).toMillis(), result.getThrowable());
        if (result.getThrowable() != null) {
            Allure.addAttachment("Failure detail", "text/plain", String.valueOf(result.getThrowable()));
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOG.warn("SKIP   {}", result.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        LOG.info("===== Finished suite: {} | passed={} failed={} skipped={} =====",
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
    }

    private static Duration duration(ITestResult result) {
        return Duration.ofMillis(result.getEndMillis() - result.getStartMillis());
    }
}
