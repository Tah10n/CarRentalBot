package com.github.tah10n.carrentalbot.db.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    private List<LocalDate> bookedDates;

    public boolean isAvailable(LocalDate date) {
        return !bookedDates.contains(date);
    }

    public void book(LocalDate date) {
        bookedDates.add(date);
    }

    @Override
    public String toString() {
        return model;
    }
}