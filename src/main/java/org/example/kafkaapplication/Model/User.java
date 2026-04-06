package org.example.kafkaapplication.Model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Model of the User
 */
@Getter
@Setter
@NoArgsConstructor
public class User {

    private String emailId;
    private String userName;
    private CustomerType customerType;
    private String userId;

    public User(String emailId, String userName,  CustomerType customerType) {
        this.userId = UUID.randomUUID().toString();
        this.emailId = emailId;
        this.userName = userName;
        this.customerType = customerType;
    }

    @Override
    public String toString() {
        return "User{" +
                "emailId='" + emailId + '\'' +
                ", userName='" + userName + '\'' +
                ", customerType=" + customerType +
                '}';
    }
}
