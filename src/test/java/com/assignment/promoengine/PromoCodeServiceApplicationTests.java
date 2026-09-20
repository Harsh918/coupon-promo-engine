package com.assignment.promoengine;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.assignment.promoengine.model.PromoCode;
import com.assignment.promoengine.repository.PromoCodeRepository;
import com.assignment.promoengine.service.CheckoutClientService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PromoCodeServiceApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private PromoCodeRepository promoCodeRepository;

    @Autowired
    private CheckoutClientService checkoutClientService;

    @BeforeEach
    void setUp() {
        promoCodeRepository.clear();
    }

    @Test
    void testConcurrentRedemptionsExceedingLimit() throws InterruptedException {
        String testCode = "SUMMERS";
        int maxAllowed = 5;
        
        PromoCode promo = new PromoCode(testCode, maxAllowed, 0, true);
        promoCodeRepository.save(promo);

        // We will fire 50 concurrent threads trying to redeem the same code
        int totalThreads = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(totalThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        for (int i = 0; i < totalThreads; i++) {
            final int userIdSuffix = i;
            executorService.submit(() -> {
                try {
                    String userId = "user_" + userIdSuffix;
                    String orderId = "order_" + userIdSuffix;
                    
                    var response = checkoutClientService.simulateCheckoutRedemption(port, testCode, userId, orderId);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                        System.err.println("FAILED -> Status: " + response.getStatusCode() + " | Body: " + response.getBody());
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.err.println("EXCEPTION IN THREAD -> " + e.getClass().getName() + ": " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // 2. Assertions to prove concurrency correctness
        // Successes must never exceed the hard cap of 5
        assertEquals(maxAllowed, successCount.get(), "Successful redemptions must equal maxRedemptions exactly!");
        assertEquals(totalThreads - maxAllowed, failureCount.get(), "Remaining requests should have failed due to cap exhaustion.");

        // 3. Verify final state in repository matches max redemptions
        PromoCode finalState = promoCodeRepository.findByCode(testCode);
        assertEquals(maxAllowed, finalState.getCurrentRedemptions());
    }
}