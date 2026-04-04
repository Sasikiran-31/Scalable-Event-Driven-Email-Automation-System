package org.example.kafkaapplication.EmailTemplates;

/**
 * A simple email-template for order confirmation
 */
public class OrderTemplate implements EmailTemplate {

    @Override
    public String sendEmail(String username) {
        return "Hello Subscriber, Your order has been received and placed successfully!";
    }

}
