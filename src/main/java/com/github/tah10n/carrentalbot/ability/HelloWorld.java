package com.github.tah10n.carrentalbot.ability;

import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Locality;
import org.telegram.telegrambots.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

public class HelloWorld implements AbilityExtension {
    private AbilityBot bot;

    public HelloWorld(AbilityBot bot) {
        this.bot = bot;
    }

    public Ability sayHelloWorld() {
        return Ability
                .builder()
                .name("hello")
                .info("says hello world!")
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> bot.getSilent().send("Hello World!", ctx.chatId()))
                .build();
    }
}
