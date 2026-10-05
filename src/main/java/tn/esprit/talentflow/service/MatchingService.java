        package tn.esprit.talentflow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.talentflow.dto.MatchingResponse;
import tn.esprit.talentflow.entity.Job;
import tn.esprit.talentflow.entity.User;
import tn.esprit.talentflow.exception.ResourceNotFoundException;
import tn.esprit.talentflow.repository.JobRepository;
import tn.esprit.talentflow.repository.UserRepository;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public MatchingResponse calculateMatching(
            Long candidateId,
            Long jobId) {

        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Candidat introuvable"
                        ));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Offre introuvable"
                        ));

        // Compétences demandées par l'offre
        List<String> requiredSkills =
                parseSkills(job.getRequiredSkills());

        // Compétences du candidat
        List<String> candidateSkills =
                extractCandidateSkills(candidate);

        // Compétences communes
        List<String> matchingSkills =
                requiredSkills.stream()
                        .filter(candidateSkills::contains)
                        .toList();

        // Compétences manquantes
        List<String> missingSkills =
                requiredSkills.stream()
                        .filter(skill ->
                                !candidateSkills.contains(skill))
                        .toList();

        // Calcul du score
        double score = 0;

        if (!requiredSkills.isEmpty()) {
            score =
                    ((double) matchingSkills.size()
                            / requiredSkills.size())
                            * 100;
        }

        return MatchingResponse.builder()
                .candidateId(candidate.getId())
                .candidateName(
                        candidate.getFirstName()
                                + " "
                                + candidate.getLastName()
                )
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .requiredSkills(requiredSkills)
                .matchingSkills(matchingSkills)
                .missingSkills(missingSkills)
                .score(Math.round(score * 100.0) / 100.0)
                .build();
    }

    // =========================================================
    // PARSE JOB SKILLS
    // =========================================================

    private List<String> parseSkills(String skills) {

        if (skills == null || skills.isBlank()) {
            return List.of();
        }

        return Arrays.stream(skills.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(skill -> !skill.isBlank())
                .distinct()
                .toList();
    }

    // =========================================================
    // CANDIDATE SKILLS
    // =========================================================

    private List<String> extractCandidateSkills(User candidate) {

        if (candidate.getSkills() == null
                || candidate.getSkills().isEmpty()) {

            return List.of();
        }

        return candidate.getSkills()
                .stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(skill -> !skill.isBlank())
                .distinct()
                .toList();
    }
}
