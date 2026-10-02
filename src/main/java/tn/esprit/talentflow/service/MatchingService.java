package tn.esprit.talentflow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.talentflow.exception.ResourceNotFoundException;
import tn.esprit.talentflow.dto.MatchingResponse;
import tn.esprit.talentflow.entity.Job;
import tn.esprit.talentflow.entity.User;
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

        List<String> requiredSkills =
                parseSkills(job.getRequiredSkills());

        List<String> candidateSkills =
                extractCandidateSkills(candidate);

        List<String> matchingSkills =
                requiredSkills.stream()
                        .filter(candidateSkills::contains)
                        .toList();

        List<String> missingSkills =
                requiredSkills.stream()
                        .filter(skill ->
                                !candidateSkills.contains(skill))
                        .toList();

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

    private List<String> extractCandidateSkills(User candidate) {

        /*
         * Pour l'instant, les compétences du candidat
         * sont récupérées depuis le champ "skills"
         * lorsqu'il existe dans User.
         */

        try {
            String skills = candidate.getSkills();

            if (skills == null || skills.isBlank()) {
                return List.of();
            }

            return Arrays.stream(skills.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(skill -> !skill.isBlank())
                    .distinct()
                    .toList();

        } catch (Exception e) {

            return List.of();
        }
    }
}
