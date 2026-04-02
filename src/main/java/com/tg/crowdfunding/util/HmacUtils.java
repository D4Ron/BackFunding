package com.tg.crowdfunding.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public class HmacUtils {

    private static final long DEFAULT_TOLERANCE_SECONDS = 300; // 5 minutes

    public static String computeHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }

    /**
     * Verifies a FedaPay webhook signature.
     * Header format: "t=<timestamp>,s=<signature>"
     * The HMAC is computed on "<timestamp>.<payload>".
     */
    public static boolean verifyHmac(String payload, String secret, String signatureHeader) {
        if (signatureHeader == null || !signatureHeader.contains("t=") || !signatureHeader.contains("s=")) {
            return false;
        }

        String timestamp = null;
        String expectedSignature = null;

        for (String part : signatureHeader.split(",")) {
            String trimmed = part.trim();
            if (trimmed.startsWith("t=")) {
                timestamp = trimmed.substring(2);
            } else if (trimmed.startsWith("s=")) {
                expectedSignature = trimmed.substring(2);
            }
        }

        if (timestamp == null || expectedSignature == null) {
            return false;
        }

        // Check timestamp tolerance (reject events older than 5 minutes)
        try {
            long eventTime = Long.parseLong(timestamp);
            long now = System.currentTimeMillis() / 1000;
            if (Math.abs(now - eventTime) > DEFAULT_TOLERANCE_SECONDS) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        // FedaPay signs "<timestamp>.<payload>"
        String signedPayload = timestamp + "." + payload;
        String computed = computeHmacSha256(signedPayload, secret);

        return MessageDigest.isEqual(
            computed.getBytes(StandardCharsets.UTF_8),
            expectedSignature.getBytes(StandardCharsets.UTF_8)
        );
    }
}
