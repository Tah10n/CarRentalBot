package com.github.tah10n.carrentalbot.ability;

import com.github.tah10n.carrentalbot.db.dao.MyUserDAO;
import com.github.tah10n.carrentalbot.db.entity.MyUser;
import com.github.tah10n.carrentalbot.keyboards.InlineKeyboardMaker;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Locality;
import org.telegram.telegrambots.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Locale;
import java.util.ResourceBundle;

public class StartAbility implements AbilityExtension {
    private final AbilityBot bot;
    private final InlineKeyboardMaker keyboardMaker;
    private final MyUserDAO myUserDAO;

    public StartAbility(AbilityBot bot, InlineKeyboardMaker keyboardMaker, MyUserDAO myUserDAO) {
        this.bot = bot;
        this.keyboardMaker = keyboardMaker;
        this.myUserDAO = myUserDAO;
    }

    public Ability start() {
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
                                false, false, user.getLanguageCode());
                        myUserDAO.save(myUser);

                    } else {
                        myUser = myUserDAO.getById(user.getId());
                    }
                    lang = myUser.getLanguage();

                    ResourceBundle messages = ResourceBundle.getBundle("messages", new Locale(lang));
                    String greeting = messages.getString("greeting");
                    bot.getSilent().send(greeting, ctx.chatId());
                    String text = messages.getString("start");
                    SendMessage message = SendMessage.builder()
                            .chatId(ctx.chatId().toString())
                            .text(text)
                            .replyMarkup(keyboardMaker.getStartKeyboard(lang)).build();
                    bot.getSilent().execute(message);

                }).build();
    }

}
