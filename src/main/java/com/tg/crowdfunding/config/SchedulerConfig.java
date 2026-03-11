package com.tg.crowdfunding.config;

import com.tg.crowdfunding.service.CampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {

    private final CampaignService campaignService;

    // Runs every hour
    @Scheduled(fixedRate = 3600000)
    public void refreshCampaignStatuses() {
        log.info("Scheduler: mise a jour des statuts de campagnes...");
        campaignService.refreshStatuses();
        log.info("Scheduler: termine.");
    }
}