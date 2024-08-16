package com.github.tah10n.carrentalbot.db.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "booking_history")
public class BookingHistory {

    @Id
    @Indexed(unique = true)
    private String id;
    private String carId;
    private Long userId;
    @CreatedDate
    private LocalDateTime createdAt;
    private List<LocalDate> bookedDates;
    private Integer totalPrice;
    private boolean isActive;

}
