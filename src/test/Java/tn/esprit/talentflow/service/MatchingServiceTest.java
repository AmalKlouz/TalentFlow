package tn.esprit.talentflow.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tn.esprit.talentflow.entity.Job;
import tn.esprit.talentflow.entity.User;
import tn.esprit.talentflow.repository.JobRepository;
import tn.esprit.talentflow.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private MatchingService matchingService;

    @Test
    void shouldThrowExceptionWhenCandidateDoesNotExist() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> matchingService.calculateMatching(1L, 1L));
    }

    @Test
    void shouldThrowExceptionWhenJobDoesNotExist() {
        User candidate = User.builder()
                .id(1L)
                .skills("Java, Spring Boot")
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(jobRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> matchingService.calculateMatching(1L, 1L));
    }
}