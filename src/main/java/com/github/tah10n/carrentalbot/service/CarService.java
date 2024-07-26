package com.github.tah10n.carrentalbot.service;

import com.github.tah10n.carrentalbot.db.dao.CarDAO;
import com.github.tah10n.carrentalbot.db.entity.Car;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CarService {
    private final CarDAO carDAO;

    public CarService(CarDAO carDAO) {
        this.carDAO = carDAO;
    }

    public List<Car> getAllCars() {
        return carDAO.getAll();
    }

    public boolean addCar(String model, String description, String lang) {
        if(carDAO.getAll().stream().map(Car::getModel).anyMatch(mdl -> mdl.equals(model))) {
            return false;
        }
        Car car = new Car();
        car.setModel(model);
        car.setDescription(description, lang);
        car.setMap(new HashMap<>());
        carDAO.save(car);
        return true;
    }

    public void deleteCar(String carId) {
        carDAO.deleteById(carId);
    }

    public boolean isAvailableDate(LocalDate date, String carId) {
        Car car = carDAO.getById(carId);
        Map<Long, List<LocalDate>> map = car.getMap();
        Set<LocalDate> dates = map.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        return !dates.contains(date);
    }

    public void book(Long userId, List<LocalDate> dates, String carId) {
        Car car = carDAO.getById(carId);
        Map<Long, List<LocalDate>> map = car.getMap();
        map.put(userId, dates);
        car.setMap(map);
        carDAO.save(car);
    }

    public List<LocalDate> getBookedDates (Long userId, String carId) {
        Car car = carDAO.getById(carId);
        Map<Long, List<LocalDate>> map = car.getMap();
        if(map != null) {
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
        if(map==null) {
            map = new HashMap<>();
        }
        if(!map.containsKey(myUserId)) {
            map.put(myUserId, List.of(date));
        } else {
            List<LocalDate> bookedDates = map.get(myUserId);
            if(bookedDates.contains(date)) {
                bookedDates.remove(date);
            } else {
                bookedDates.add(date);
            }
        }
        car.setMap(map);
        carDAO.save(car);
        return car;
    }
}
