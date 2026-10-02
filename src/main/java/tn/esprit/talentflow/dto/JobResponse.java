
package tn.esprit.talentflow.dto;

import lombok.*;

import tn.esprit.talentflow.entity.JobStatus;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobResponse {

    private Long id;

    private String title;

    private String description;

    private String location;

    private String contractType;

    private String experienceLevel;

    private String requiredSkills;

    private LocalDate publicationDate;

    private LocalDate deadline;

    private JobStatus status;

    private Long createdById;

    private String createdByName;
}

