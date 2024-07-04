package com.github.tah10n.carrentalbot.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Car {
    private final String model;
    private final int pricePerDay;
    private final List<LocalDate> bookedDates = new ArrayList<>();

    public Car(String model, int pricePerDay) {
        this.model = model;
        this.pricePerDay = pricePerDay;
    }

    public String getModel() {
        return model;
    }

    public boolean isAvailable(LocalDate date) {
        return !bookedDates.contains(date);
    }

    public void book(LocalDate date) {
        bookedDates.add(date);
    }
}