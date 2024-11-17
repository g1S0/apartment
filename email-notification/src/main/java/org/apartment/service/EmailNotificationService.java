package org.apartment.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apartment.dto.EmailDetailsDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@Slf4j
public class EmailNotificationService {

  @Value("${spring.mail.username}")
  private String username;

  private final JavaMailSender mailSender;
  private final SpringTemplateEngine templateEngine;

  public EmailNotificationService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
    this.mailSender = mailSender;
    this.templateEngine = templateEngine;
  }

  @KafkaListener(topics = "email_topic", groupId = "email_group")
  public void handleEmailNotification(String toEmail) {
    String subject = "TApartment";
    String messageBody = buildRegistrationSuccessEmailBody();

    EmailDetailsDto emailDetails = new EmailDetailsDto(toEmail, subject, messageBody);
    sendEmail(emailDetails);
  }

  private void sendEmail(EmailDetailsDto emailDetails) {
    MimeMessage message = mailSender.createMimeMessage();

    try {
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(username);
      helper.setTo(emailDetails.getToEmail());
      helper.setSubject(emailDetails.getSubject());
      helper.setText(emailDetails.getMessageBody(), true);

      mailSender.send(message);
      log.info("Email sent to: {}", emailDetails.getToEmail());
    } catch (Exception e) {
      log.error("Error sending email to {}: {}", emailDetails.getToEmail(), e.getMessage());
    }
  }

  private String buildRegistrationSuccessEmailBody() {
    Context context = new Context();
    return templateEngine.process("registrationSuccessEmail", context);
  }
}