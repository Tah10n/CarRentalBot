package com.github.tah10n.carrentalbot.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;


@Setter
@Getter
@Validated
@Configuration
@ConfigurationProperties(prefix = "telegrambot")
public class BotConfig {

    @NotBlank
    private String token;

    @NotBlank
    private String name;

    @NotNull
    private long creatorId;

}