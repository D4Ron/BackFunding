package com.tg.crowdfunding.repository;

import com.tg.crowdfunding.entity.PlatformSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformSettingsRepository extends JpaRepository<PlatformSettings, Long> {
    Optional<PlatformSettings> findByCle(String cle);
}