package com.tg.crowdfunding.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CampaignRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotBlank(message = "La catégorie est obligatoire")
    private String categorie;

    private String imageUrl;

    @NotNull(message = "L'objectif est obligatoire")
    @DecimalMin(value = "1000.00", message = "L'objectif minimum est 1000 CFA")
    private BigDecimal objectifCfa;

    @NotNull(message = "La date de début est obligatoire")
    @FutureOrPresent(message = "La date de début ne peut pas être dans le passé")
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    @Future(message = "La date de fin doit être dans le futur")
    private LocalDate dateFin;
}