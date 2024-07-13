package com.github.tah10n.carrentalbot.db.entity;

import lombok.Data;

@Data
public class BookingState {
    private final String carId;
    private final String carName;

    public BookingState(String carId, String carName) {
        this.carId = carId;
        this.carName = carName;
    }

}