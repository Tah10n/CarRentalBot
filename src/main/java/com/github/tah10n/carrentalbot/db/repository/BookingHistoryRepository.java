package com.github.tah10n.carrentalbot.db.repository;

import com.github.tah10n.carrentalbot.db.entity.BookingHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingHistoryRepository extends MongoRepository<BookingHistory, String> {

    public List<BookingHistory> findAllByCarIdAndIsActive(String carId, boolean isActive);

    List<BookingHistory> findAllByIsActive(boolean isActive);

    List<BookingHistory> findAllByIsActiveAndUserId(boolean isActive, Long userId);
}
