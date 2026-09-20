package com.assignment.promoengine.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CheckoutClientService {

    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<String> simulateCheckoutRedemption(int port, String code, String userId, String orderId) {
        String url = "http://localhost:" + port + "/promo-codes/" + code + "/redeem";

        // Set headers to application/json
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construct request payload body matching your DTO/Controller expectation
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("userId", userId);
        requestBody.put("orderId", orderId);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        // Send POST request
        return restTemplate.postForEntity(url, requestEntity, String.class);
    }
}