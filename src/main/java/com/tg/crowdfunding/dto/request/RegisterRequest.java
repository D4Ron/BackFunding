package com.tg.crowdfunding.dto.request;

import com.tg.crowdfunding.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String motDePasse;

    @NotNull(message = "Le rôle est obligatoire")
    private Role role;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(
        regexp = "^(\\+228|00228)?[0-9]{8}$",
        message = "Numéro invalide. Format: 90000000"
    )
    private String telephone;
}