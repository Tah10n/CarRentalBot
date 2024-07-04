package com.github.tah10n.carrentalbot.entity;

import lombok.Data;

@Data
public class BookingState {
    private final String carId;

    public BookingState(String carId) {
        this.carId = carId;
    }

    public String getCarId() {
        return carId;
    }
}