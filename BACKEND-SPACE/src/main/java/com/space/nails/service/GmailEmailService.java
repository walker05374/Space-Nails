package com.space.nails.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class GmailEmailService {

    private static final Logger logger = LoggerFactory.getLogger(GmailEmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // --- MÉTODO NOVO GENÉRICO PARA CORRIGIR O ERRO DE COMPILAÇÃO ---
    public boolean sendEmail(String toEmail, String subject, String body) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(body, true); // true = HTML permitido
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setFrom(fromEmail);

            mailSender.send(mimeMessage);
            logger.info("E-mail genérico enviado para {} com sucesso!", toEmail);
            return true;
        } catch (MessagingException e) {
            logger.error("Falha ao enviar e-mail para {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    // Método específico existente
    public boolean sendPasswordResetEmail(String toEmail, String userName, String resetLink) {
        // Você pode reutilizar o método acima ou manter a lógica específica aqui
        String htmlContent = "Olá " + userName + ",<br><br>" +
                             "Você solicitou a redefinição de sua senha. " +
                             "Clique no link a seguir para redefinir:<br>" +
                             "<a href=\"" + resetLink + "\">Redefinir Senha</a><br><br>" +
                             "Se você não solicitou isso, por favor, ignore este e-mail.";
        return sendEmail(toEmail, "Redefina sua Senha", htmlContent);
    }
}