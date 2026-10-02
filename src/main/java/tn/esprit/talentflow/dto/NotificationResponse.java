package tn.esprit.talentflow.dto;

import lombok.*;
import tn.esprit.talentflow.entity.NotificationType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;

    private NotificationType type;

    private String message;

    private boolean read;

    private LocalDateTime createdAt;
}