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

    @NotNull(message = "L'identifiant de la candidature est obligatoire")
    private Long applicationId;

    @NotNull(message = "La date de l'entretien est obligatoire")
    @Future(message = "La date de l'entretien doit être dans le futur")
    private LocalDateTime scheduledAt;

    @NotNull(message = "La durée est obligatoire")
    @Min(value = 15, message = "La durée minimale est de 15 minutes")
    private Integer duration;

    @NotNull(message = "Le type d'entretien est obligatoire")
    private InterviewType type;

    private String location;

    private String notes;
}