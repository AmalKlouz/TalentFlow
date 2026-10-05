
package tn.esprit.talentflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPasswordRequest {

    @Email(message = "L'adresse email est invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;
}
