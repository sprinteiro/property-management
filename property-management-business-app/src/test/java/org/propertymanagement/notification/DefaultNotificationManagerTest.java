package org.propertymanagement.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.propertymanagement.TestCorrelationIdLogUtils;
import org.propertymanagement.associationmeeting.notification.FailedNotification;
import org.propertymanagement.domain.*;
import org.propertymanagement.domain.notification.Meeting;
import org.propertymanagement.domain.notification.NotificationDelivery;
import org.propertymanagement.domain.notification.NotificationRequest;
import org.propertymanagement.domain.notification.Recipient;

import java.util.concurrent.Executor;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.*;
import static org.propertymanagement.domain.notification.NotificationRequest.NotificationChannel.EMAIL;
import static org.propertymanagement.domain.notification.NotificationRequest.NotificationChannel.SMS;

public class DefaultNotificationManagerTest {
    private static final CommunityId COMMUNITY_ID = new CommunityId(1L);
    private static final NeighbourgId PRESIDENT_ID = new NeighbourgId(2L);
    private static final MeetingDate MEETING_DATE = new MeetingDate("01/12/2024");
    private static final MeetingTime MEETING_TIME = new MeetingTime("19:00");
    public static final String CORRELATION_ID = "correlationId";

    private EmailNotificationSender emailSender = mock(EmailNotificationSender.class);
    private SmsNotificationSender smsSender = mock(SmsNotificationSender.class);
    // Direct executor for simplicity, ensuring tasks execute immediately:
    private Executor executor = Runnable::run;
    private TestCorrelationIdLogUtils correlationIdLog = new TestCorrelationIdLogUtils();
    private FailedNotification failedNotificationManager = mock(FailedNotification.class);


    @BeforeEach
    @Test
    void setup() {
        emailSender = mock(EmailNotificationSender.class);
        smsSender = mock(SmsNotificationSender.class);
        executor = Runnable::run;
        correlationIdLog = new TestCorrelationIdLogUtils();
        failedNotificationManager = mock(FailedNotification.class);
    }

    @Test
    void sendNotificationViaSms() {
        NotificationDelivery<Meeting> notification = newNotification(SMS);
        when(smsSender.sendNotification(notification)).thenReturn(true);

        NotificationManager notificationManager = new DefaultNotificationManager(emailSender, smsSender, executor, correlationIdLog, failedNotificationManager);
        notificationManager.sendNotification(notification);

        verify(smsSender).sendNotification(notification);
        verifyNoInteractions(emailSender);
    }

    @Test
    void sendNotificationViaEmail() {
        NotificationDelivery<Meeting> notification = newNotification(EMAIL);
        when(smsSender.sendNotification(notification)).thenReturn(true);

        NotificationManager notificationManager = new DefaultNotificationManager(emailSender, smsSender, executor, correlationIdLog, failedNotificationManager);
        notificationManager.sendNotification(notification);

        verifyNoInteractions(smsSender);
        verify(emailSender).sendNotification(notification);
    }

    private NotificationDelivery<Meeting> newNotification(NotificationRequest.NotificationChannel channel) {
        var phoneNumber = new PhoneNumber("+1111111111");
        Recipient recipient = new Recipient(PRESIDENT_ID, channel, phoneNumber, new Name("test field"));
        var details = new Meeting(MEETING_DATE, MEETING_TIME, new MeetingSubject("Meeting subject", "Description"), CORRELATION_ID.getBytes(UTF_8));
        return new NotificationDelivery<>(
                NotificationDelivery.NotificationType.MEETING,
                CORRELATION_ID.getBytes(UTF_8),
                COMMUNITY_ID,
                recipient,
                details);
    }
}
