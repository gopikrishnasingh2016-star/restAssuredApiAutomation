package com.apiautomation.core.filters;

import com.apiautomation.utils.JsonUtils;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Logs every request and response in a compact, readable form and redacts credentials.
 *
 * <p>Written as a REST Assured {@link Filter} rather than {@code .log().all()} so that
 * logging is a property of the framework, not something each test has to remember.
 */
public class LoggingFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger("api");
    private static final Set<String> SENSITIVE_HEADERS =
            Set.of("authorization", "x-api-key", "cookie", "set-cookie", "proxy-authorization");
    private static final int MAX_BODY_CHARS = 4_000;

    /** Credentials and tokens are redacted in bodies as well as in headers. */
    private static final java.util.regex.Pattern SENSITIVE_FIELDS = java.util.regex.Pattern.compile(
            "(?i)(\"?(?:password|client_secret|access_token|refresh_token|token)\"?\\s*[:=]\\s*)(\"[^\"]*\"|[^,&\\s}]+)");

    @Override
    public Response filter(FilterableRequestSpecification request,
                           FilterableResponseSpecification responseSpec,
                           FilterContext context) {

        long startedAt = System.nanoTime();
        Response response = context.next(request, responseSpec);
        Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);

        LOG.info("""
                        
                        --> {} {}
                        Headers: {}
                        Body   : {}
                        <-- {} ({} ms)
                        Body   : {}""",
                request.getMethod(),
                request.getURI(),
                headers(request),
                truncate(mask(request.getBody() == null ? "<empty>" : JsonUtils.toPrettyJson(request.getBody().toString()))),
                response.getStatusLine(),
                elapsed.toMillis(),
                truncate(mask(body(response))));

        return response;
    }

    private static String headers(FilterableRequestSpecification request) {
        List<String> rendered = request.getHeaders().asList().stream()
                .map(header -> SENSITIVE_HEADERS.contains(header.getName().toLowerCase(Locale.ROOT))
                        ? header.getName() + "=***"
                        : header.getName() + "=" + header.getValue())
                .toList();
        return String.join(", ", rendered);
    }

    private static String body(Response response) {
        String raw = response.getBody() == null ? "" : response.getBody().asString();
        return raw.isBlank() ? "<empty>" : JsonUtils.toPrettyJson(raw);
    }

    private static String mask(String text) {
        return SENSITIVE_FIELDS.matcher(text).replaceAll("$1***");
    }

    private static String truncate(String text) {
        return text.length() <= MAX_BODY_CHARS
                ? text
                : text.substring(0, MAX_BODY_CHARS) + "... [truncated " + (text.length() - MAX_BODY_CHARS) + " chars]";
    }
}
