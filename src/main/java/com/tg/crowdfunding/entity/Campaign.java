package com.tg.crowdfunding.entity;

import com.tg.crowdfunding.enums.CampaignStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "campagnes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String categorie;

    private String imageUrl;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal objectifCfa;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal montantCollecte = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CampaignStatus statut = CampaignStatus.BROUILLON;

    private String raisonRejet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "porteur_id", nullable = false)
    private User porteur;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL)
    private List<Contribution> contributions;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}