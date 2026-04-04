package org.example.kafkaapplication.EmailTemplates;


/**
 * This interface serves as the template of emails
 */
public interface EmailTemplate {
    String sendEmail(String username);
}
