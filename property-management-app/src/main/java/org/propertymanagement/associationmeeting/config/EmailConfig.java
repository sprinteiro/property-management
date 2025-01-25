package org.propertymanagement.associationmeeting.config;

import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import org.propertymanagement.notification.EmailNotificationSender;
import org.propertymanagement.notification.email.EmailDecoratorNotificationSender;
import org.propertymanagement.notification.email.EmailStubNotificationSender;
import org.propertymanagement.notification.email.JavaEmailNotificationSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.mail.MailHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;


@Slf4j
public class EmailConfig {
    public static final String EMAIL_INTEGRATION_PROPERTY = "email.integration";

    public final static String EMAIL_TEMPLATE = """
            Dear {recipientName},
            We are delighted to invite you to our next community association meeting.
            
            Details:
            
             Date: {date}
             Time: {time}
            
            More details: {description}
            
            
            The Administration is looking forward to meet you soon.
            
            Kind regards,
            The Administration.""";


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

    @ConditionalOnProperty(name = {"email.retries", "notification.retry"}, havingValue = "on")
    @Bean
    @Primary
    public EmailNotificationSender emailRetrySender(EmailNotificationSender emailNotificationSender, Retry retryNotification) {
        log.info("Creating emailRetrySender");
        return new EmailDecoratorNotificationSender(emailNotificationSender, retryNotification);
    }

    @Bean
    public EmailNotificationSender emailNotificationSender(
            JavaMailSender emailSender,
            SimpleMailMessage emailTemplate,
            @Autowired Environment environment) {
        String emailIntegration = environment.getProperty(EMAIL_INTEGRATION_PROPERTY, "off");
        log.info("Email integration is {}", emailIntegration);
        if ("on".equalsIgnoreCase(emailIntegration)) {
            return new JavaEmailNotificationSender(emailSender, emailTemplate);
        } else {
            return new EmailStubNotificationSender();
        }
    }

    @ConditionalOnProperty(name = EMAIL_INTEGRATION_PROPERTY, havingValue = "off")
    @Bean
    public MailHealthIndicator mailHealthIndicator(JavaMailSenderImpl mailSender) {
        log.info("Custom mail health indicator as email integration is disabled.");
        return new CustomMailHealthIndicator(mailSender);
    }

    static class CustomMailHealthIndicator extends MailHealthIndicator {
        public CustomMailHealthIndicator(JavaMailSenderImpl mailSender) {
            super(mailSender);
        }

        @Override
        protected void doHealthCheck(Health.Builder builder) {
            // Skip mail health check as service is being stubbed. Prevent connection.
        }
    }
}
