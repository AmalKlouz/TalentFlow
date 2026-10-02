package tn.esprit.talentflow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import tn.esprit.talentflow.dto.NotificationResponse;
import tn.esprit.talentflow.entity.Notification;
import tn.esprit.talentflow.entity.NotificationType;
import tn.esprit.talentflow.entity.Role;
import tn.esprit.talentflow.entity.User;
import tn.esprit.talentflow.exception.ResourceNotFoundException;
import tn.esprit.talentflow.repository.NotificationRepository;
import tn.esprit.talentflow.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Notification notification;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(1L)
                .firstName("Amal")
                .lastName("Candidate")
                .email("amal.candidate@talentflow.com")
                .role(Role.CANDIDATE)
                .enabled(true)
                .build();

        notification = Notification.builder()
                .id(100L)
                .user(user)
                .type(NotificationType.INTERVIEW_SCHEDULED)
                .message(
                        "Un entretien a été planifié pour votre candidature"
                )
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void authenticateUser() {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null
                )
        );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
    }


    @Test
    void shouldCreateNotification() {

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(notification);

        notificationService.createNotification(
                user,
                NotificationType.INTERVIEW_SCHEDULED,
                "Un entretien a été planifié"
        );

        verify(notificationRepository)
                .save(any(Notification.class));
    }


    @Test
    void shouldGetMyNotifications() {

        authenticateUser();

        when(
                notificationRepository
                        .findByUserIdOrderByCreatedAtDesc(1L)
        ).thenReturn(List.of(notification));

        List<NotificationResponse> responses =
                notificationService.getMyNotifications();

        assertNotNull(responses);
        assertEquals(1, responses.size());

        NotificationResponse response = responses.get(0);

        assertEquals(100L, response.getId());
        assertEquals(
                NotificationType.INTERVIEW_SCHEDULED,
                response.getType()
        );
        assertEquals(
                "Un entretien a été planifié pour votre candidature",
                response.getMessage()
        );
        assertFalse(response.isRead());
        assertNotNull(response.getCreatedAt());

        verify(notificationRepository)
                .findByUserIdOrderByCreatedAtDesc(1L);

        SecurityContextHolder.clearContext();
    }


    @Test
    void shouldGetMyUnreadNotifications() {

        authenticateUser();

        when(
                notificationRepository
                        .findByUserIdAndReadFalseOrderByCreatedAtDesc(1L)
        ).thenReturn(List.of(notification));

        List<NotificationResponse> responses =
                notificationService.getMyUnreadNotifications();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertFalse(responses.get(0).isRead());

        verify(notificationRepository)
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(1L);

        SecurityContextHolder.clearContext();
    }


    @Test
    void shouldGetUnreadCount() {

        authenticateUser();

        when(
                notificationRepository
                        .countByUserIdAndReadFalse(1L)
        ).thenReturn(3L);

        long count =
                notificationService.getUnreadCount();

        assertEquals(3L, count);

        verify(notificationRepository)
                .countByUserIdAndReadFalse(1L);

        SecurityContextHolder.clearContext();
    }


    @Test
    void shouldMarkNotificationAsRead() {

        authenticateUser();

        when(notificationRepository.findById(100L))
                .thenReturn(Optional.of(notification));

        when(notificationRepository.save(notification))
                .thenReturn(notification);

        NotificationResponse response =
                notificationService.markAsRead(100L);

        assertNotNull(response);
        assertTrue(response.isRead());
        assertTrue(notification.isRead());

        verify(notificationRepository)
                .save(notification);

        SecurityContextHolder.clearContext();
    }


    @Test
    void shouldThrowExceptionWhenNotificationDoesNotExist() {

        authenticateUser();

        when(notificationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> notificationService.markAsRead(999L)
        );

        verify(notificationRepository, never())
                .save(any(Notification.class));

        SecurityContextHolder.clearContext();
    }


    @Test
    void shouldRejectNotificationBelongingToAnotherUser() {

        authenticateUser();

        User otherUser = User.builder()
                .id(2L)
                .firstName("Other")
                .lastName("User")
                .email("other@talentflow.com")
                .role(Role.CANDIDATE)
                .enabled(true)
                .build();

        Notification otherNotification = Notification.builder()
                .id(200L)
                .user(otherUser)
                .type(NotificationType.INTERVIEW_SCHEDULED)
                .message("Notification privée")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationRepository.findById(200L))
                .thenReturn(Optional.of(otherNotification));

        assertThrows(
                IllegalArgumentException.class,
                () -> notificationService.markAsRead(200L)
        );

        verify(notificationRepository, never())
                .save(any(Notification.class));

        SecurityContextHolder.clearContext();
    }


    @Test
    void shouldMarkAllNotificationsAsRead() {

        authenticateUser();

        Notification notification2 = Notification.builder()
                .id(101L)
                .user(user)
                .type(NotificationType.APPLICATION_STATUS_CHANGED)
                .message("Votre candidature a changé de statut")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(
                notificationRepository
                        .findByUserIdOrderByCreatedAtDesc(1L)
        ).thenReturn(
                List.of(notification, notification2)
        );

        notificationService.markAllAsRead();

        assertTrue(notification.isRead());
        assertTrue(notification2.isRead());

        verify(notificationRepository)
                .saveAll(anyList());

        SecurityContextHolder.clearContext();
    }


    @Test
    void shouldThrowExceptionWhenCurrentUserDoesNotExist() {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "unknown@talentflow.com",
                        null
                )
        );

        when(
                userRepository.findByEmail(
                        "unknown@talentflow.com"
                )
        ).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> notificationService.getMyNotifications()
        );

        SecurityContextHolder.clearContext();
    }
}