package org.apartment.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailNotificationService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String username;

  public EmailNotificationService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  @KafkaListener(topics = "email_topic", groupId = "email_group")
  public void sendRegistrationSuccessEmail(String toEmail) {
    MimeMessage message = mailSender.createMimeMessage();

    try {
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(username);
      helper.setTo(toEmail);
      helper.setSubject("TApartment");

      String htmlMsg = "<div style='text-align: center;'>" + "<h2>Congratulations!</h2>"
          +
          "<p>Your registration was successful.</p>" + "</div>";

      helper.setText(htmlMsg, true);

      mailSender.send(message);
      log.info("Registration success email sent to: {}", toEmail);
    } catch (Exception e) {
      log.error("Error sending email to {}: {}", toEmail, e.getMessage());
    }
  }
}