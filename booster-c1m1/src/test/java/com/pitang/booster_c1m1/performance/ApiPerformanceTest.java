package com.pitang.booster_c1m1.performance;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ApiPerformanceTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    void testUsersEndpointPerformance() throws InterruptedException {
        warmUpEndpoint("/v1/users");

        int numberOfRequests = 100;
        int concurrentThreads = 10;

        List<Long> responseTimes = performConcurrentRequests("/v1/users", numberOfRequests, concurrentThreads);

        responseTimes.sort(Long::compare);
        long p95 = calculatePercentile(responseTimes, 95);
        long p99 = calculatePercentile(responseTimes, 99);
        long average = responseTimes.stream().mapToLong(Long::longValue).sum() / responseTimes.size();

        System.out.println("=== Users Endpoint Performance Results ===");
        System.out.println("Total requests: " + numberOfRequests);
        System.out.println("Average response time: " + average + "ms");
        System.out.println("P95 response time: " + p95 + "ms");
        System.out.println("P99 response time: " + p99 + "ms");
        System.out.println("Max response time: " + responseTimes.get(responseTimes.size() - 1) + "ms");
        System.out.println("Min response time: " + responseTimes.get(0) + "ms");

        assertThat(p95).as("P95 response time should be under 400ms").isLessThanOrEqualTo(400);
    }

    @Test
    void testProductsEndpointPerformance() throws InterruptedException {
        warmUpEndpoint("/v1/products");

        int numberOfRequests = 100;
        int concurrentThreads = 10;

        List<Long> responseTimes = performConcurrentRequests("/v1/products", numberOfRequests, concurrentThreads);

        responseTimes.sort(Long::compare);
        long p95 = calculatePercentile(responseTimes, 95);
        long p99 = calculatePercentile(responseTimes, 99);
        long average = responseTimes.stream().mapToLong(Long::longValue).sum() / responseTimes.size();

        System.out.println("=== Products Endpoint Performance Results ===");
        System.out.println("Total requests: " + numberOfRequests);
        System.out.println("Average response time: " + average + "ms");
        System.out.println("P95 response time: " + p95 + "ms");
        System.out.println("P99 response time: " + p99 + "ms");
        System.out.println("Max response time: " + responseTimes.get(responseTimes.size() - 1) + "ms");
        System.out.println("Min response time: " + responseTimes.get(0) + "ms");

        assertThat(p95).as("P95 response time should be under 400ms").isLessThanOrEqualTo(400);
    }

    @Test
    void testSingleUserEndpointPerformance() throws InterruptedException {
        createTestUser();

        warmUpEndpoint("/v1/users/1");

        int numberOfRequests = 100;
        int concurrentThreads = 10;

        List<Long> responseTimes = performConcurrentRequests("/v1/users/1", numberOfRequests, concurrentThreads);

        responseTimes.sort(Long::compare);
        long p95 = calculatePercentile(responseTimes, 95);
        long p99 = calculatePercentile(responseTimes, 99);
        long average = responseTimes.stream().mapToLong(Long::longValue).sum() / responseTimes.size();

        System.out.println("=== Single User Endpoint Performance Results ===");
        System.out.println("Total requests: " + numberOfRequests);
        System.out.println("Average response time: " + average + "ms");
        System.out.println("P95 response time: " + p95 + "ms");
        System.out.println("P99 response time: " + p99 + "ms");
        System.out.println("Max response time: " + responseTimes.get(responseTimes.size() - 1) + "ms");
        System.out.println("Min response time: " + responseTimes.get(0) + "ms");

        assertThat(p95).as("P95 response time should be under 400ms").isLessThanOrEqualTo(400);
    }

    private void warmUpEndpoint(String endpoint) {
        System.out.println("Warming up endpoint: " + endpoint);
        for (int i = 0; i < 10; i++) {
            RestAssured.given()
                    .when()
                    .get(endpoint)
                    .then()
                    .statusCode(200);
        }
        System.out.println("Warm-up completed for: " + endpoint);
    }

    private List<Long> performConcurrentRequests(String endpoint, int numberOfRequests, int concurrentThreads) throws InterruptedException {
        List<Long> responseTimes = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(concurrentThreads);
        List<CompletableFuture<Long>> futures = new ArrayList<>();

        System.out.println("Starting performance test for endpoint: " + endpoint);
        long testStartTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfRequests; i++) {
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                Response response = RestAssured.given()
                        .when()
                        .get(endpoint);
                long endTime = System.currentTimeMillis();

                assertThat(response.getStatusCode()).isIn(200, 404);

                return endTime - startTime;
            }, executor);

            futures.add(future);
        }

        for (CompletableFuture<Long> future : futures) {
            try {
                responseTimes.add(future.get(10, TimeUnit.SECONDS));
            } catch (Exception e) {
                System.err.println("Request failed: " + e.getMessage());
            }
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        long testEndTime = System.currentTimeMillis();
        System.out.println("Performance test completed in " + (testEndTime - testStartTime) + "ms");
        System.out.println("Collected " + responseTimes.size() + " response times");

        return responseTimes;
    }

    private long calculatePercentile(List<Long> sortedValues, int percentile) {
        if (sortedValues.isEmpty()) {
            return 0;
        }

        int index = (int) Math.ceil((percentile / 100.0) * sortedValues.size()) - 1;
        index = Math.max(0, Math.min(index, sortedValues.size() - 1));

        return sortedValues.get(index);
    }

    private void createTestUser() {
        try {
            RestAssured.given()
                    .contentType("application/json")
                    .body("""
                        {
                            "name": "Performance Test User",
                            "email": "performance@test.com",
                            "password": "password123"
                        }
                        """)
                    .when()
                    .post("/v1/users")
                    .then()
                    .statusCode(201);
        } catch (Exception e) {
            System.out.println("Test user creation failed (might already exist): " + e.getMessage());
        }
    }
}