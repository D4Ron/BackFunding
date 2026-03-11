package com.tg.crowdfunding.service;

import com.tg.crowdfunding.dto.response.CampaignResponse;
import com.tg.crowdfunding.dto.response.ContributionResponse;
import com.tg.crowdfunding.entity.Campaign;
import com.tg.crowdfunding.entity.User;
import com.tg.crowdfunding.enums.CampaignStatus;
import com.tg.crowdfunding.exception.ResourceNotFoundException;
import com.tg.crowdfunding.repository.CampaignRepository;
import com.tg.crowdfunding.repository.ContributionRepository;
import com.tg.crowdfunding.repository.PlatformSettingsRepository;
import com.tg.crowdfunding.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final ContributionRepository contributionRepository;
    private final PlatformSettingsRepository platformSettingsRepository;
    private final CampaignService campaignService;
    private final ContributionService contributionService;
    private final NotificationService notificationService;

    // ===== CAMPAIGN VALIDATION =====

    public CampaignResponse validateCampaign(Long id) {
        Campaign campaign = findCampaign(id);
        if (campaign.getStatut() != CampaignStatus.EN_ATTENTE_VALIDATION) {
            throw new IllegalStateException("La campagne n'est pas en attente de validation");
        }
        campaign.setStatut(CampaignStatus.ACTIVE);
        Campaign saved = campaignRepository.save(campaign);
        notificationService.notifyCampaignValidated(saved);
        return campaignService.toResponse(saved);
    }

    public CampaignResponse rejectCampaign(Long id, String raison) {
        Campaign campaign = findCampaign(id);
        if (campaign.getStatut() != CampaignStatus.EN_ATTENTE_VALIDATION) {
            throw new IllegalStateException("La campagne n'est pas en attente de validation");
        }
        campaign.setStatut(CampaignStatus.REJETEE);
        campaign.setRaisonRejet(raison);
        Campaign saved = campaignRepository.save(campaign);
        notificationService.notifyCampaignRejected(saved);
        return campaignService.toResponse(saved);
    }

    public List<CampaignResponse> getPendingCampaigns() {
        return campaignRepository.findByStatut(CampaignStatus.EN_ATTENTE_VALIDATION)
                .stream().map(campaignService::toResponse).toList();
    }

    public List<CampaignResponse> getAllCampaigns() {
        return campaignRepository.findAll()
                .stream().map(campaignService::toResponse).toList();
    }

    // ===== USER MANAGEMENT =====

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User suspendUser(Long id) {
        User user = findUser(id);
        user.setActif(false);
        return userRepository.save(user);
    }

    public User reactivateUser(Long id) {
        User user = findUser(id);
        user.setActif(true);
        return userRepository.save(user);
    }

    public User banUser(Long id) {
        User user = findUser(id);
        user.setBanni(true);
        user.setActif(false);
        return userRepository.save(user);
    }

    // ===== TRANSACTIONS =====

    public List<ContributionResponse> getAllTransactions() {
        return contributionRepository.findAll()
                .stream().map(contributionService::toResponse).toList();
    }

    // ===== COMMISSION =====

    public void updateCommissionRate(BigDecimal newRate) {
        var settings = platformSettingsRepository.findByCle("COMMISSION_RATE")
                .orElseThrow(() -> new ResourceNotFoundException("Parametre introuvable"));
        settings.setTauxCommission(newRate);
        platformSettingsRepository.save(settings);
    }

    // ===== STATISTICS =====

    public Map<String, Object> getStats() {
        long totalUsers = userRepository.count();
        long activeCampaigns = campaignRepository.findByStatut(CampaignStatus.ACTIVE).size();
        long pendingCampaigns = campaignRepository.findByStatut(CampaignStatus.EN_ATTENTE_VALIDATION).size();

        BigDecimal totalCollected = contributionRepository.findAll().stream()
                .filter(c -> c.getStatut().name().equals("SUCCESS"))
                .map(c -> c.getMontantNet())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalContributions = contributionRepository.count();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Long> dailyContributions = contributionRepository.findAll().stream()
                .filter(c -> c.getStatut().name().equals("SUCCESS"))
                .collect(Collectors.groupingBy(
                        c -> c.getCreatedAt().toLocalDate().format(formatter),
                        Collectors.counting()
                ));

        return Map.of(
                "totalUtilisateurs", totalUsers,
                "campagnesActives", activeCampaigns,
                "campagnesEnAttente", pendingCampaigns,
                "totalCollecte", totalCollected,
                "totalContributions", totalContributions,
                "contributionsJournalieres", dailyContributions
        );
    }
    public Map<String, Object> getDailyStats() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<String, Long> dailyCount = contributionRepository.findAll().stream()
                .filter(c -> c.getStatut().name().equals("SUCCESS"))
                .collect(Collectors.groupingBy(
                        c -> c.getCreatedAt().toLocalDate().format(formatter),
                        Collectors.counting()
                ));
        //TODO Make success a constant at the top and then use it where needed instead

        Map<String, BigDecimal> dailyAmount = contributionRepository.findAll().stream()
                .filter(c -> c.getStatut().name().equals("SUCCESS"))
                .collect(Collectors.groupingBy(
                        c -> c.getCreatedAt().toLocalDate().format(formatter),
                        Collectors.reducing(BigDecimal.ZERO,
                                c -> c.getMontantNet(), BigDecimal::add)
                ));

        return Map.of(
                "contributionsParJour", dailyCount,
                "montantsParJour", dailyAmount
        );
    }

    private Campaign findCampaign(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne non trouvee"));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));
    }
}