package com.github.tah10n.carrentalbot.utils;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.ResourceBundle;


public class ButtonsUtil {
    public static String getButton(String key, String lang) {
        ResourceBundle buttons = ResourceBundle.getBundle("buttons", new Locale(lang));
        if (!buttons.containsKey(key)) {
            return key;
        }
        return buttons.getString(key);
    }
}
