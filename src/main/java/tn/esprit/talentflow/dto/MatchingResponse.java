package tn.esprit.talentflow.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingResponse {

    private Long candidateId;
    private String candidateName;

    private Long jobId;
    private String jobTitle;

    private List<String> requiredSkills;
    private List<String> matchingSkills;
    private List<String> missingSkills;

    private double score;
}
