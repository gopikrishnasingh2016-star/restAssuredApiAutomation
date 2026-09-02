package com.apiautomation.listeners;

import com.apiautomation.config.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Re-runs a failed test up to {@code retry.count} times.
 *
 * <p>Off by default: a retry that hides a real defect is worse than a red build. Enable it
 * per environment (for example {@code -Dretry.count=1} against a rate-limited public API).
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger LOG = LoggerFactory.getLogger(RetryAnalyzer.class);
    private static final int MAX_RETRIES = ConfigReader.get().retryCount();

    private int attempts;

    @Override
    public boolean retry(ITestResult result) {
        if (attempts >= MAX_RETRIES) {
            return false;
        }
        attempts++;
        LOG.warn("Retrying {} — attempt {} of {}", result.getName(), attempts, MAX_RETRIES);
        return true;
    }
}
