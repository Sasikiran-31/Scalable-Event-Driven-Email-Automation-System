package org.example.kafkaapplication.PayLoad_DTO;

import lombok.Getter;
import org.example.kafkaapplication.EmailTemplates.SignupTemplate;
import org.example.kafkaapplication.EmailTemplates.EmailTemplate;
import org.example.kafkaapplication.EmailTemplates.OrderTemplate;
import org.example.kafkaapplication.EmailTemplates.TrialExpirationTemplate;

import java.util.function.Supplier;

/**
 * This contains the type of Event along with the template
 */
public enum EventType {
    SIGNUP("user.signup", () -> new SignupTemplate()),
    ORDER ("order.completed", () -> new OrderTemplate()),
    TRIAL ("trial.expiring", () -> new TrialExpirationTemplate());

    @Getter
    private final String topic;
    private final Supplier<EmailTemplate> emailTemplateSupplier;

    EventType(String topic, Supplier<EmailTemplate> emailTemplateSupplier) {
        this.topic = topic;
        this.emailTemplateSupplier = emailTemplateSupplier;
    }

    public EmailTemplate getEmailTemplate() {
        return emailTemplateSupplier.get();
    }
}
