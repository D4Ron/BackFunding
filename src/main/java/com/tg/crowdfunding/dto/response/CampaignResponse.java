package com.tg.crowdfunding.dto.response;

import com.tg.crowdfunding.enums.CampaignStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@Builder
public class CampaignResponse {
    private Long id;
    private String titre;
    private String description;
    private String categorie;
    private String imageUrl;
    private BigDecimal objectifCfa;
    private BigDecimal montantCollecte;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private CampaignStatus statut;
    private String porteurNom;
    private Long porteurId;
    private int nombreContributeurs;
    private double pourcentageAtteint;
    private long joursRestants;
    private LocalDateTime createdAt;
}