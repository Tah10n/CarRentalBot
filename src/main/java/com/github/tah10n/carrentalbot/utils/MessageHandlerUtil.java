package com.github.tah10n.carrentalbot.utils;

import com.github.tah10n.carrentalbot.db.dao.MyUserDAO;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessages;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
public class MessageHandlerUtil {

    private MessageHandlerUtil() {
    }

    public static void executeMessage(BaseAbilityBot bot, Long myUserId, MyUserDAO myUserDAO, BotApiMethod<Message> method) {

        Optional<Message> executed = bot.getSilent().execute(method);
        executed.ifPresent(message -> myUserDAO.addMessageToStack(myUserId, message.getMessageId()));
    }

    public static void clearMessagesStack(BaseAbilityBot bot, Long chatId, MyUserDAO myUserDAO) {
        if (myUserDAO.isMessageStackFilled(chatId)) {
            List<Integer> messageList = myUserDAO.popAllMessagesFromStack(chatId);
            DeleteMessages deleteMessages = DeleteMessages.builder()
                    .chatId(chatId)
                    .messageIds(messageList)
                    .build();

            bot.getSilent().execute(deleteMessages);
        }
    }

    public static Message editMessage(BaseAbilityBot bot, Update upd, Long chatId, Integer messageId, String text, InlineKeyboardMarkup keyboard) {
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
                return (Message) bot.getTelegramClient().execute(message);
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
               return (Message) bot.getTelegramClient().execute(message);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            }
        }
        return null;
    }
}
