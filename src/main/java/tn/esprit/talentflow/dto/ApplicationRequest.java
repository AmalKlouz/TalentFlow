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

    @NotNull
    private Long jobId;

    private String coverLetter;

    private MultipartFile cv;
}

