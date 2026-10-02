package tn.esprit.talentflow.dto;

import lombok.*;
import tn.esprit.talentflow.entity.InterviewStatus;
import tn.esprit.talentflow.entity.InterviewType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewResponse {

    private Long id;

    private Long applicationId;

    private Long candidateId;
    private String candidateName;
    private String candidateEmail;

    private Long jobId;
    private String jobTitle;

    private LocalDateTime scheduledAt;

    private Integer duration;

    private InterviewType type;

    private String location;

    private String notes;

    private InterviewStatus status;
}