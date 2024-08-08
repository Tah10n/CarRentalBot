package com.github.tah10n.carrentalbot.service;

import com.github.tah10n.carrentalbot.db.dao.BookingHistoryDAO;
import com.github.tah10n.carrentalbot.db.dao.CarDAO;
import com.github.tah10n.carrentalbot.db.entity.BookingHistory;
import com.github.tah10n.carrentalbot.db.entity.Car;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import static com.github.tah10n.carrentalbot.utils.MessagesUtil.getMessage;

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

    public List<LocalDate> getChosenDates(Long userId, String carId) {
        Car car = carDAO.getById(carId);
        if ((car == null) || (car.getMap() == null)) {
            return Collections.emptyList();
        }
        List<LocalDate> dateList = car.getMap().get(userId);
        if(dateList == null) {
            return Collections.emptyList();
        }
        if(dateList.size() == 1) {
            return Collections.singletonList(dateList.get(0));
        }
        var startDate = dateList.get(0);
        var endDate = dateList.get(1);
        if (endDate.isBefore(startDate)) {
            var tmp = endDate;
            endDate = startDate;
            startDate = tmp;
        }
        return getDates(startDate, endDate);
    }

    private List<LocalDate> getDates(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            dates.add(date);
            date = date.plusDays(1);
        }
        return dates;
    }

    public String getCarName(String carId) {
        Car car = carDAO.getById(carId);
        if (car == null) {
            return getMessage("car_not_found", "ru");
        }
        return car.getModel();
    }

    public Car addDate(Long myUserId, String carId, LocalDate date) {
        Car car = carDAO.getById(carId);
        Map<Long, List<LocalDate>> map = car.getMap();
        List<LocalDate> localDates = map.get(myUserId);
        if (localDates == null || localDates.isEmpty()) {
            localDates = new ArrayList<>();
            localDates.add(date);
        } else if (localDates.size() == 1 && !localDates.get(0).equals(date)) {
            localDates.add(date);
        } else {
            localDates.clear();
            localDates.add(date);
        }

        map.put(myUserId, localDates);
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
        if (checkIsChosenDatesAlreadyOccupied(bookedDates, carId)) {
            throw new IllegalArgumentException("Dates is not available");
        } else {
            bookingHistoryDAO.save(bookingHistory);
        }
    }

    public boolean checkIsChosenDatesAlreadyOccupied(@NotNull List<LocalDate> chosenDates, String carId) {
        List<LocalDate> activeBookedDates = bookingHistoryDAO.getActiveBookedDatesByCarId(carId);
        for (LocalDate date : chosenDates) {
            if (activeBookedDates.contains(date)) {
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

    public Car getCarById(String carId) {
        return carDAO.getById(carId);

    }

    public void deleteEndDate(Long myUserId, String carId) {
        Car car = carDAO.getById(carId);
        Map<Long, List<LocalDate>> map = car.getMap();
        List<LocalDate> localDates = map.get(myUserId);
        if (localDates == null || localDates.isEmpty()) {
            return;
        }
        localDates.remove(localDates.size() - 1);
        map.put(myUserId, localDates);
        car.setMap(map);
        carDAO.save(car);
    }
}
