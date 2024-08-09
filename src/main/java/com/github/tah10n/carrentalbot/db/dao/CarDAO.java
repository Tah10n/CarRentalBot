package com.github.tah10n.carrentalbot.db.dao;

import com.github.tah10n.carrentalbot.db.entity.Car;
import com.github.tah10n.carrentalbot.db.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CarDAO {
    private final CarRepository carRepository;

    private final Map<String,Car> carsCacheMap = new HashMap<>();
    
    public CarDAO(CarRepository carRepository) {
        this.carRepository = carRepository;

        fetchCarsFromDB();
    }

    private void fetchCarsFromDB() {
        List<Car> all = carRepository.findAll();

        carsCacheMap.clear();
        for (Car car : all) {
            carsCacheMap.put(car.getId(),car);
        }
    }

    public void save(Car car) {
        carRepository.save(car);
        fetchCarsFromDB();
    }

    public Car getById(String id) {
        return carsCacheMap.get(id);
    }

    public boolean existsById(String id) {
        return carsCacheMap.containsKey(id);
    }

    public List<Car> getAll() {
        return carsCacheMap.values().stream().toList();
    }

}
