package com.github.tah10n.carrentalbot.service;

import com.github.tah10n.carrentalbot.db.dao.BookingHistoryDAO;
import com.github.tah10n.carrentalbot.db.dao.CarDAO;
import com.github.tah10n.carrentalbot.db.entity.BookingHistory;
import com.github.tah10n.carrentalbot.db.entity.Car;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CarService {
    private final CarDAO carDAO;
    private final BookingHistoryDAO bookingHistoryDAO;

    public CarService(CarDAO carDAO, BookingHistoryDAO bookingHistoryDAO) {
        this.carDAO = carDAO;
        this.bookingHistoryDAO = bookingHistoryDAO;
    }

    public List<Car> getAllCars() {
        return carDAO.getAll();
    }

    public boolean addCar(Car car) {
        if (carDAO.getAll().stream().map(Car::getModel).anyMatch(mdl -> mdl.equals(car.getModel()))) {
            return false;
        }

        carDAO.save(car);
        return true;
    }

    public List<LocalDate> getBookedDates(Long userId, String carId) {
        Car car = carDAO.getById(carId);
        Map<Long, List<LocalDate>> map = car.getMap();
        if (map != null) {
            return map.get(userId);
        } else {
            return Collections.emptyList();
        }
    }

    public String getCarName(String carId) {
        Car car = carDAO.getById(carId);
        return car.getModel();
    }

    public Car addDate(Long myUserId, String carId, LocalDate date) {
        Car car = carDAO.getById(carId);
        Map<Long, List<LocalDate>> map = car.getMap();
        if (map == null) {
            map = new HashMap<>();
        }
        if (!map.containsKey(myUserId)) {
            map.put(myUserId, List.of(date));
        } else {
            List<LocalDate> bookedDates = map.get(myUserId);
            if (bookedDates.contains(date)) {
                bookedDates.remove(date);
            } else {
                bookedDates.add(date);
            }
        }
        car.setMap(map);
        carDAO.save(car);
        return car;
    }

    public void clearDates(Long myUserId, String carId) {
        Car car = carDAO.getById(carId);
        Map<Long, List<LocalDate>> map = car.getMap();
        map.remove(myUserId);
        car.setMap(map);
        carDAO.save(car);
    }

    public void bookACar(Long myUserId, String carId) {
        Car car = carDAO.getById(carId);
        Map<Long, List<LocalDate>> map = car.getMap();

        List<LocalDate> bookedDates = map.get(myUserId);
        if (bookedDates == null) {
            throw new NullPointerException("Booked dates is null");
        }
        BookingHistory bookingHistory = new BookingHistory();
        bookingHistory.setCarId(carId);
        bookingHistory.setUserId(myUserId);
        bookingHistory.setBookedDates(bookedDates);
        bookingHistory.setActive(true);
        if (checkIsBookedDatesAlreadyOccupied(bookedDates)) {
            throw new IllegalArgumentException("Car is not available");
        } else {
            bookingHistoryDAO.save(bookingHistory);
        }
    }

    private boolean checkIsBookedDatesAlreadyOccupied(List<LocalDate> bookedDates) {
        List<LocalDate> allActiveBookedDates = bookingHistoryDAO.getAllActiveBookedDates();
        for (LocalDate bookedDate : bookedDates) {
            if (allActiveBookedDates.contains(bookedDate)) {
                return true;
            }
        }
        return false;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void checkExpiredDates() {
        System.out.println("\n \n  Checking expired dates \n \n");
        List<BookingHistory> allActiveHistory = bookingHistoryDAO.getAllActiveHistory();
        for (BookingHistory history : allActiveHistory) {
            LocalDate nowDate = LocalDate.now();
            for (LocalDate bookedDate : history.getBookedDates()) {
                if (bookedDate.isAfter(nowDate)) {
                    return;
                }
            }
            history.setActive(false);
            bookingHistoryDAO.save(history);
        }
    }

    public boolean isBookedDate(LocalDate date, String carId) {
        List<LocalDate> allActiveBookedDates = bookingHistoryDAO.getActiveBookedDates(carId);
        return allActiveBookedDates.contains(date);
    }

    public boolean isUserHasBookings(Long myUserId) {
        List<BookingHistory> allActiveHistory = bookingHistoryDAO.getAllActiveHistory();
        for (BookingHistory history : allActiveHistory) {
            if (history.getUserId().equals(myUserId)) {
                return true;
            }
        }
        return false;
    }

    public List<BookingHistory> getBookingsByUserId(Long myUserId) {
        return bookingHistoryDAO.getActiveBookingsByUserId(myUserId);
    }

    public void deleteBookingById(String bookId) {
        bookingHistoryDAO.deleteById(bookId);
    }

    public boolean hasCarPhoto(String carId) {
        return carDAO.getById(carId).getPhotoId() != null;

    }
}
