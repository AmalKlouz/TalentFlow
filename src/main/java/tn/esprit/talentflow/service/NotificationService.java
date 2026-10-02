package tn.esprit.talentflow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import tn.esprit.talentflow.dto.NotificationResponse;
import tn.esprit.talentflow.entity.Notification;
import tn.esprit.talentflow.entity.NotificationType;
import tn.esprit.talentflow.entity.User;
import tn.esprit.talentflow.repository.NotificationRepository;
import tn.esprit.talentflow.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;


    // =========================================================
    // GET MY NOTIFICATIONS
    // =========================================================

    public List<NotificationResponse> getMyNotifications() {

        User user = getCurrentUser();

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =========================================================
    // GET MY UNREAD NOTIFICATIONS
    // =========================================================
    public void createNotification(
            User user,
            NotificationType type,
            String message) {

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .message(message)
                .read(false)
                .createdAt(java.time.LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }
    public List<NotificationResponse> getMyUnreadNotifications() {

        User user = getCurrentUser();

        return notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                        user.getId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =========================================================
    // GET UNREAD NOTIFICATIONS COUNT
    // =========================================================

    public long getUnreadCount() {

        User user = getCurrentUser();

        return notificationRepository
                .countByUserIdAndReadFalse(user.getId());
    }


    // =========================================================
    // CURRENT USER
    // =========================================================

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur introuvable"
                        )
                );
    }


    // =========================================================
    // ENTITY → DTO
    // =========================================================

    private NotificationResponse toResponse(
            Notification notification) {

        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
    public NotificationResponse markAsRead(Long notificationId) {

        User user = getCurrentUser();

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification introuvable"
                                )
                        );

        // Vérifier que la notification appartient
        // bien à l'utilisateur connecté
        if (!notification.getUser().getId().equals(user.getId())) {

            throw new RuntimeException(
                    "Accès interdit à cette notification"
            );
        }

        notification.setRead(true);

        Notification updated =
                notificationRepository.save(notification);

        return toResponse(updated);
    }
    public void markAllAsRead() {

        User user = getCurrentUser();

        List<Notification> notifications =
                notificationRepository
                        .findByUserIdOrderByCreatedAtDesc(
                                user.getId()
                        );

        notifications.forEach(notification ->
                notification.setRead(true)
        );

        notificationRepository.saveAll(notifications);
    }
}