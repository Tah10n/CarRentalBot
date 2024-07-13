package com.github.tah10n.carrentalbot.keyboards;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class InlineKeyboardMaker {

    public InlineKeyboardMarkup getStartKeyboard(String lang) {
        ResourceBundle buttons = ResourceBundle.getBundle("buttons", new Locale(lang));
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text(buttons.getString("car_list")).callbackData("carlist").build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text(buttons.getString("help")).callbackData("help").build(),
                        InlineKeyboardButton.builder().text(buttons.getString("rules")).callbackData("rules").build(),
                        InlineKeyboardButton.builder().text(buttons.getString("contacts")).callbackData("contacts").build()
                )).build();
    }

    public InlineKeyboardMarkup getLanguageKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("RU").callbackData("lang_ru").build(),
                        InlineKeyboardButton.builder().text("EN").callbackData("lang_en").build(),
                        InlineKeyboardButton.builder().text("SR").callbackData("lang_sr").build()
                )).build();
    }
}
