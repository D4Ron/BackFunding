package com.tg.crowdfunding.dto.response;

import com.tg.crowdfunding.enums.ContributionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ContributionResponse {
    private Long id;
    private BigDecimal montant;
    private BigDecimal commission;
    private BigDecimal montantNet;
    private String telephone;
    private String referenceTransaction;
    private ContributionStatus statut;
    private Long campaignId;
    private String campaignTitre;
    private String contributeurNom;
    private LocalDateTime createdAt;
}