package com.tg.crowdfunding.repository;

import com.tg.crowdfunding.entity.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContributionRepository extends JpaRepository<Contribution, Long> {
    List<Contribution> findByContributeurId(Long contributeurId);
    List<Contribution> findByCampaignId(Long campaignId);
    List<Contribution> findByCampaignIdIn(List<Long> campaignIds);
    Optional<Contribution> findByReferenceTransaction(String referenceTransaction);
}