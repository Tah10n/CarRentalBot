package com.github.tah10n.carrentalbot.db.repository;

import com.github.tah10n.carrentalbot.db.entity.MyUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyUserRepository extends MongoRepository<MyUser, Long> {

}