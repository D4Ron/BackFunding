package com.tg.crowdfunding.repository;

import com.tg.crowdfunding.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUtilisateurIdOrderByCreatedAtDesc(Long utilisateurId);
    long countByUtilisateurIdAndLuFalse(Long utilisateurId);
}