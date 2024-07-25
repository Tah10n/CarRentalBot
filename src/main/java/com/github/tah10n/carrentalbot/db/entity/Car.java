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
    private String description;
    private int pricePerDay;
    private Map<Long, List<LocalDate>> map;

    public boolean isAvailable(LocalDate date) {
        Set<LocalDate> dates = map.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        return !dates.contains(date);
    }

    public void book(Long userId, List<LocalDate> dates) {
        map.put(userId, dates);
    }

    public List<LocalDate> getBookedDates (Long userId) {
        if(map != null) {
            return map.get(userId);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String toString() {
        return model;
    }
}