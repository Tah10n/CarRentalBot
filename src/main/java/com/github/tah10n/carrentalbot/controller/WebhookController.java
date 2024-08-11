package com.github.tah10n.carrentalbot.controller;

import com.github.tah10n.carrentalbot.CarRentalBot;
import com.github.tah10n.carrentalbot.db.dao.MyUserDAO;
import com.github.tah10n.carrentalbot.db.entity.MyUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getUser;


@Slf4j
@AllArgsConstructor
@RestController
public class WebhookController {
    private final CarRentalBot bot;
    private final MyUserDAO myUserDAO;


    @PostMapping("/")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        MyUser myUser = myUserDAO.getById(getUser(update).getId());

        if (myUser.getIsBanned() != null && myUser.getIsBanned()) {
            return null;
        }
        return bot.consumeUpdate(update);
    }
}