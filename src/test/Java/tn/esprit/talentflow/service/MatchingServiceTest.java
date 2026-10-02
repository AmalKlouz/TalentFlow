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
    void shouldCalculateMatchingScore() {

        User candidate = User.builder()
                .id(1L)
                .firstName("Amine")
                .lastName("Khelifi")
                .skills("Java, Spring Boot")
                .build();

        Job job = Job.builder()
                .id(1L)
                .title("Développeur Java")
                .requiredSkills("Java, Spring Boot, Angular, PostgreSQL")
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(jobRepository.findById(1L))
                .thenReturn(Optional.of(job));

        var result = matchingService.calculateMatching(1L, 1L);

        assertEquals(50.0, result.getScore());

        assertEquals(2, result.getMatchingSkills().size());

        assertTrue(result.getMatchingSkills().contains("java"));
        assertTrue(result.getMatchingSkills().contains("spring boot"));

        assertEquals(2, result.getMissingSkills().size());

        assertTrue(result.getMissingSkills().contains("angular"));
        assertTrue(result.getMissingSkills().contains("postgresql"));
    }


    @Test
    void shouldReturn100PercentWhenAllSkillsMatch() {

        User candidate = User.builder()
                .id(1L)
                .firstName("Amine")
                .lastName("Khelifi")
                .skills("Java, Spring Boot, Angular")
                .build();

        Job job = Job.builder()
                .id(1L)
                .title("Développeur Full Stack")
                .requiredSkills("Java, Spring Boot, Angular")
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(jobRepository.findById(1L))
                .thenReturn(Optional.of(job));

        var result = matchingService.calculateMatching(1L, 1L);

        assertEquals(100.0, result.getScore());

        assertEquals(3, result.getMatchingSkills().size());

        assertTrue(result.getMissingSkills().isEmpty());
    }


    @Test
    void shouldReturn0PercentWhenNoSkillMatches() {

        User candidate = User.builder()
                .id(1L)
                .firstName("Amine")
                .lastName("Khelifi")
                .skills("Python, Django")
                .build();

        Job job = Job.builder()
                .id(1L)
                .title("Développeur Java")
                .requiredSkills("Java, Spring Boot, Angular")
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(jobRepository.findById(1L))
                .thenReturn(Optional.of(job));

        var result = matchingService.calculateMatching(1L, 1L);

        assertEquals(0.0, result.getScore());

        assertTrue(result.getMatchingSkills().isEmpty());

        assertEquals(3, result.getMissingSkills().size());
    }




    @Test
    void shouldThrowExceptionWhenJobDoesNotExist() {

        User candidate = User.builder()
                .id(1L)
                .firstName("Amine")
                .lastName("Khelifi")
                .skills("Java, Spring Boot")
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(jobRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> matchingService.calculateMatching(1L, 1L)
        );

        assertEquals(
                "Offre introuvable",
                exception.getMessage()
        );
    }
}