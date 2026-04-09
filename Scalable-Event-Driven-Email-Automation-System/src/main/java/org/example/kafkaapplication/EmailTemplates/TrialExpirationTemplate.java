package org.example.kafkaapplication.EmailTemplates;

/**
 * A simple email-template for alerting customers of their trial expiration
 */
public class TrialExpirationTemplate implements EmailTemplate {
    @Override
    public String sendEmail(String username) {
        return "Dear User, Your trial is expiring soon....";
    }
}
