package tn.esprit.talentflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.talentflow.entity.Notification;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(
            Long userId
    );
    void deleteByUserId(Long userId);
    long countByUserIdAndReadFalse(Long userId);
}