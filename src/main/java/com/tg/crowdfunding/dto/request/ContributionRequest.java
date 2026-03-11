package com.tg.crowdfunding.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ContributionRequest {

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "100.00", message = "Le montant minimum est 100 CFA")
    private BigDecimal montant;

    @NotBlank(message = "Le numero de telephone est obligatoire")
    @Pattern(regexp = "^(\\+228|00228)?[0-9]{8}$",
            message = "Numero de telephone togolais invalide")
    private String telephone;

    @NotBlank(message = "La methode de paiement est obligatoire")
    @Pattern(regexp = "moov_tg|togocel",
            message = "Methode de paiement invalide. Utilisez moov_tg ou togocel")
    private String methodePaiement;
}