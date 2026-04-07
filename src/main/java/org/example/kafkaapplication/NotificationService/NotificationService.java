package org.example.kafkaapplication.NotificationService;

import org.example.kafkaapplication.Model.User;
import org.example.kafkaapplication.PayLoad_DTO.Event;
import org.springframework.stereotype.Service;


/**
 * This serves as the actual notifier that Makes the SMTP API call
 */
@Service
public class NotificationService {

    public void sendEmail(Event event){
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (System.getProperty("test.mode") == null && Math.random() < 0.30) {
            throw new RuntimeException("Simulated SMTP Network Timeout");
        }

        User user = event.payload().values().iterator().next();
        String content = event.event_type().getEmailTemplate().sendEmail(user.getUserName());
//        System.out.println("Sending email to " + user.getUserName() + " with content " + content);
        // Notification Call

        /// Scheduler (call kafka)-> kafka -> SendEmail
    }
}
