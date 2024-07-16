package com.github.tah10n.carrentalbot.utils;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class MessagesUtil {
    public String getMessage(String key, String lang) {
        ResourceBundle messages = ResourceBundle.getBundle("messages", new Locale(lang));

        if (!messages.containsKey(key)) {
            return key;
        }

        return messages.getString(key);
    }
}
