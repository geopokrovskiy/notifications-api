package com.geopokrovskiy.service;

import com.geopokrovskiy.config.MailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.angus.mail.smtp.SMTPTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
@Slf4j
@Data
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    private final MailProperties mailProperties;

    public int sendEmail(String subject, String destination, String message) {
        int code;
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", mailProperties.getHost());
            props.put("mail.smtp.port", mailProperties.getPort());

            Session session = Session.getDefaultInstance(props);
            log.info("Sending email to " + destination);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

            helper.setTo(destination);
            helper.setSubject(subject);
            helper.setText(message, true);

            try (SMTPTransport transport = (SMTPTransport) session.getTransport("smtp")) {
                transport.connect();
                transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
                code = transport.getLastReturnCode();
            }

        } catch (MessagingException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return code;
    }
}
