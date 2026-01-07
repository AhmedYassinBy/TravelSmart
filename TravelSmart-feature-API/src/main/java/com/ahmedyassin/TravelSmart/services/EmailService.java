package com.ahmedyassin.TravelSmart.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@travelsmart.com}")
    private String fromEmail;

    /**
     * Send a simple email
     */
    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send confirmation email for client registration
     */
    public void sendConfirmationEmail(String to, String confirmationLink) {
        String subject = "Confirmer votre compte TravelSmart";
        String text = "Bienvenue sur TravelSmart!\n\n" +
                "Cliquez sur le lien suivant pour activer votre compte:\n" +
                confirmationLink + "\n\n" +
                "Si vous n'avez pas créé de compte, ignorez cet email.\n\n" +
                "L'équipe TravelSmart";
        sendEmail(to, subject, text);
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String to, String resetLink) {
        String subject = "Réinitialiser votre mot de passe TravelSmart";
        String text = "Vous avez demandé une réinitialisation de mot de passe.\n\n" +
                "Cliquez sur le lien suivant pour réinitialiser votre mot de passe:\n" +
                resetLink + "\n\n" +
                "Ce lien expire dans 1 heure.\n\n" +
                "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n\n" +
                "L'équipe TravelSmart";
        sendEmail(to, subject, text);
    }

    /**
     * Send booking confirmation email
     */
    public void sendBookingConfirmation(String to, String offerName, String bookingId, Double price) {
        String subject = "Confirmation de réservation - " + offerName;
        String text = "Votre réservation a été confirmée!\n\n" +
                "Détails de la réservation:\n" +
                "- Offre: " + offerName + "\n" +
                "- Référence: " + bookingId + "\n" +
                "- Prix total: " + price + " TND\n\n" +
                "Merci de votre confiance!\n\n" +
                "L'équipe TravelSmart";
        sendEmail(to, subject, text);
    }
}
