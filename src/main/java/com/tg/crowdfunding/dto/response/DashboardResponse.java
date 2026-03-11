package com.tg.crowdfunding.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardResponse {
    private int totalCampaigns;
    private int activeCampaigns;
    private int pendingCampaigns;
    private int fundedCampaigns;
    private BigDecimal totalCollected;
    private int totalContributors;
    private List<CampaignResponse> recentCampaigns;
    private Map<String, BigDecimal> dailyCollections;
}