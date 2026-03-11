package com.tg.crowdfunding.controller;

import com.tg.crowdfunding.dto.request.CampaignRequest;
import com.tg.crowdfunding.dto.response.CampaignResponse;
import com.tg.crowdfunding.dto.response.DashboardResponse;
import com.tg.crowdfunding.entity.User;
import com.tg.crowdfunding.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CampaignController {

    private final CampaignService campaignService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('PORTEUR_DE_PROJET')")
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(campaignService.getPorteurDashboard(currentUser));
    }

    // Public — anyone can browse active campaigns
    @GetMapping("/public")
    public ResponseEntity<List<CampaignResponse>> getActiveCampaigns() {
        return ResponseEntity.ok(campaignService.getActiveCampaigns());
    }

    // Public — view single campaign
    @GetMapping("/public/{id}")
    public ResponseEntity<CampaignResponse> getCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getById(id));
    }

    // Porteur — create campaign
    @PostMapping
    @PreAuthorize("hasRole('PORTEUR_DE_PROJET')")
    public ResponseEntity<CampaignResponse> create(
            @Valid @RequestBody CampaignRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(campaignService.create(request, currentUser));
    }

    // Porteur — view own campaigns
    @GetMapping("/my")
    @PreAuthorize("hasRole('PORTEUR_DE_PROJET')")
    public ResponseEntity<List<CampaignResponse>> myCampaigns(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(campaignService.getMyCampaigns(currentUser));
    }

    // Porteur — update campaign
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PORTEUR_DE_PROJET')")
    public ResponseEntity<CampaignResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CampaignRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(campaignService.update(id, request, currentUser));
    }

    // Porteur — delete campaign
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PORTEUR_DE_PROJET')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        campaignService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    // Porteur — submit for admin validation
    @PatchMapping("/{id}/submit")
    @PreAuthorize("hasRole('PORTEUR_DE_PROJET')")
    public ResponseEntity<CampaignResponse> submit(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(campaignService.submitForValidation(id, currentUser));
    }
}