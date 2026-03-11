package com.tg.crowdfunding.service;

import com.tg.crowdfunding.dto.response.NotificationResponse;
import com.tg.crowdfunding.entity.Campaign;
import com.tg.crowdfunding.entity.Contribution;
import com.tg.crowdfunding.entity.Notification;
import com.tg.crowdfunding.entity.User;
import com.tg.crowdfunding.enums.NotificationType;
import com.tg.crowdfunding.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public void notifyContribution(Contribution contribution) {
        Campaign campaign = contribution.getCampaign();
        User porteur = campaign.getPorteur();
        User contributeur = contribution.getContributeur();

        String message = String.format(
                "Nouvelle contribution de %s CFA recue sur votre campagne '%s' de la part de %s. Reference: %s",
                contribution.getMontantNet(),
                campaign.getTitre(),
                contributeur.getNom(),
                contribution.getReferenceTransaction()
        );

        // Internal notification for porteur
        createNotification(porteur, message, NotificationType.CONTRIBUTION_RECEIVED);

        // Email to porteur
        emailService.sendEmail(
                porteur.getEmail(),
                "Nouvelle contribution sur " + campaign.getTitre(),
                message
        );

        // Check milestones
        double percentage = campaign.getMontantCollecte()
                .divide(campaign.getObjectifCfa(), 4,
                        java.math.RoundingMode.HALF_UP)
                .multiply(java.math.BigDecimal.valueOf(100))
                .doubleValue();

        if (percentage >= 100) {
            String funded = String.format(
                    "Felicitations! Votre campagne '%s' est completement financee!",
                    campaign.getTitre());
            createNotification(porteur, funded, NotificationType.MILESTONE_100);
            emailService.sendEmail(porteur.getEmail(),
                    "Campagne financee a 100%!", funded);

        } else if (percentage >= 50) {
            String half = String.format(
                    "Votre campagne '%s' a atteint 50%% de son objectif!",
                    campaign.getTitre());
            createNotification(porteur, half, NotificationType.MILESTONE_50);
        }
    }

    public void notifyCampaignValidated(Campaign campaign) {
        String message = String.format(
                "Votre campagne '%s' a ete validee et est maintenant active.",
                campaign.getTitre());
        createNotification(campaign.getPorteur(), message,
                NotificationType.CAMPAIGN_VALIDATED);
        emailService.sendEmail(campaign.getPorteur().getEmail(),
                "Campagne validee!", message);
    }

    public void notifyCampaignRejected(Campaign campaign) {
        String message = String.format(
                "Votre campagne '%s' a ete rejetee. Raison: %s",
                campaign.getTitre(), campaign.getRaisonRejet());
        createNotification(campaign.getPorteur(), message,
                NotificationType.CAMPAIGN_REJECTED);
        emailService.sendEmail(campaign.getPorteur().getEmail(),
                "Campagne rejetee", message);
    }

    public List<NotificationResponse> getMyNotifications(User user) {
        return notificationRepository
                .findByUtilisateurIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).toList();
    }

    public long countUnread(User user) {
        return notificationRepository.countByUtilisateurIdAndLuFalse(user.getId());
    }

    public void markAllAsRead(User user) {
        List<Notification> notifications = notificationRepository
                .findByUtilisateurIdOrderByCreatedAtDesc(user.getId());
        notifications.forEach(n -> n.setLu(true));
        notificationRepository.saveAll(notifications);
    }

    private void createNotification(User user, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .utilisateur(user)
                .message(message)
                .type(type)
                .lu(false)
                .build();
        notificationRepository.save(notification);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .message(n.getMessage())
                .type(n.getType())
                .lu(n.isLu())
                .createdAt(n.getCreatedAt())
                .build();
    }
}