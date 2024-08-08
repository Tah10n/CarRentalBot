package com.github.tah10n.carrentalbot.db.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cars")
public class Car {
    @Id
    private String id;
    private String model;
    private Description description;
    private int pricePerDay;
    private String photoId;
    private Map<Long, List<LocalDate>> map;

    @Override
    public String toString() {
        return model + "\n" + description.getRuDescription() + "\n" +"Цена за день " + pricePerDay;
    }

    public void setDescription(String description, String lang) {
        this.description = new Description();
        setDescriptionForLanguage(description, lang, this.description);
    }

    public String getDescription(String lang) {
        switch (lang) {
            case "en":
                return description.getEnDescription();
            case "ru":
                return description.getRuDescription();
            case "sr":
                return description.getRsDescription();
            default:
                return description.getRuDescription();
        }
    }

    private void setDescriptionForLanguage(String description, String lang, Description descriptionObject) {
        switch (lang) {
            case "en":
                descriptionObject.setEnDescription(description);
                break;
            case "ru":
                descriptionObject.setRuDescription(description);
                break;
            case "sr":
                descriptionObject.setRsDescription(description);
                break;
            default:
                descriptionObject.setRuDescription(description);
                break;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class Description {
        private String enDescription;
        private String ruDescription;
        private String rsDescription;

    }
}