package com.tg.crowdfunding.component;

import com.tg.crowdfunding.entity.PlatformSettings;
import com.tg.crowdfunding.repository.PlatformSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PlatformSettingsRepository platformSettingsRepository;

    @Override
    public void run(String... args) {
        if (platformSettingsRepository.findByCle("COMMISSION_RATE").isEmpty()) {
            log.info("Initializing default commission rate...");
            PlatformSettings settings = PlatformSettings.builder()
                    .cle("COMMISSION_RATE")
                    .tauxCommission(new BigDecimal("0.05")) // 5% default
                    .build();
            platformSettingsRepository.save(settings);
        }
    }
}
