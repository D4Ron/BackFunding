package com.tg.crowdfunding.service;

import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class MockPaymentService {

    private final Random random = new Random();

    public PaymentResult process(String telephone, String montant) {
        try {
            // Simulate 2-3 second processing delay
            Thread.sleep(2000 + random.nextInt(1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 90% success rate
        boolean success = random.nextInt(10) != 0;

        String reference = "TG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new PaymentResult(success, reference);
    }

    public record PaymentResult(boolean success, String reference) {}
}