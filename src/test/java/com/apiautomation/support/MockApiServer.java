package com.apiautomation.support;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A local stand-in for the public API, driven by the stub definitions in
 * {@code src/test/resources/wiremock}.
 *
 * <p>It exists so the same tests can run in three situations: against the real service, in CI
 * where outbound traffic is blocked, and while debugging a failure — if a test passes here but
 * fails against the live API, the defect is in the service, not in the suite.
 */
public final class MockApiServer {

    private static final Logger LOG = LoggerFactory.getLogger(MockApiServer.class);
    private static WireMockServer server;

    private MockApiServer() {
    }

    /** Starts the stub server and points the framework's {@code base.uri} at it. */
    public static synchronized void start(int requestedPort) {
        if (server != null && server.isRunning()) {
            return;
        }
        server = new WireMockServer(WireMockConfiguration.options()
                .port(requestedPort > 0 ? requestedPort : 0)
                .usingFilesUnderClasspath("wiremock")
                .notifier(new Slf4jNotifier()));
        server.start();

        String baseUri = "http://localhost:" + server.port();
        System.setProperty("base.uri", baseUri);
        LOG.info("Stub API server started on {} with {} stub(s)", baseUri, server.getStubMappings().size());
    }

    public static synchronized void stop() {
        if (server != null && server.isRunning()) {
            server.stop();
            LOG.info("Stub API server stopped");
        }
        server = null;
    }

    public static boolean isRunning() {
        return server != null && server.isRunning();
    }

    /** Bridges WireMock's own notifications into the framework log. */
    private static final class Slf4jNotifier implements com.github.tomakehurst.wiremock.common.Notifier {

        private static final Logger WIREMOCK_LOG = LoggerFactory.getLogger("wiremock");

        @Override
        public void info(String message) {
            WIREMOCK_LOG.debug(message);
        }

        @Override
        public void error(String message) {
            WIREMOCK_LOG.error(message);
        }

        @Override
        public void error(String message, Throwable t) {
            WIREMOCK_LOG.error(message, t);
        }
    }
}
