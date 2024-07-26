package com.github.tah10n.carrentalbot;

import com.github.tah10n.carrentalbot.ability.LanguageAbility;
import com.github.tah10n.carrentalbot.ability.ListOfCarsAbility;
import com.github.tah10n.carrentalbot.ability.RentACarAbility;
import com.github.tah10n.carrentalbot.ability.StartAbility;
import com.github.tah10n.carrentalbot.config.BotConfig;
import com.github.tah10n.carrentalbot.db.dao.CarDAO;
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
    private final MessagesUtil messagesUtil;

    public CarRentalBot(BotConfig botConfig, TelegramClient telegramClient, InlineKeyboardMaker keyboardMaker, MyUserDAO myUserDAO, CarService carService, MessagesUtil messagesUtil) {
        super(telegramClient, botConfig.getName());
        this.botConfig = botConfig;
        this.keyboardMaker = keyboardMaker;
        this.myUserDAO = myUserDAO;
        this.carService = carService;
        this.messagesUtil = messagesUtil;

        addExtensions(
                new StartAbility(this, keyboardMaker, myUserDAO, messagesUtil),
                new LanguageAbility(this, keyboardMaker, myUserDAO, messagesUtil),
                new ListOfCarsAbility(this, keyboardMaker, myUserDAO, carService, messagesUtil),
                new RentACarAbility(this, keyboardMaker, myUserDAO, carService, messagesUtil)
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