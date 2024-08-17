package com.github.tah10n.carrentalbot.ability;

import com.github.tah10n.carrentalbot.db.dao.MyUserDAO;
import com.github.tah10n.carrentalbot.db.entity.MyUser;
import com.github.tah10n.carrentalbot.keyboards.InlineKeyboardMaker;
import com.github.tah10n.carrentalbot.service.CarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Locality;
import org.telegram.telegrambots.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.abilitybots.api.objects.ReplyFlow;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Stack;
import java.util.function.Predicate;

import static com.github.tah10n.carrentalbot.utils.MessageHandlerUtil.editMessage;
import static com.github.tah10n.carrentalbot.utils.MessageHandlerUtil.executeMessage;
import static com.github.tah10n.carrentalbot.utils.MessagesUtil.getMessage;
import static org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getUser;

@Slf4j
@Component
public class StartAbility implements AbilityExtension {
    private final BaseAbilityBot abilityBot;
    private final InlineKeyboardMaker keyboardMaker;
    private final MyUserDAO myUserDAO;
    private final CarService carService;

    public StartAbility(BaseAbilityBot abilityBot, InlineKeyboardMaker keyboardMaker, MyUserDAO myUserDAO, CarService carService) {
        this.abilityBot = abilityBot;
        this.keyboardMaker = keyboardMaker;
        this.myUserDAO = myUserDAO;
        this.carService = carService;
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
                                , false, false, user.getLanguageCode(), new Stack<>());
                        myUserDAO.save(myUser);
                        String greeting = getMessage("greeting", myUser.getLanguage());
                        SendMessage sendMessage = SendMessage.builder()
                                .chatId(ctx.chatId().toString())
                                .text(greeting).build();
                        executeMessage(abilityBot, myUser.getId(), myUserDAO, sendMessage);

                    } else {
                        myUser = myUserDAO.getById(user.getId());
                        if(Boolean.TRUE.equals(myUser.getIsUnsubscribed())) {
                            myUser.setIsUnsubscribed(false);
                            myUserDAO.save(myUser);
                        }
                    }
                    lang = myUser.getLanguage();


                    String text = getMessage("start", lang);
                    SendMessage sendMessage = SendMessage.builder()
                            .chatId(ctx.chatId().toString())
                            .text(text)
                            .replyMarkup(keyboardMaker.getStartKeyboard(lang, myUser.getId())).build();
                    executeMessage(abilityBot, myUser.getId(), myUserDAO, sendMessage);


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
        Long myUserId = getUser(upd).getId();
        MyUser myUser = myUserDAO.getById(myUserId);
        Long chatId = upd.getCallbackQuery().getFrom().getId();
        Integer messageId = upd.getCallbackQuery().getMessage().getMessageId();
        String lang = myUser.getLanguage();
        String text = getMessage(actionName, lang);
        InlineKeyboardMarkup backKeyboard = keyboardMaker.getBackKeyboard(lang);

        editMessage(bot, upd, chatId, messageId, text, backKeyboard);
        myUserDAO.addMessageToStack(myUserId, messageId);
        
    }

    private Predicate<Update> hasCallbackQueryWith(String string) {
        return update -> update.hasCallbackQuery() &&
                update.getCallbackQuery().getData().contains(string);
    }

}
