package com.github.tah10n.carrentalbot.db.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class MyUser {
    @Id
    private Long id;
    private String firstName;
    private String lastName;
    private String userName;
    private Boolean isAdmin;
    private Boolean isUnsubscribed;
    private String language;


    public void setLanguage(String language) {
        if (language == null || language.isEmpty() || (!language.equals("en") && !language.equals("ru") && !language.equals("sr"))) {
            this.language = "ru";
        } else {
            this.language = language;
        }
    }

    @Override
    public String toString() {
        return "id=" + id + ", " +
               "userName=" + userName +
               ", firstName=" + firstName +
               ", lastName=" + lastName +
               ", isAdmin=" + isAdmin;
    }
}
