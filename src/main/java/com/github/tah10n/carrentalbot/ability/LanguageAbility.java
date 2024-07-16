package com.github.tah10n.carrentalbot.ability;

import com.github.tah10n.carrentalbot.db.dao.MyUserDAO;
import com.github.tah10n.carrentalbot.db.entity.MyUser;
import com.github.tah10n.carrentalbot.keyboards.InlineKeyboardMaker;
import com.github.tah10n.carrentalbot.utils.MessagesUtil;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Locality;
import org.telegram.telegrambots.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.abilitybots.api.objects.ReplyFlow;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import static org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getChatId;

public class LanguageAbility implements AbilityExtension {
    private final AbilityBot abilityBot;
    private final InlineKeyboardMaker keyboardMaker;
    private final MyUserDAO myUserDAO;
    private final MessagesUtil messagesUtils;

    public LanguageAbility(AbilityBot abilityBot, InlineKeyboardMaker keyboardMaker, MyUserDAO myUserDAO, MessagesUtil messagesUtils) {
        this.abilityBot = abilityBot;
        this.keyboardMaker = keyboardMaker;
        this.myUserDAO = myUserDAO;
        this.messagesUtils = messagesUtils;
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
                    String text = messagesUtils.getMessage("select_language", lang);
                    SendMessage message = SendMessage.builder()
                            .chatId(ctx.chatId().toString())
                            .text(text)
                            .parseMode("HTML")
                            .replyMarkup(keyboardMaker.getLanguageKeyboard()).build();

                    abilityBot.getSilent().execute(message);
                }).build();
    }


    public ReplyFlow changeLanguage() {

        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    String callbackQueryData = upd.getCallbackQuery().getData();
                    String[] data = callbackQueryData.split("_");
                    String language = data[1];
                    MyUser myUser = myUserDAO.getById(upd.getCallbackQuery().getFrom().getId());
                    myUser.setLanguage(language);
                    myUserDAO.save(myUser);
                    String text = messagesUtils.getMessage("language_changed",language);
                    bot.getSilent().execute(DeleteMessage.builder().chatId(getChatId(upd)).messageId(upd.getCallbackQuery().getMessage().getMessageId()).build());
                    bot.getSilent().send(text, getChatId(upd));
                })
                .onlyIf(hasCallbackQueryWith("lang"))
                .build();


    }

    private Predicate<Update> hasCallbackQueryWith(String string) {
        return update -> update.hasCallbackQuery() &&
                         update.getCallbackQuery().getData().contains(string);
    }
}


