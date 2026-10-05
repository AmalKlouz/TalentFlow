
package tn.esprit.talentflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {

    @Email(message = "L'adresse email est invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotBlank(message = "Le code de réinitialisation est obligatoire")
    private String code;

    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    private String newPassword;
}

