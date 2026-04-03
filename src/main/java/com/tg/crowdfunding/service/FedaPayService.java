package com.tg.crowdfunding.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FedaPayService {

    @Value("${app.fedapay.secret-key}")
    private String secretKey;

    @Value("${app.fedapay.base-url}")
    private String baseUrl;

    @Value("${app.fedapay.callback-url}")
    private String callbackUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FedaPayResult processPayment(String telephone,
                                        BigDecimal montant,
                                        String methodePaiement,
                                        String customerNom,
                                        String customerEmail) {
        try {
            // In sandbox, remap phone and method to FedaPay test values
            boolean sandbox = baseUrl.contains("sandbox");
            String effectivePhone = sandbox ? "64000001" : telephone; // 64000001 = success scenario
            String effectiveMethod = sandbox ? "momo_test" : methodePaiement;

            // Step 1 — Create transaction
            Map<String, Object> transactionData = createTransaction(
                    montant, customerNom, customerEmail, effectivePhone);

            if (transactionData == null) {
                log.error("FedaPay: transaction creation returned null");
                return new FedaPayResult(false,
                        "FAILED-" + System.currentTimeMillis(), null);
            }

            Long transactionId = Long.valueOf(transactionData.get("id").toString());
            log.info("FedaPay: transaction created with id={}", transactionId);

            // Step 2 — Get payment token
            String token = getPaymentToken(transactionId);
            if (token == null) {
                log.error("FedaPay: could not get token for transaction {}", transactionId);
                return new FedaPayResult(false,
                        "FAILED-" + System.currentTimeMillis(), null);
            }
            log.info("FedaPay: got token for transaction {}", transactionId);

            // Step 3 — Send Mobile Money payment request
            boolean success = sendMobileMoneyPayment(
                    token, effectiveMethod, effectivePhone);

            log.info("FedaPay: payment result for transaction {} via {}: {}",
                    transactionId, effectiveMethod, success ? "SUCCESS" : "FAILED");

            String reference = "FEDA-" + transactionId;
            return new FedaPayResult(success, reference, transactionId);

        } catch (Exception e) {
            log.error("FedaPay processPayment error: {}", e.getMessage(), e);
            return new FedaPayResult(false,
                    "FAILED-" + System.currentTimeMillis(), null);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> createTransaction(BigDecimal montant,
                                                  String customerNom,
                                                  String customerEmail,
                                                  String telephone) {
        try {
            HttpHeaders headers = buildHeaders();

            String[] parts = customerNom.split(" ", 2);
            String firstname = parts[0];
            String lastname = parts.length > 1 ? parts[1] : parts[0];

            Map<String, Object> customer = new HashMap<>();
            customer.put("firstname", firstname);
            customer.put("lastname", lastname);
            customer.put("email", customerEmail);
            customer.put("phone_number", Map.of(
                    "number", telephone,
                    "country", "TG"
            ));

            Map<String, Object> currency = new HashMap<>();
            currency.put("iso", "XOF");

            Map<String, Object> body = new HashMap<>();
            body.put("amount", montant.intValue());
            body.put("currency", currency);
            body.put("description", "Contribution crowdfunding FoundTogo");
            body.put("callback_url", callbackUrl);
            body.put("customer", customer);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            log.info("FedaPay: creating transaction for amount={}", montant);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/transactions", entity, Map.class);

            log.info("FedaPay createTransaction response status: {}",
                    response.getStatusCode());

            if (response.getBody() != null) {
                log.info("FedaPay createTransaction response body keys: {}",
                        response.getBody().keySet());

                // Try different response structures
                Object txObj = response.getBody().get("v1/transaction");
                if (txObj instanceof Map) {
                    return (Map<String, Object>) txObj;
                }

                txObj = response.getBody().get("transaction");
                if (txObj instanceof Map) {
                    return (Map<String, Object>) txObj;
                }

                // If the body itself is the transaction
                if (response.getBody().containsKey("id")) {
                    return response.getBody();
                }
            }

            log.error("FedaPay: unexpected response structure: {}",
                    response.getBody());

        } catch (Exception e) {
            log.error("FedaPay createTransaction error: {}", e.getMessage(), e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String getPaymentToken(Long transactionId) {
        try {
            HttpHeaders headers = buildHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/transactions/" + transactionId + "/token",
                    HttpMethod.POST, entity, Map.class);

            log.info("FedaPay getToken response status: {}", response.getStatusCode());

            if (response.getBody() != null) {
                log.info("FedaPay getToken response body keys: {}",
                        response.getBody().keySet());

                Object token = response.getBody().get("token");
                if (token != null) return token.toString();

                // Some versions return it nested
                Object tokenObj = response.getBody().get("v1/token");
                if (tokenObj instanceof Map) {
                    Object t = ((Map<?, ?>) tokenObj).get("token");
                    if (t != null) return t.toString();
                }
            }
        } catch (Exception e) {
            log.error("FedaPay getToken error: {}", e.getMessage(), e);
        }
        return null;
    }

    private boolean sendMobileMoneyPayment(String token,
                                           String methodePaiement,
                                           String telephone) {
        try {
            HttpHeaders headers = buildHeaders();

            Map<String, Object> body = new HashMap<>();
            body.put("token", token);
            body.put("phone_number", Map.of(
                    "number", telephone,
                    "country", "TG"
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            log.info("FedaPay: sending {} payment for phone={}",
                    methodePaiement, telephone);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/" + methodePaiement,
                    entity, Map.class);

            log.info("FedaPay sendPayment response status: {}",
                    response.getStatusCode());
            log.info("FedaPay sendPayment response body: {}",
                    response.getBody());

            return response.getStatusCode().is2xxSuccessful();

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("FedaPay sendMobileMoneyPayment HTTP error: status={}, body={}, url={}/{}",
                    e.getStatusCode(), e.getResponseBodyAsString(), baseUrl, methodePaiement);
            return false;
        } catch (Exception e) {
            log.error("FedaPay sendMobileMoneyPayment error: {}", e.getMessage(), e);
            return false;
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(secretKey);
        return headers;
    }

    public record FedaPayResult(boolean success, String reference, Long fedaPayId) {}
}