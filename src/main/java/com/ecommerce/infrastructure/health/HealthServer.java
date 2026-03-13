package com.ecommerce.infrastructure.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class HealthServer {
    private static final Logger logger = LoggerFactory.getLogger(HealthServer.class);
    private final HttpServer server;
    // Readiness flag can be toggled by the main application based on DB status, etc.
    private final AtomicBoolean isReady = new AtomicBoolean(true);

    public HealthServer(int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Context for Liveness Probe (Control Plane)
        this.server.createContext("/health/liveness", new LivenessHandler());
        
        // Context for Readiness Probe (Control Plane)
        this.server.createContext("/health/readiness", new ReadinessHandler());
        
        // Use a lightweight executor for health checks to not block the main app
        this.server.setExecutor(Executors.newFixedThreadPool(2));
    }

    public void start() {
        logger.info("[HealthServer] Starting on port " + server.getAddress().getPort());
        server.start();
    }

    public void stop() {
        logger.info("[HealthServer] Stopping...");
        server.stop(1); // 1 second delay
    }
    
    public void setReady(boolean ready) {
        this.isReady.set(ready);
    }

    private static class LivenessHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"status\":\"UP\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    private class ReadinessHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            boolean ready = isReady.get();
            int statusCode = ready ? 200 : 503;
            String statusText = ready ? "UP" : "OUT_OF_SERVICE";
            String response = "{\"status\":\"" + statusText + "\"}";
            
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}
