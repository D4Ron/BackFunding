package com.tg.crowdfunding.entity;

import com.tg.crowdfunding.enums.ContributionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "contributions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Contribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal commission;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montantNet;

    @Column(nullable = false)
    private String telephone;

    @Column(nullable = false, unique = true)
    private String referenceTransaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ContributionStatus statut = ContributionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contributeur_id", nullable = false)
    private User contributeur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campagne_id", nullable = false)
    private Campaign campaign;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}