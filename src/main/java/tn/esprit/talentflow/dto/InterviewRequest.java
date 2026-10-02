package tn.esprit.talentflow.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.esprit.talentflow.entity.InterviewType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewRequest {

    @NotNull
    private Long applicationId;

    @NotNull
    @Future
    private LocalDateTime scheduledAt;

    @NotNull
    @Min(15)
    private Integer duration;

    @NotNull
    private InterviewType type;

    private String location;

    private String notes;
}