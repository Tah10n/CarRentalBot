package com.github.tah10n.carrentalbot.keyboards;

import com.github.tah10n.carrentalbot.utils.ButtonsUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class InlineKeyboardMaker {
    private final ButtonsUtil buttonsUtil;

    public InlineKeyboardMaker(ButtonsUtil buttonsUtil) {
        this.buttonsUtil = buttonsUtil;
    }

    public InlineKeyboardMarkup getStartKeyboard(String lang) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text(buttonsUtil.getButton("list_of_cars", lang)).callbackData("list_of_cars").build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text(buttonsUtil.getButton("help", lang)).callbackData("help").build(),
                        InlineKeyboardButton.builder().text(buttonsUtil.getButton("rules", lang)).callbackData("rules").build(),
                        InlineKeyboardButton.builder().text(buttonsUtil.getButton("contacts", lang)).callbackData("contacts").build()
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

    public InlineKeyboardMarkup getBackKeyboard(String lang) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text(buttonsUtil.getButton("back", lang)).callbackData("back").build()
                )).build();
    }

    public InlineKeyboardMarkup getCarKeyboard(String carId, String lang) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text(buttonsUtil.getButton("choose_car", lang)).callbackData("choose_car:" + carId).build()
                )).build();
    }

    public InlineKeyboardMarkup getAddCarKeyboard(String language) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text(buttonsUtil.getButton("add_car", language)).callbackData("add_car").build()
                )).build();
    }
}
