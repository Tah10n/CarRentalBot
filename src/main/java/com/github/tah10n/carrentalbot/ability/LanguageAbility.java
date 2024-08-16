package com.github.tah10n.carrentalbot.ability;

import com.github.tah10n.carrentalbot.db.dao.MyUserDAO;
import com.github.tah10n.carrentalbot.db.entity.MyUser;
import com.github.tah10n.carrentalbot.keyboards.InlineKeyboardMaker;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Locality;
import org.telegram.telegrambots.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.abilitybots.api.objects.ReplyFlow;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;
import java.util.function.Predicate;

import static com.github.tah10n.carrentalbot.utils.MessageHandlerUtil.clearMessagesStack;
import static com.github.tah10n.carrentalbot.utils.MessageHandlerUtil.executeMessage;
import static com.github.tah10n.carrentalbot.utils.MessagesUtil.getMessage;
import static org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getChatId;

@Component
public class LanguageAbility implements AbilityExtension {
    private final BaseAbilityBot abilityBot;
    private final InlineKeyboardMaker keyboardMaker;
    private final MyUserDAO myUserDAO;

    public LanguageAbility(BaseAbilityBot abilityBot, InlineKeyboardMaker keyboardMaker, MyUserDAO myUserDAO) {
        this.abilityBot = abilityBot;
        this.keyboardMaker = keyboardMaker;
        this.myUserDAO = myUserDAO;
    }

    public Ability languageCommand() {
        return Ability
                .builder()
                .name("language")
                .info("change language")
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    MyUser myUser = myUserDAO.getById(ctx.user().getId());
                    String lang = myUser.getLanguage();
                    String text = getMessage("select_language", lang);
                    SendMessage sendMessage = SendMessage.builder()
                            .chatId(ctx.chatId().toString())
                            .text(text)
                            .parseMode("HTML")
                            .replyMarkup(keyboardMaker.getLanguageKeyboard()).build();

                    executeMessage(abilityBot, myUser.getId(), myUserDAO, sendMessage);
                }).build();
    }


    public ReplyFlow changeLanguage() {

        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    String callbackQueryData = upd.getCallbackQuery().getData();
                    String[] data = callbackQueryData.split("_");
                    String language = data[1];
                    Long chatId = getChatId(upd);
                    Long myUserId = upd.getCallbackQuery().getFrom().getId();
                    MyUser myUser = myUserDAO.getById(myUserId);
                    myUser.setLanguage(language);
                    myUserDAO.save(myUser);

                    clearMessagesStack(bot, chatId, myUserDAO);

                    String text = getMessage("language_changed",language);
                    SendMessage sendMessage = SendMessage.builder()
                            .chatId(chatId.toString())
                            .text(text)
                                    .build();

                    bot.getSilent().execute(DeleteMessage.builder().chatId(chatId).messageId(upd.getCallbackQuery().getMessage().getMessageId()).build());

                    bot.getSilent().execute(sendMessage);

                })
                .onlyIf(hasCallbackQueryWith("lang"))
                .build();


    }

    private Predicate<Update> hasCallbackQueryWith(String string) {
        return update -> update.hasCallbackQuery() &&
                         update.getCallbackQuery().getData().contains(string);
    }
}


