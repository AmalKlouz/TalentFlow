        package tn.esprit.talentflow.dto;

import lombok.*;
import tn.esprit.talentflow.entity.ApplicationStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationResponse {

    private Long id;

    private Long candidateId;
    private String candidateName;
    private String candidateEmail;

    private Long jobId;
    private String jobTitle;

    private ApplicationStatus status;

    private LocalDateTime appliedAt;

    private String coverLetter;

    private String cvPath;
}
