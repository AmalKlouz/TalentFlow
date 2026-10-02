        package tn.esprit.talentflow.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String location;

    @NotBlank
    private String contractType;

    @NotBlank
    private String experienceLevel;

    @NotBlank
    private String requiredSkills;

    private LocalDate deadline;
}
