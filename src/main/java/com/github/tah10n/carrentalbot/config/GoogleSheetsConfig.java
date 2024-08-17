package com.github.tah10n.carrentalbot.config;

import com.github.tah10n.carrentalbot.service.GoogleSheetsService;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Setter
@Getter
@Configuration
public class GoogleSheetsConfig {
    @Value("${spring.application.name}")
    private String applicationName;
    @Value("${google.spreadSheetId}")
    private String spreadsheetId;
    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final String TOKENS_DIRECTORY_PATH = "tokens";
    private final List<String> SCOPES =
            Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private final String CREDENTIALS_FILE_PATH = "/credentials.json";


    public GoogleCredentials getCredentials() throws IOException {
        return GoogleCredentials.fromStream(Objects.requireNonNull(GoogleSheetsService.class.getResourceAsStream(CREDENTIALS_FILE_PATH)))
                .createScoped(SCOPES);
    }

    public NetHttpTransport getNetHttpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    @Bean
    public Sheets getSheetsService() {
        NetHttpTransport httpTransport = null;
        try {
            httpTransport = getNetHttpTransport();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        GoogleCredentials credential = null;
        try {
            credential = getCredentials();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credential);
        return new Sheets.Builder(httpTransport, JSON_FACTORY, requestInitializer)
                .setApplicationName(applicationName)
                .build();
    }

}
