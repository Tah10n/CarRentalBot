package com.github.tah10n.carrentalbot.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
@AllArgsConstructor
public class SpringConfig {

    @Bean
    public TelegramClient telegramClient(BotConfig botConfig) {
        return new OkHttpTelegramClient(botConfig.getToken());
    }


}
