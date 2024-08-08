package com.github.tah10n.carrentalbot.utils;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;

@Slf4j
public class MessageHandlerUtil {

    public static void editMessage(BaseAbilityBot bot, Update upd, Long chatId, Integer messageId, String text, InlineKeyboardMarkup keyboard) {
        Message incomingMessage = null;
        if(upd.hasCallbackQuery()) {
            incomingMessage = (Message) upd.getCallbackQuery().getMessage();
        } else if(upd.hasMessage()) {
            incomingMessage =  upd.getMessage();
        }


        if (incomingMessage != null && incomingMessage.hasCaption()) {

            EditMessageCaption message = EditMessageCaption.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .caption(text)
                    .replyMarkup(keyboard)
                    .build();
            try {
                bot.getTelegramClient().execute(message);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            }
        } else {
            EditMessageText message = EditMessageText.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .text(text)
                    .replyMarkup(keyboard)
                    .build();

            try {
                bot.getTelegramClient().execute(message);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            }
        }
    }
}
