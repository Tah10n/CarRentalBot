package com.github.tah10n.carrentalbot;

import com.github.tah10n.carrentalbot.ability.*;
import com.github.tah10n.carrentalbot.config.BotConfig;
import com.github.tah10n.carrentalbot.db.dao.MyUserDAO;
import com.github.tah10n.carrentalbot.keyboards.InlineKeyboardMaker;
import com.github.tah10n.carrentalbot.service.CarService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityWebhookBot;
import org.telegram.telegrambots.abilitybots.api.toggle.BareboneToggle;
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

    private static final BareboneToggle toggle = new BareboneToggle();

    public CarRentalBot(BotConfig botConfig, TelegramClient telegramClient, InlineKeyboardMaker keyboardMaker, MyUserDAO myUserDAO, CarService carService) {
        super(telegramClient, botConfig.getName(), botConfig.getBotPath(),toggle);
        this.botConfig = botConfig;
        this.keyboardMaker = keyboardMaker;
        this.myUserDAO = myUserDAO;
        this.carService = carService;

        addExtensions(
                new StartAbility(this, this.keyboardMaker, this.myUserDAO, this.carService),
                new LanguageAbility(this, this.keyboardMaker, this.myUserDAO),
                new ListOfCarsAbility(this, this.keyboardMaker, this.myUserDAO, this.carService),
                new BookACarAbility(this, this.keyboardMaker, this.myUserDAO, this.carService),
                new CommonAbility(this, this.keyboardMaker, this.myUserDAO, this.carService)
        );
    }

    @Override
    public long creatorId() {
        return botConfig.getCreatorId();
    }

    @Override
    public void runDeleteWebhook() {
        String telegramApiUrl = "https://api.telegram.org/bot" + botConfig.getToken();
        String deleteWebhookUrl = telegramApiUrl + "/deleteWebhook?url=" + botConfig.getBotPath();

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(deleteWebhookUrl))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException("Failed to delete webhook", e);
        }
    }

    @Override
    public void runSetWebhook() {
        String telegramApiUrl = "https://api.telegram.org/bot" + botConfig.getToken();
        String webhookUrl = telegramApiUrl + "/setWebhook?url=" + botConfig.getBotPath();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(webhookUrl))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException("Failed to set webhook", e);
        }
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