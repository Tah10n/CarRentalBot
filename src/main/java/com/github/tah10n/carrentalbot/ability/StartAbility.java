package com.github.tah10n.carrentalbot.ability;

import com.github.tah10n.carrentalbot.db.dao.MyUserDAO;
import com.github.tah10n.carrentalbot.db.entity.MyUser;
import com.github.tah10n.carrentalbot.keyboards.InlineKeyboardMaker;
import com.github.tah10n.carrentalbot.utils.MessagesUtil;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Locality;
import org.telegram.telegrambots.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.abilitybots.api.objects.ReplyFlow;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Predicate;

@Slf4j
public class StartAbility implements AbilityExtension {
    private final AbilityBot abilityBot;
    private final InlineKeyboardMaker keyboardMaker;
    private final MyUserDAO myUserDAO;
    private final MessagesUtil messagesUtil;

    public StartAbility(AbilityBot abilityBot, InlineKeyboardMaker keyboardMaker, MyUserDAO myUserDAO, MessagesUtil messagesUtil) {
        this.abilityBot = abilityBot;
        this.keyboardMaker = keyboardMaker;
        this.myUserDAO = myUserDAO;
        this.messagesUtil = messagesUtil;
    }

    public Ability startCommand() {
        return Ability
                .builder()
                .name("start")
                .info("starts the bot")
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    User user = ctx.user();
                    String lang;
                    MyUser myUser;
                    if (!myUserDAO.existsById(user.getId())) {
                        myUser = new MyUser(user.getId(), user.getFirstName(), user.getLastName(), user.getUserName(),
                                false
                                , false, user.getLanguageCode());
                        myUserDAO.save(myUser);

                    } else {
                        myUser = myUserDAO.getById(user.getId());
                    }
                    lang = myUser.getLanguage();

                    String greeting = messagesUtil.getMessage("greeting", lang);
                    abilityBot.getSilent().send(greeting, ctx.chatId());
                    String text = messagesUtil.getMessage("start", lang);
                    SendMessage message = SendMessage.builder()
                            .chatId(ctx.chatId().toString())
                            .text(text)
                            .replyMarkup(keyboardMaker.getStartKeyboard(lang)).build();
                    abilityBot.getSilent().execute(message);

                }).build();
    }

    public ReplyFlow helpButton() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> simpleButtonAction(upd, "help", bot))
                .onlyIf(hasCallbackQueryWith("help"))
                .build();
    }

    public ReplyFlow rulesButton() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> simpleButtonAction(upd, "rules", bot))
                .onlyIf(hasCallbackQueryWith("rules"))
                .build();
    }

    public ReplyFlow contactsButton() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> simpleButtonAction(upd, "contacts", bot))
                .onlyIf(hasCallbackQueryWith("contacts"))
                .build();
    }

    private void simpleButtonAction(Update upd, String actionName, BaseAbilityBot bot) {
        MyUser myUser = myUserDAO.getById(upd.getCallbackQuery().getFrom().getId());
        String lang = myUser.getLanguage();
        String text = messagesUtil.getMessage(actionName, lang);
        EditMessageText message = EditMessageText.builder()
                .chatId(upd.getCallbackQuery().getFrom().getId().toString())
                .messageId(upd.getCallbackQuery().getMessage().getMessageId())
                .text(text)
                .replyMarkup(keyboardMaker.getBackKeyboard(lang))
                .build();
        bot.getSilent().execute(message);
    }

    public ReplyFlow backButton() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    MyUser myUser = myUserDAO.getById(upd.getCallbackQuery().getFrom().getId());
                    String lang = myUser.getLanguage();
                    String text = messagesUtil.getMessage("start", lang);
                    EditMessageText message = EditMessageText.builder()
                            .chatId(upd.getCallbackQuery().getFrom().getId().toString())
                            .messageId(upd.getCallbackQuery().getMessage().getMessageId())
                            .text(text)
                            .replyMarkup(keyboardMaker.getStartKeyboard(lang)).build();
                    bot.getSilent().execute(message);
                })
                .onlyIf(hasCallbackQueryWith("back"))
                .build();
    }

    private Predicate<Update> hasCallbackQueryWith(String string) {
        return update -> update.hasCallbackQuery() &&
                         update.getCallbackQuery().getData().contains(string);
    }

}
