package com.github.tah10n.carrentalbot;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
public class BotInitializer {

    private final CarRentalBot bot;

    public BotInitializer(CarRentalBot bot) {
        this.bot = bot;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        bot.onRegister();
    }
}
