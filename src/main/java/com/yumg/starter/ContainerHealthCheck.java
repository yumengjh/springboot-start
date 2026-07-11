package com.yumg.starter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class ContainerHealthCheck {
    private ContainerHealthCheck() {}

    public static void main(String[] args) {
        if (!isReady(readinessUri(System.getenv("SERVER_PORT")))) {
            System.exit(1);
        }
    }

    static URI readinessUri(String configuredPort) {
        String port = configuredPort == null || configuredPort.isBlank() ? "8080" : configuredPort;
        return URI.create("http://127.0.0.1:" + port + "/actuator/health/readiness");
    }

    static boolean isReady(URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        try {
            return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
        } catch (IOException exception) {
            return false;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
