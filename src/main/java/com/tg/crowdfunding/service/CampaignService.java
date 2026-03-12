package com.tg.crowdfunding.service;

import com.tg.crowdfunding.dto.request.CampaignRequest;
import com.tg.crowdfunding.dto.response.CampaignResponse;
import com.tg.crowdfunding.entity.Campaign;
import com.tg.crowdfunding.entity.User;
import com.tg.crowdfunding.enums.CampaignStatus;
import com.tg.crowdfunding.exception.ResourceNotFoundException;
import com.tg.crowdfunding.exception.UnauthorizedException;
import com.tg.crowdfunding.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.tg.crowdfunding.dto.response.DashboardResponse;
import com.tg.crowdfunding.enums.CampaignStatus;
import com.tg.crowdfunding.repository.ContributionRepository;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final ContributionRepository contributionRepository;

    public CampaignResponse create(CampaignRequest request, User porteur) {
        Campaign campaign = Campaign.builder()
                .titre(request.getTitre())
                .description(request.getDescription())
                .categorie(request.getCategorie())
                .imageUrl(request.getImageUrl())
                .objectifCfa(request.getObjectifCfa())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .statut(CampaignStatus.BROUILLON)
                .porteur(porteur)
                .build();

        return toResponse(campaignRepository.save(campaign));
    }

    public List<CampaignResponse> getActiveCampaigns() {
        refreshStatuses();
        return campaignRepository.findByStatut(CampaignStatus.ACTIVE)
                .stream().map(this::toResponse).toList();
    }

    public List<CampaignResponse> getMyCampaigns(User porteur) {
        return campaignRepository.findByPorteurId(porteur.getId())
                .stream().map(this::toResponse).toList();
    }

    public CampaignResponse getById(Long id) {
        return toResponse(findById(id));
    }

    public CampaignResponse update(Long id, CampaignRequest request, User porteur) {
        Campaign campaign = findById(id);

        if (!campaign.getPorteur().getId().equals(porteur.getId())) {
            throw new UnauthorizedException("Vous n'êtes pas le porteur de cette campagne");
        }
        if (campaign.getStatut() != CampaignStatus.BROUILLON) {
            throw new UnauthorizedException("Seules les campagnes en brouillon peuvent être modifiées");
        }

        campaign.setTitre(request.getTitre());
        campaign.setDescription(request.getDescription());
        campaign.setCategorie(request.getCategorie());
        campaign.setImageUrl(request.getImageUrl());
        campaign.setObjectifCfa(request.getObjectifCfa());
        campaign.setDateDebut(request.getDateDebut());
        campaign.setDateFin(request.getDateFin());

        return toResponse(campaignRepository.save(campaign));
    }

    public void delete(Long id, User porteur) {
        Campaign campaign = findById(id);

        if (!campaign.getPorteur().getId().equals(porteur.getId())) {
            throw new UnauthorizedException("Vous n'êtes pas le porteur de cette campagne");
        }
        if (campaign.getStatut() != CampaignStatus.BROUILLON) {
            throw new UnauthorizedException("Seules les campagnes en brouillon peuvent être supprimées");
        }

        campaignRepository.delete(campaign);
    }

    public CampaignResponse submitForValidation(Long id, User porteur) {
        Campaign campaign = findById(id);

        log.info("Submit: campId={}, ownerId={}, callerEmail={}, status={}",
                id, campaign.getPorteur().getId(), porteur.getEmail(), campaign.getStatut());

        if (!campaign.getPorteur().getId().equals(porteur.getId())) {
            throw new UnauthorizedException("Vous n'êtes pas le porteur de cette campagne");
        }
        if (campaign.getStatut() != CampaignStatus.BROUILLON) {
            throw new UnauthorizedException("Seules les campagnes en brouillon peuvent être soumises");
        }

        campaign.setStatut(CampaignStatus.EN_ATTENTE_VALIDATION);
        return toResponse(campaignRepository.save(campaign));
    }

    // Called internally to auto-update campaign statuses
    public void refreshStatuses() {
        List<Campaign> active = campaignRepository.findByStatut(CampaignStatus.ACTIVE);
        for (Campaign c : active) {
            if (c.getMontantCollecte().compareTo(c.getObjectifCfa()) >= 0) {
                c.setStatut(CampaignStatus.FINANCEE);
                campaignRepository.save(c);
            } else if (c.getDateFin().isBefore(LocalDate.now())) {
                c.setStatut(CampaignStatus.EXPIREE);
                campaignRepository.save(c);
            }
        }
    }

    public Campaign findById(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne non trouvée"));
    }

    public CampaignResponse toResponse(Campaign c) {
        double pourcentage = 0;
        if (c.getObjectifCfa().compareTo(BigDecimal.ZERO) > 0) {
            pourcentage = c.getMontantCollecte()
                    .divide(c.getObjectifCfa(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        long joursRestants = Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), c.getDateFin()));

        int nbContributeurs = c.getContributions() == null ? 0 : c.getContributions().size();

        return CampaignResponse.builder()
                .id(c.getId())
                .titre(c.getTitre())
                .description(c.getDescription())
                .categorie(c.getCategorie())
                .imageUrl(c.getImageUrl())
                .objectifCfa(c.getObjectifCfa())
                .montantCollecte(c.getMontantCollecte())
                .dateDebut(c.getDateDebut())
                .dateFin(c.getDateFin())
                .statut(c.getStatut())
                .porteurNom(c.getPorteur().getNom())
                .porteurId(c.getPorteur().getId())
                .nombreContributeurs(nbContributeurs)
                .pourcentageAtteint(pourcentage)
                .joursRestants(joursRestants)
                .createdAt(c.getCreatedAt())
                .build();
    }
    public DashboardResponse getPorteurDashboard(User porteur) {
        List<Campaign> myCampaigns = campaignRepository.findByPorteurId(porteur.getId());

        int total = myCampaigns.size();
        int active = (int) myCampaigns.stream()
                .filter(c -> c.getStatut() == CampaignStatus.ACTIVE).count();
        int pending = (int) myCampaigns.stream()
                .filter(c -> c.getStatut() == CampaignStatus.EN_ATTENTE_VALIDATION).count();
        int funded = (int) myCampaigns.stream()
                .filter(c -> c.getStatut() == CampaignStatus.FINANCEE).count();

        BigDecimal totalCollected = myCampaigns.stream()
                .map(Campaign::getMontantCollecte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total unique contributors across all campaigns
        int totalContributors = myCampaigns.stream()
                .mapToInt(c -> c.getContributions() == null ? 0 : c.getContributions().size())
                .sum();

        // Daily collections for the last 7 days across all porteur campaigns
        List<Long> campaignIds = myCampaigns.stream()
                .map(Campaign::getId).toList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<String, BigDecimal> dailyCollections = campaignIds.isEmpty()
                ? Map.of()
                : contributionRepository.findByCampaignIdIn(campaignIds).stream()
                .filter(c -> c.getStatut().name().equals("SUCCESS"))
                .collect(Collectors.groupingBy(
                        c -> c.getCreatedAt().toLocalDate().format(formatter),
                        Collectors.reducing(BigDecimal.ZERO,
                                c -> c.getMontantNet(), BigDecimal::add)
                ));

        List<CampaignResponse> recentCampaigns = myCampaigns.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map(this::toResponse)
                .toList();

        return DashboardResponse.builder()
                .totalCampaigns(total)
                .activeCampaigns(active)
                .pendingCampaigns(pending)
                .fundedCampaigns(funded)
                .totalCollected(totalCollected)
                .totalContributors(totalContributors)
                .recentCampaigns(recentCampaigns)
                .dailyCollections(dailyCollections)
                .build();
    }
}