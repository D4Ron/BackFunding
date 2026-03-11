package com.tg.crowdfunding.repository;

import com.tg.crowdfunding.entity.Campaign;
import com.tg.crowdfunding.enums.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByPorteurId(Long porteurId);
    List<Campaign> findByStatut(CampaignStatus statut);
}