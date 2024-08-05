package com.github.tah10n.carrentalbot.db.dao;

import com.github.tah10n.carrentalbot.db.entity.Car;
import com.github.tah10n.carrentalbot.db.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarDAO {
    private final CarRepository carRepository;

    public CarDAO(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public void save(Car car) {
        carRepository.save(car);
    }

    public Car getById(String id) {
        return carRepository.findById(id).orElse(null);
    }

    public boolean existsById(String id) {
        return carRepository.existsById(id);
    }

    public List<Car> getAll() {
        return carRepository.findAll();
    }

    public void deleteById(String id) {
        carRepository.deleteById(id);
    }


    public Car getByModel(String model) {
        return carRepository.findByModel(model);
    }
}
