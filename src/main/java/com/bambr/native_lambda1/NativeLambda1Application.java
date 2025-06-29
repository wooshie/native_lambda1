package com.bambr.native_lambda1;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SpringBootApplication
public class NativeLambda1Application implements CommandLineRunner {
    private static final String REQUEST_ID_HEADER = "lambda-runtime-aws-request-id";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public static void main(String[] args) {
        SpringApplication.run(NativeLambda1Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String endpoint = System.getenv("AWS_LAMBDA_RUNTIME_API");
        
        if (endpoint == null) {
            throw new RuntimeException("AWS_LAMBDA_RUNTIME_API environment variable is not set");
        }

        System.out.println("Lambda runtime starting...");
        
        // Lambda execution loop - this keeps the process running
        while (true) {
            try {
                // Get next invocation
                var invocationResponse = getInvocation(endpoint);
                System.out.println("Processing request ID: " + invocationResponse.requestId());
                
                // Process the request (your business logic here)
                String response = processRequest(invocationResponse.body(), args);
                
                // Send successful response
                sendResponse(endpoint, invocationResponse.requestId(), response);
                
            } catch (Exception e) {
                System.err.println("Error in Lambda execution loop: " + e.getMessage());
                e.printStackTrace();
                // Continue the loop even if one invocation fails
            }
        }
    }

    private String processRequest(String requestBody, String... args) {
        try {
            // Your business logic here
            String argsString = String.join(", ", args);
            return String.format("Hello from Native Lambda! Args: [%s], Request: %s", argsString, requestBody);
        } catch (Exception e) {
            throw new RuntimeException("Error processing request", e);
        }
    }

    private InvocationResponse getInvocation(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(String.format("http://%s/2018-06-01/runtime/invocation/next", endpoint)))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        String requestId = response.headers().firstValue(REQUEST_ID_HEADER)
                .orElseThrow(() -> new RuntimeException("No request ID in response"));
        return new InvocationResponse(requestId, response.body());
    }

    private void sendResponse(String endpoint, String requestId, String responseBody) 
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(responseBody))
                .uri(URI.create(String.format("http://%s/2018-06-01/runtime/invocation/%s/response", 
                        endpoint, requestId)))
                .build();

        HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Response sent for request ID: " + requestId);
    }

    record InvocationResponse(String requestId, String body) {}
}