package com.github.tah10n.carrentalbot.ability;

import com.github.tah10n.carrentalbot.db.dao.MyUserDAO;
import com.github.tah10n.carrentalbot.keyboards.InlineKeyboardMaker;
import com.github.tah10n.carrentalbot.service.CarService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Locality;
import org.telegram.telegrambots.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static com.github.tah10n.carrentalbot.utils.MessagesUtil.getMessage;

@Slf4j
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

                        abilityBot.getSilent().execute(message);
                    } else {
                        String argument = arguments[0];
                        try {
                            myUserDAO.ban(argument);
                            SendMessage message = SendMessage.builder()
                                    .chatId(ctx.chatId())
                                    .text(getMessage("user_was_banned", language))
                                    .build();

                            abilityBot.getSilent().execute(message);
                        } catch (IllegalArgumentException e) {
                            SendMessage message = SendMessage.builder()
                                    .chatId(ctx.chatId())
                                    .text(getMessage("user_not_found", language))
                                    .build();

                            abilityBot.getSilent().execute(message);
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

                        abilityBot.getSilent().execute(message);
                    } else {
                        String argument = arguments[0];
                        try {
                            myUserDAO.unban(argument);
                            SendMessage message = SendMessage.builder()
                                    .chatId(ctx.chatId())
                                    .text(getMessage("user_was_unbanned", language))
                                    .build();

                            abilityBot.getSilent().execute(message);
                        } catch (IllegalArgumentException e) {
                            SendMessage message = SendMessage.builder()
                                    .chatId(ctx.chatId())
                                    .text(getMessage("user_not_found", language))
                                    .build();

                            abilityBot.getSilent().execute(message);
                        }
                    }

                }).build();
    }
}
