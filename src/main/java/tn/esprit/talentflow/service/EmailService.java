package tn.esprit.talentflow.service;

import lombok.RequiredArgsConstructor;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendResetCode(
            String email,
            String code) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(email);

        message.setSubject(
                "TalentFlow - Réinitialisation du mot de passe"
        );

        message.setText(
                "Bonjour,\n\n"
                        + "Vous avez demandé la réinitialisation "
                        + "de votre mot de passe TalentFlow.\n\n"
                        + "Votre code de réinitialisation est : "
                        + code
                        + "\n\n"
                        + "Ce code est valable pendant 10 minutes.\n\n"
                        + "Si vous n'êtes pas à l'origine de cette demande, "
                        + "vous pouvez ignorer cet email.\n\n"
                        + "L'équipe TalentFlow"
        );

        mailSender.send(message);
    }
}
