package org.propertymanagement.associationmeeting.config;

import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import org.propertymanagement.notification.SmsNotificationSender;
import org.propertymanagement.notification.sms.SmsDecoratorNotificationSender;
import org.propertymanagement.notification.sms.SmsStubNotificationSender;
import org.propertymanagement.notification.sms.SmsTwilioNotificationSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;


@Slf4j
public class SmsConfig {
    @Autowired
    private Environment environment;

    @ConditionalOnProperty(name = { "sms.retries", "notification.retry" }, havingValue = "on")
    @Bean
    @Primary
    public SmsNotificationSender smsRetrySender(SmsNotificationSender smsNotificationSender, Retry retryNotification) {
        return new SmsDecoratorNotificationSender(smsNotificationSender, retryNotification);
    }

    @Bean
    public SmsNotificationSender smsNotificationSender() {
        String twilioIntegration = environment.getProperty("sms.twilio.integration", "off");
        log.info("Twilio integration is {}", twilioIntegration);
        if ("on".equalsIgnoreCase(twilioIntegration)) {
            String accountId = environment.getProperty("twilio.accountid", "default_account_id");
            String authToken = environment.getProperty("twilio.authtoken", "default_token_auth");
            return new SmsTwilioNotificationSender(
                    accountId,
                    authToken
            );
        } else {
            return new SmsStubNotificationSender();
        }
    }
}
