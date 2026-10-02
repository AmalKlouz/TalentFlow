package tn.esprit.talentflow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationRequest {

    @NotNull(message = "L'identifiant de l'offre est obligatoire")
    private Long jobId;

    private String coverLetter;

    @NotNull(message = "Le CV est obligatoire")
    private MultipartFile cv;
}