package com.github.tah10n.carrentalbot;

import com.github.tah10n.carrentalbot.ability.*;
import com.github.tah10n.carrentalbot.config.BotConfig;
import com.github.tah10n.carrentalbot.db.dao.MyUserDAO;
import com.github.tah10n.carrentalbot.db.entity.Car;
import com.github.tah10n.carrentalbot.keyboards.InlineKeyboardMaker;
import com.github.tah10n.carrentalbot.service.CarService;
import com.github.tah10n.carrentalbot.utils.MessagesUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.Map;

@Component
public class CarRentalBot extends AbilityBot implements SpringLongPollingBot {

    private final BotConfig botConfig;
    private final Map<String, Car> cars = new HashMap<>();
    private final InlineKeyboardMaker keyboardMaker;
    private final MyUserDAO myUserDAO;
    private final CarService carService;

    public CarRentalBot(BotConfig botConfig, TelegramClient telegramClient, InlineKeyboardMaker keyboardMaker, MyUserDAO myUserDAO, CarService carService) {
        super(telegramClient, botConfig.getName());
        this.botConfig = botConfig;
        this.keyboardMaker = keyboardMaker;
        this.myUserDAO = myUserDAO;
        this.carService = carService;

        addExtensions(
                new StartAbility(this, keyboardMaker, myUserDAO, carService),
                new LanguageAbility(this, keyboardMaker, myUserDAO),
                new ListOfCarsAbility(this, keyboardMaker, myUserDAO, carService),
                new BookACarAbility(this, keyboardMaker, myUserDAO, carService)
        );

    }

    @Override
    public long creatorId() {
        return botConfig.getCreatorId();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

}