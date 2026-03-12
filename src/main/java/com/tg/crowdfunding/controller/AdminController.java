package com.tg.crowdfunding.controller;

import com.tg.crowdfunding.dto.response.CampaignResponse;
import com.tg.crowdfunding.dto.response.ContributionResponse;
import com.tg.crowdfunding.entity.User;
import com.tg.crowdfunding.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import com.tg.crowdfunding.repository.PlatformSettingsRepository;
import com.tg.crowdfunding.entity.PlatformSettings;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final PlatformSettingsRepository platformSettingsRepository;

    // ===== CAMPAIGNS =====

    @GetMapping("/campaigns/pending")
    public ResponseEntity<List<CampaignResponse>> getPending() {
        return ResponseEntity.ok(adminService.getPendingCampaigns());
    }

    @GetMapping("/campaigns")
    public ResponseEntity<List<CampaignResponse>> getAllCampaigns() {
        return ResponseEntity.ok(adminService.getAllCampaigns());
    }

    @PatchMapping("/campaigns/{id}/validate")
    public ResponseEntity<CampaignResponse> validate(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.validateCampaign(id));
    }

    @PatchMapping("/campaigns/{id}/reject")
    public ResponseEntity<CampaignResponse> reject(
            @PathVariable Long id,
            @RequestParam String raison) {
        return ResponseEntity.ok(adminService.rejectCampaign(id, raison));
    }

    // ===== USERS =====

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PatchMapping("/users/{id}/suspend")
    public ResponseEntity<User> suspend(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.suspendUser(id));
    }

    @PatchMapping("/users/{id}/reactivate")
    public ResponseEntity<User> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.reactivateUser(id));
    }

    @PatchMapping("/users/{id}/ban")
    public ResponseEntity<User> ban(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.banUser(id));
    }

    // ===== TRANSACTIONS =====

    @GetMapping("/transactions")
    public ResponseEntity<List<ContributionResponse>> getTransactions() {
        return ResponseEntity.ok(adminService.getAllTransactions());
    }

    // ===== COMMISSION =====

    @GetMapping("/commission")
    public ResponseEntity<Map<String, Object>> getCommission() {
        BigDecimal rate = platformSettingsRepository.findByCle("COMMISSION_RATE")
            .map(PlatformSettings::getTauxCommission)
            .orElse(new BigDecimal("0.05"));
        return ResponseEntity.ok(Map.of("currentCommission", rate.multiply(BigDecimal.valueOf(100)).doubleValue()));
    }

    @PatchMapping("/commission")
    public ResponseEntity<Map<String, String>> updateCommission(
            @RequestParam BigDecimal rate) {
        adminService.updateCommissionRate(rate);
        return ResponseEntity.ok(Map.of("message", "Taux de commission mis a jour"));
    }

    // ===== STATISTICS =====

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }
    @GetMapping("/stats/daily")
    public ResponseEntity<Map<String, Object>> getDailyStats() {
        return ResponseEntity.ok(adminService.getDailyStats());
    }
}