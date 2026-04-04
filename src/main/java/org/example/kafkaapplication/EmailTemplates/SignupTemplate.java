package org.example.kafkaapplication.EmailTemplates;

/**
 * A simple email-template for Account creation
 */
public class SignupTemplate implements EmailTemplate {

    @Override
    public String sendEmail(String username) {
        return "Hello Subscriber, Welcome to our service!";
    }
}
