package com.github.tah10n.carrentalbot.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;


@Setter
@Getter
@Validated
@Configuration

public class BotConfig {

    @NotBlank
    @Value("${telegrambot.token}")
    private String token;

    @NotBlank
    @Value("${telegrambot.name}")
    private String name;

    @Value("${telegrambot.botPath}")
    private String botPath;

    @NotNull
    @Value("${telegrambot.creatorId}")
    private long creatorId;

}