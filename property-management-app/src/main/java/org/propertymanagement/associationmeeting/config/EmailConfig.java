package org.propertymanagement.associationmeeting.config;

import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import org.propertymanagement.notification.EmailNotificationSender;
import org.propertymanagement.notification.email.EmailDecoratorNotificationSender;
import org.propertymanagement.notification.email.EmailStubNotificationSender;
import org.propertymanagement.notification.email.JavaEmailNotificationSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;


@Slf4j
public class EmailConfig {
    @Autowired
    private Environment environment;

    public final static String EMAIL_TEMPLATE = "Dear {recipientName},\n" +
            "We are delighted to invite you to our next community association meeting.\n\n" +
            "Details:\n\n" +
            " Date: {date}\n" +
            " Time: {time}\n\n" +
            "More details: {description}\n\n\n" +
            "The Administration is looking forward to meet you soon.\n\n" +
            "Kind regards,\nThe Administration.";


    @Bean
    public SimpleMailMessage templateMessage(
            @Value("${email.from:default_noreply@propertymanagement.com}") String emailFrom,
            @Value("${email.subject:Default subject}") String emailSubject) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailFrom);
        message.setSubject(emailSubject);
        message.setText(EMAIL_TEMPLATE);
        return message;
    }

    @ConditionalOnProperty(name = { "email.retries", "notification.retry" }, havingValue = "on")
    @Bean
    @Primary
    public EmailNotificationSender emailRetrySender(EmailNotificationSender emailNotificationSender, Retry retryNotification) {
        log.info("Creating emailRetrySender");
        return new EmailDecoratorNotificationSender(emailNotificationSender, retryNotification);
    }

    @Bean
    public EmailNotificationSender emailNotificationSender(JavaMailSender emailSender, SimpleMailMessage emailTemplate) {
        String emailIntegration = environment.getProperty("email.integration", "off");
        log.info("Email integration is {}", emailIntegration);
        if ("on".equalsIgnoreCase(emailIntegration)) {
            return new JavaEmailNotificationSender(emailSender, emailTemplate);
        } else {
            return new EmailStubNotificationSender();
        }
    }
}
