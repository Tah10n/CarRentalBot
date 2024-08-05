package com.github.tah10n.carrentalbot.db.repository;

import com.github.tah10n.carrentalbot.db.entity.Car;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends MongoRepository<Car, String> {

    Car findByModel(String model);
}