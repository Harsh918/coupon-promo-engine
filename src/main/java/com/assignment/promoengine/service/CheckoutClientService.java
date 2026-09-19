package com.assignment.promoengine.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.assignment.promoengine.model.Redemption;

@Service
public class CheckoutClientService {

    private final RestClient restClient;

    public CheckoutClientService() {
        this.restClient = RestClient.create();
    }

    public ResponseEntityWrapper<Redemption> simulateCheckoutRedemption(int port, String code, String userId, String orderId) {
        String url = "http://localhost:" + port + "/promo-codes/" + code + "/redeem";
        Map<String, String> payload = Map.of("userId", userId, "orderId", orderId);

        try {
            ResponseEntity<Redemption> response = restClient.post()
                    .uri(url)
                    .body(payload)
                    .retrieve()
                    .toEntity(Redemption.class);
            return new ResponseEntityWrapper<>(response.getStatusCode().value(), response.getBody());
        } catch (HttpClientErrorException e) {
            return new ResponseEntityWrapper<>(e.getStatusCode().value(), null);
        }
    }

    public static class ResponseEntityWrapper<T> {
        private final int statusCode;
        private final T body;

        public ResponseEntityWrapper(int statusCode, T body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int getStatusCode() { return statusCode; }
        public T getBody() { return body; }
    }
}