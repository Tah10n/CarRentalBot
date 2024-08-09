package com.github.tah10n.carrentalbot.controller;

import com.github.tah10n.carrentalbot.CarRentalBot;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@AllArgsConstructor
@RestController
public class WebhookController {
    private final CarRentalBot bot;


    @PostMapping("/")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return bot.consumeUpdate(update);
    }
}