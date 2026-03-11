package com.tg.crowdfunding.controller;

import com.tg.crowdfunding.service.ContributionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackController {

    private final ContributionService contributionService;

    @PostMapping("/callback")
    public ResponseEntity<Void> handleCallback(@RequestBody Map<String, Object> payload) {
        log.info("FedaPay callback received: {}", payload);

        try {
            Object eventObj = payload.get("name");
            Object dataObj = payload.get("data");

            if (eventObj != null && dataObj instanceof Map<?, ?> data) {
                String event = eventObj.toString();
                Object transactionObj = data.get("object");

                if (transactionObj instanceof Map<?, ?> transaction) {
                    String status = transaction.get("status") != null
                            ? transaction.get("status").toString() : "";
                    String reference = "FEDA-" + transaction.get("id");

                    if (event.equals("transaction.approved") || status.equals("approved")) {
                        contributionService.updateContributionStatus(reference, true);
                    } else if (event.equals("transaction.canceled") || status.equals("canceled")) {
                        contributionService.updateContributionStatus(reference, false);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Callback processing error: {}", e.getMessage());
        }

        return ResponseEntity.ok().build();
    }
}