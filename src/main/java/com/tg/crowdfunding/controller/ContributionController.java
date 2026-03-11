package com.tg.crowdfunding.controller;

import com.tg.crowdfunding.dto.request.ContributionRequest;
import com.tg.crowdfunding.dto.response.ContributionResponse;
import com.tg.crowdfunding.entity.User;
import com.tg.crowdfunding.service.ContributionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contributions")
@RequiredArgsConstructor
public class ContributionController {

    private final ContributionService contributionService;

    // Contributeur — make a contribution
    @PostMapping("/campaign/{campaignId}")
    @PreAuthorize("hasRole('CONTRIBUTEUR')")
    public ResponseEntity<ContributionResponse> contribute(
            @PathVariable Long campaignId,
            @Valid @RequestBody ContributionRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                contributionService.contribute(campaignId, request, currentUser));
    }

    // Contributeur — view own contribution history
    @GetMapping("/my")
    @PreAuthorize("hasRole('CONTRIBUTEUR')")
    public ResponseEntity<List<ContributionResponse>> myContributions(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                contributionService.getMyContributions(currentUser));
    }

    // Porteur — view contributions on their campaign
    @GetMapping("/campaign/{campaignId}")
    @PreAuthorize("hasRole('PORTEUR_DE_PROJET') or hasRole('ADMIN')")
    public ResponseEntity<List<ContributionResponse>> campaignContributions(
            @PathVariable Long campaignId) {
        return ResponseEntity.ok(
                contributionService.getCampaignContributions(campaignId));
    }
}