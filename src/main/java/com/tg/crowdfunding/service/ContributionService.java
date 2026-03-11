package com.tg.crowdfunding.service;

import com.tg.crowdfunding.dto.request.ContributionRequest;
import com.tg.crowdfunding.dto.response.ContributionResponse;
import com.tg.crowdfunding.entity.Campaign;
import com.tg.crowdfunding.entity.Contribution;
import com.tg.crowdfunding.entity.PlatformSettings;
import com.tg.crowdfunding.entity.User;
import com.tg.crowdfunding.enums.CampaignStatus;
import com.tg.crowdfunding.enums.ContributionStatus;
import com.tg.crowdfunding.exception.ResourceNotFoundException;
import com.tg.crowdfunding.exception.UnauthorizedException;
import com.tg.crowdfunding.repository.ContributionRepository;
import com.tg.crowdfunding.repository.PlatformSettingsRepository;
import com.tg.crowdfunding.service.MockPaymentService.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContributionService {

    private final ContributionRepository contributionRepository;
    private final PlatformSettingsRepository platformSettingsRepository;
    private final CampaignService campaignService;
    private final FedaPayService fedaPayService;
    private final NotificationService notificationService;

    @Transactional
    public ContributionResponse contribute(Long campaignId,
                                           ContributionRequest request,
                                           User contributeur) {
        Campaign campaign = campaignService.findById(campaignId);

        if (campaign.getStatut() != CampaignStatus.ACTIVE) {
            throw new UnauthorizedException("Cette campagne n'accepte pas de contributions");
        }

        // Get commission rate from DB, default to 5% if not found
        BigDecimal commissionRate = platformSettingsRepository
                .findByCle("COMMISSION_RATE")
                .map(PlatformSettings::getTauxCommission)
                .orElse(new BigDecimal("0.05"));

        BigDecimal montant = request.getMontant();
        BigDecimal commission = montant.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal montantNet = montant.subtract(commission);

        // Process Mobile Money payment
        FedaPayService.FedaPayResult result = fedaPayService.processPayment(
                request.getTelephone(),
                montant,
                request.getMethodePaiement(),
                contributeur.getNom(),
                contributeur.getEmail()
        );

        ContributionStatus statut = result.success()
                ? ContributionStatus.SUCCESS
                : ContributionStatus.FAILED;

        Contribution contribution = Contribution.builder()
                .montant(montant)
                .commission(commission)
                .montantNet(montantNet)
                .telephone(request.getTelephone())
                .referenceTransaction(result.reference())
                .statut(statut)
                .contributeur(contributeur)
                .campaign(campaign)
                .build();

        contributionRepository.save(contribution);

        // Only update campaign amount if payment succeeded
        // For sandbox testing we update immediately since webhook may not reach localhost
        if (result.success()) {
            campaign.setMontantCollecte(
                    campaign.getMontantCollecte().add(montantNet));
            if (campaign.getMontantCollecte()
                    .compareTo(campaign.getObjectifCfa()) >= 0) {
                campaign.setStatut(CampaignStatus.FINANCEE);
            }
            notificationService.notifyContribution(contribution);
        }
        return toResponse(contribution);
    }

    public List<ContributionResponse> getMyContributions(User contributeur) {
        return contributionRepository.findByContributeurId(contributeur.getId())
                .stream().map(this::toResponse).toList();
    }

    public List<ContributionResponse> getCampaignContributions(Long campaignId) {
        return contributionRepository.findByCampaignId(campaignId)
                .stream().map(this::toResponse).toList();
    }
    public void updateContributionStatus(String reference, boolean success) {
        contributionRepository.findByReferenceTransaction(reference).ifPresent(contribution -> {
            contribution.setStatut(success ? ContributionStatus.SUCCESS : ContributionStatus.FAILED);
            contributionRepository.save(contribution);

            if (success) {
                Campaign campaign = contribution.getCampaign();
                campaign.setMontantCollecte(
                        campaign.getMontantCollecte().add(contribution.getMontantNet()));
                if (campaign.getMontantCollecte().compareTo(campaign.getObjectifCfa()) >= 0) {
                    campaign.setStatut(CampaignStatus.FINANCEE);
                }
                notificationService.notifyContribution(contribution);
            }
        });
    }

    public ContributionResponse toResponse(Contribution c) {
        return ContributionResponse.builder()
                .id(c.getId())
                .montant(c.getMontant())
                .commission(c.getCommission())
                .montantNet(c.getMontantNet())
                .telephone(c.getTelephone())
                .referenceTransaction(c.getReferenceTransaction())
                .statut(c.getStatut())
                .campaignId(c.getCampaign().getId())
                .campaignTitre(c.getCampaign().getTitre())
                .contributeurNom(c.getContributeur().getNom())
                .createdAt(c.getCreatedAt())
                .build();
    }
}