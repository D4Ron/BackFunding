package com.tg.crowdfunding.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tg.crowdfunding.service.ContributionService;
import com.tg.crowdfunding.util.HmacUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackController {

    private final ContributionService contributionService;
    private final ObjectMapper objectMapper;

    @Value("${app.fedapay.webhook-secret}")
    private String webhookSecret;

    @PostMapping("/callback")
    public ResponseEntity<Void> handleCallback(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Fedapay-Signature", required = false) String signature) {

        if (signature == null || !HmacUtils.verifyHmac(rawBody, webhookSecret, signature)) {
            log.warn("FedaPay: invalid or missing webhook signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("FedaPay callback received");

        try {
            Map<String, Object> payload = objectMapper.readValue(
                rawBody, new TypeReference<Map<String, Object>>() {});

            Object eventObj = payload.get("name");
            Object dataObj  = payload.get("data");

            if (eventObj != null && dataObj instanceof Map<?, ?> data) {
                String event          = eventObj.toString();
                Object transactionObj = data.get("object");

                if (transactionObj instanceof Map<?, ?> transaction) {
                    String status    = transaction.get("status") != null
                            ? transaction.get("status").toString() : "";
                    Object idObj     = transaction.get("id");
                    String reference = idObj != null ? "FEDA-" + idObj : null;

                    if (reference == null) {
                        log.warn("FedaPay callback: no transaction id found in payload");
                        return ResponseEntity.ok().build();
                    }

                    boolean approved = event.equals("transaction.approved")
                            || status.equals("approved");
                    boolean canceled = event.equals("transaction.canceled")
                            || status.equals("canceled");

                    if (approved) {
                        contributionService.updateContributionStatus(reference, true);
                        log.info("Contribution approved for reference: {}", reference);
                    } else if (canceled) {
                        contributionService.updateContributionStatus(reference, false);
                        log.info("Contribution canceled for reference: {}", reference);
                    } else {
                        log.info("Unhandled FedaPay event '{}' with status '{}'", event, status);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Callback processing error: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok().build();
    }
}