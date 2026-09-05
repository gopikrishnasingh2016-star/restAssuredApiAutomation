package com.apiautomation.core.filters;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.MDC;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Stamps every outgoing request with {@code X-Correlation-ID}.
 *
 * <p>The id is {@code <run>-<sequence>}: one run identifier shared by the whole suite plus a
 * per-request counter. When a test fails in a shared environment, that single value is enough to
 * pull the matching server-side logs and traces, which is usually the difference between "the API
 * returned 500" and knowing which downstream call caused it.
 */
public class CorrelationIdFilter implements Filter {

    public static final String HEADER = "X-Correlation-ID";

    private static final String RUN_ID = UUID.randomUUID().toString().substring(0, 8);
    private static final AtomicLong SEQUENCE = new AtomicLong();

    /** The identifier shared by every request of this run — worth printing in the report header. */
    public static String runId() {
        return RUN_ID;
    }

    @Override
    public Response filter(FilterableRequestSpecification request,
                           FilterableResponseSpecification responseSpec,
                           FilterContext context) {

        String correlationId = RUN_ID + "-" + SEQUENCE.incrementAndGet();
        request.replaceHeader(HEADER, correlationId);
        MDC.put("correlationId", correlationId);
        try {
            return context.next(request, responseSpec);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
