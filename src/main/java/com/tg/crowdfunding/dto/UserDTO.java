package com.tg.crowdfunding.dto;

import java.time.LocalDateTime;

public record UserDTO(
    Long id,
    String nom,
    String email,
    String telephone,
    String role,
    LocalDateTime createdAt
) {}
