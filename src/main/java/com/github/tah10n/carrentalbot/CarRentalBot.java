package com.github.tah10n.carrentalbot;

import com.github.tah10n.carrentalbot.ability.BookACarAbility;
import com.github.tah10n.carrentalbot.ability.LanguageAbility;
import com.github.tah10n.carrentalbot.ability.ListOfCarsAbility;
import com.github.tah10n.carrentalbot.ability.StartAbility;
import com.github.tah10n.carrentalbot.config.BotConfig;
import com.github.tah10n.carrentalbot.db.dao.MyUserDAO;
import com.github.tah10n.carrentalbot.keyboards.InlineKeyboardMaker;
import com.github.tah10n.carrentalbot.service.CarService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityWebhookBot;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class CarRentalBot extends AbilityWebhookBot {

    private final BotConfig botConfig;
    private final InlineKeyboardMaker keyboardMaker;
    private final MyUserDAO myUserDAO;
    private final CarService carService;

    public CarRentalBot(BotConfig botConfig, TelegramClient telegramClient, InlineKeyboardMaker keyboardMaker, MyUserDAO myUserDAO, CarService carService) {
        super(telegramClient, botConfig.getName(), botConfig.getBotPath());
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
    public void runDeleteWebhook() {
//        var telegramUrl = "https://api.telegram.org/bot" + botConfig.getToken();
//        var url = telegramUrl + "/deleteWebhook?url=" + botConfig.getBotPath();
//        final HttpClient client = HttpClient.newHttpClient();
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(url))
//                .build();
//        HttpResponse<String> response = null;
//        try {
//            response = client.send(request, HttpResponse.BodyHandlers.ofString());
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void runSetWebhook() {
//        var telegramUrl = "https://api.telegram.org/bot" + botConfig.getToken();
//        var url = telegramUrl + "/setWebhook?url=" + botConfig.getBotPath();
//        final HttpClient client = HttpClient.newBuilder().build();
//        HttpRequest request = null;
//        try {
//            request = HttpRequest.newBuilder()
//                    .uri(new URI(url))
//                    .build();
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
//        HttpResponse<String> response = null;
//        try {
//            response = client.send(request, HttpResponse.BodyHandlers.ofString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

//    @Override
//    public String getBotToken() {
//        return botConfig.getToken();
//    }
//
//    @Override
//    public LongPollingUpdateConsumer getUpdatesConsumer() {
//        return this;
//    }

}