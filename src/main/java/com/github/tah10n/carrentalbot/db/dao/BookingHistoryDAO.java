package com.github.tah10n.carrentalbot.db.dao;

import com.github.tah10n.carrentalbot.db.entity.BookingHistory;
import com.github.tah10n.carrentalbot.db.repository.BookingHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingHistoryDAO {
    private final BookingHistoryRepository bookingHistoryRepository;

    public BookingHistoryDAO(BookingHistoryRepository bookingHistoryRepository) {
        this.bookingHistoryRepository = bookingHistoryRepository;
    }


    public void save(BookingHistory bookingHistory) {
        bookingHistoryRepository.save(bookingHistory);
    }

    public BookingHistory getById(String id) {
        return bookingHistoryRepository.findById(id).orElse(null);
    }

    public List<BookingHistory> getActiveBookingsByCarId(String carId) {
        return bookingHistoryRepository.findAllByCarIdAndIsActive(carId, true);
    }

    public List<LocalDate> getActiveBookedDates(String carId) {
        List<BookingHistory> allByCarIdAndIsActive = bookingHistoryRepository.findAllByCarIdAndIsActive(carId, true);
        List<LocalDate> dates = allByCarIdAndIsActive.stream().map(BookingHistory::getBookedDates).flatMap(List::stream).collect(Collectors.toList());
        return dates;
    }

    public List<BookingHistory> getAllActiveHistory() {
        return bookingHistoryRepository.findAllByIsActive(true);
    }

    public List<BookingHistory> getActiveBookingsByUserId(Long userId) {
        return bookingHistoryRepository.findAllByIsActiveAndUserId(true, userId);
    }

    public void deleteById(String bookId) {
        bookingHistoryRepository.deleteById(bookId);
    }

    public List<LocalDate> getActiveBookedDatesByCarId(String carId) {
        return bookingHistoryRepository.findAllByIsActive(true).stream()
                .filter(bh -> bh.getCarId().equals(carId))
                .map(BookingHistory::getBookedDates)
                .flatMap(List::stream).collect(Collectors.toList());
    }
}
