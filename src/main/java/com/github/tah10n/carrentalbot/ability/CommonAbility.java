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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessages;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.github.tah10n.carrentalbot.utils.MessageHandlerUtil.clearMessagesStack;
import static com.github.tah10n.carrentalbot.utils.MessageHandlerUtil.executeMessage;
import static com.github.tah10n.carrentalbot.utils.MessagesUtil.getMessage;

@Slf4j
@Component
public class CommonAbility implements AbilityExtension {
    private final BaseAbilityBot abilityBot;
    private final InlineKeyboardMaker keyboardMaker;
    private final MyUserDAO myUserDAO;
    private final CarService carService;

    public CommonAbility(BaseAbilityBot abilityBot, InlineKeyboardMaker keyboardMaker, MyUserDAO myUserDAO, CarService carService) {
        this.abilityBot = abilityBot;
        this.keyboardMaker = keyboardMaker;
        this.myUserDAO = myUserDAO;
        this.carService = carService;
    }

    public ReplyFlow backButton() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    Long chatId = upd.getCallbackQuery().getFrom().getId();
                    MyUser myUser = myUserDAO.getById(chatId);
                    String lang = myUser.getLanguage();
                    String text = getMessage("start", lang);
                    InlineKeyboardMarkup startKeyboard = keyboardMaker.getStartKeyboard(lang, myUser.getId());
                    clearMessagesStack(bot, chatId, myUserDAO);
                    SendMessage sendMessage = SendMessage.builder()
                            .chatId(chatId)
                            .text(text)
                            .replyMarkup(startKeyboard).build();
                    executeMessage(bot, myUser.getId(), myUserDAO, sendMessage);

                })
                .onlyIf(hasCallbackQueryWith("back"))
                .build();
    }

    public Ability banCommand() {
        return Ability
                .builder()
                .name("ban")
                .info("bans the user")
                .locality(Locality.USER)
                .privacy(Privacy.ADMIN)
                .action(ctx -> {
                    String[] arguments = ctx.arguments();
                    String language = myUserDAO.getById(ctx.user().getId()).getLanguage();

                    if (arguments.length != 1) {
                        SendMessage message = SendMessage.builder()
                                .chatId(ctx.chatId())
                                .text(getMessage("ban_command_usage", language))
                                .build();

                        executeMessage(abilityBot, ctx.user().getId(), myUserDAO, message);
                    } else {
                        String argument = arguments[0];
                        try {
                            myUserDAO.ban(argument);
                            SendMessage message = SendMessage.builder()
                                    .chatId(ctx.chatId())
                                    .text(getMessage("user_was_banned", language))
                                    .build();

                            executeMessage(abilityBot, ctx.user().getId(), myUserDAO, message);
                        } catch (IllegalArgumentException e) {
                            SendMessage message = SendMessage.builder()
                                    .chatId(ctx.chatId())
                                    .text(getMessage("user_not_found", language))
                                    .build();

                            executeMessage(abilityBot, ctx.user().getId(), myUserDAO, message);
                        }
                    }

                }).build();
    }

    public Ability unbanCommand() {
        return Ability
                .builder()
                .name("unban")
                .info("unbans the user")
                .locality(Locality.USER)
                .privacy(Privacy.ADMIN)
                .action(ctx -> {
                    String[] arguments = ctx.arguments();
                    String language = myUserDAO.getById(ctx.user().getId()).getLanguage();

                    if (arguments.length != 1) {
                        SendMessage message = SendMessage.builder()
                                .chatId(ctx.chatId())
                                .text(getMessage("unban_command_usage", language))
                                .build();

                        executeMessage(abilityBot, ctx.user().getId(), myUserDAO, message);
                    } else {
                        String argument = arguments[0];
                        try {
                            myUserDAO.unban(argument);
                            SendMessage message = SendMessage.builder()
                                    .chatId(ctx.chatId())
                                    .text(getMessage("user_was_unbanned", language))
                                    .build();

                            executeMessage(abilityBot, ctx.user().getId(), myUserDAO, message);
                        } catch (IllegalArgumentException e) {
                            SendMessage message = SendMessage.builder()
                                    .chatId(ctx.chatId())
                                    .text(getMessage("user_not_found", language))
                                    .build();

                            executeMessage(abilityBot, ctx.user().getId(), myUserDAO, message);
                        }
                    }

                }).build();
    }

    public ReplyFlow unsubscribeFlow() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    Long myUserId = upd.getMyChatMember().getFrom().getId();
                    MyUser myUser = myUserDAO.getById(myUserId);
                    myUser.setIsUnsubscribed(true);
                    myUserDAO.save(myUser);

                })
                .onlyIf(hasStatusKicked())
                .build();
    }

    private Predicate<Update> hasStatusKicked() {
        return update -> {
            if (update.hasMyChatMember()) {
                ChatMemberUpdated myChatMember = update.getMyChatMember();
                return myChatMember.getNewChatMember().getStatus().equals("kicked");
            }
            return false;

        };
    }

    private Predicate<Update> hasCallbackQueryWith(String string) {
        return update -> update.hasCallbackQuery() && update.getCallbackQuery().getData().contains(string);
    }
}
