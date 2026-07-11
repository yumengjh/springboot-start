package com.yumg.starter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ContainerHealthCheckTest {
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void acceptsSuccessfulReadinessResponse() throws IOException {
        URI readiness = startServer(200);

        assertTrue(ContainerHealthCheck.isReady(readiness));
    }

    @Test
    void rejectsUnhealthyReadinessResponse() throws IOException {
        URI readiness = startServer(503);

        assertFalse(ContainerHealthCheck.isReady(readiness));
    }

    @Test
    void usesDefaultPortWhenServerPortIsNotConfigured() {
        assertEquals(
                URI.create("http://127.0.0.1:8080/actuator/health/readiness"),
                ContainerHealthCheck.readinessUri(null));
    }

    @Test
    void usesConfiguredServerPort() {
        assertEquals(
                URI.create("http://127.0.0.1:9090/actuator/health/readiness"),
                ContainerHealthCheck.readinessUri("9090"));
    }

    private URI startServer(int status) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/actuator/health/readiness", exchange -> {
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        });
        server.start();
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort()
                + "/actuator/health/readiness");
    }
}
