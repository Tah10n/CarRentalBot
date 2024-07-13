package com.github.tah10n.carrentalbot;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;


@Component
public class BotInitializer implements InitializingBean {

    private final CarRentalBot bot;

    public BotInitializer(CarRentalBot bot) {
        this.bot = bot;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        bot.onRegister();
    }
}
