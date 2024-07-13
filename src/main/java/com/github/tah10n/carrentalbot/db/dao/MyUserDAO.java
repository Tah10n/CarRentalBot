package com.github.tah10n.carrentalbot.db.dao;

import com.github.tah10n.carrentalbot.db.entity.MyUser;
import com.github.tah10n.carrentalbot.db.repository.MyUserRepository;
import org.springframework.stereotype.Service;

@Service
public class MyUserDAO {

    private final MyUserRepository myUserRepository;

    public MyUserDAO(MyUserRepository myUserRepository) {
        this.myUserRepository = myUserRepository;
    }

    public void save(MyUser user) {
        myUserRepository.save(user);
    }

    public MyUser getById(Long id) {
        return myUserRepository.findById(id).orElse(null);
    }

    public boolean existsById(Long id) {
        return myUserRepository.existsById(id);
    }
}
