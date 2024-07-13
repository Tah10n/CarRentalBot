package com.github.tah10n.carrentalbot;

import com.github.tah10n.carrentalbot.ability.LanguageAbility;
import com.github.tah10n.carrentalbot.ability.StartAbility;
import com.github.tah10n.carrentalbot.config.BotConfig;
import com.github.tah10n.carrentalbot.db.dao.MyUserDAO;
import com.github.tah10n.carrentalbot.db.entity.Car;
import com.github.tah10n.carrentalbot.keyboards.InlineKeyboardMaker;
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

    public CarRentalBot(BotConfig botConfig, TelegramClient telegramClient, InlineKeyboardMaker keyboardMaker, MyUserDAO myUserDAO) {
        super(telegramClient, botConfig.getName());
        this.botConfig = botConfig;
        this.keyboardMaker = keyboardMaker;
        this.myUserDAO = myUserDAO;

        addExtensions(new StartAbility(this, keyboardMaker, myUserDAO), new LanguageAbility(this, keyboardMaker, myUserDAO));

        // Инициализация списка автомобилей
        cars.put("car1", new Car("ауди", 50));
        cars.put("car2", new Car("миникупер", 80));
        cars.put("car3", new Car("рено", 70));
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