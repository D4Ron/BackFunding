package com.tg.crowdfunding.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "parametres_plateforme")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PlatformSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal tauxCommission;

    @Column(nullable = false, unique = true)
    private String cle;
}