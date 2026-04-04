package org.example.kafkaapplication.Model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Model of the User
 */
@Getter
@Setter
@NoArgsConstructor
public class User {
    private static long userIdAutoIncr;

    private long userId;
    private String emailId;
    private String userName;
    private CustomerType customerType;

    public User(String emailId, String userName,  CustomerType customerType) {
        userIdAutoIncr++;
        this.userId = userIdAutoIncr;
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
