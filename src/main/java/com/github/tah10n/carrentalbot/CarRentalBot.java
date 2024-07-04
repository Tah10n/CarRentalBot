package com.github.tah10n.carrentalbot;

import com.github.tah10n.carrentalbot.ability.HelloWorld;
import com.github.tah10n.carrentalbot.config.BotConfig;
import com.github.tah10n.carrentalbot.entity.BookingState;
import com.github.tah10n.carrentalbot.entity.Car;
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
    private final Map<Long, BookingState> userStates = new HashMap<>();

    public CarRentalBot(BotConfig botConfig, TelegramClient telegramClient) {
        super(telegramClient, botConfig.getName());
        this.botConfig = botConfig;

        addExtension(new HelloWorld(this));

        this.onRegister();

        // Инициализация списка автомобилей
        cars.put("car1", new Car("Седан", 50));
        cars.put("car2", new Car("Внедорожник", 80));
        cars.put("car3", new Car("Минивэн", 70));
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