package com.github.tah10n.carrentalbot.db.dao;

import com.github.tah10n.carrentalbot.db.entity.MyUser;
import com.github.tah10n.carrentalbot.db.repository.MyUserRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MyUserDAO {

    private final MyUserRepository myUserRepository;
    private final Map<Long, MyUser> myUsersCacheMap = new HashMap<>();

    public MyUserDAO(MyUserRepository myUserRepository) {
        this.myUserRepository = myUserRepository;

        fetchMyUsersFromDB();
    }

    private void fetchMyUsersFromDB() {
        List<MyUser> all = myUserRepository.findAll();

        myUsersCacheMap.clear();
        for (MyUser myUser : all) {
            myUsersCacheMap.put(myUser.getId(), myUser);
        }
    }

    public void save(MyUser user) {
        myUsersCacheMap.put(user.getId(), user);
        pushMyUserToDB(user);
    }

    public MyUser getById(Long id) {
        return myUsersCacheMap.get(id);
    }

    public boolean existsById(Long id) {
        return myUsersCacheMap.containsKey(id);
    }

    public void addMessageToStack(Long userId, Integer messageId) {
        MyUser user = getById(userId);
        if (user == null) {
            return;
        }
        List<Integer> messagesStack = user.getMessagesStack();
        if (messagesStack == null) {
            messagesStack = new Stack<>();
        }
        messagesStack.add(messageId);
        user.setMessagesStack(messagesStack);
        myUsersCacheMap.put(userId, user);
        pushMyUserToDB(user);
    }

    public boolean isMessageStackFilled(Long userId) {
        MyUser user = getById(userId);
        if (user == null) {
            return false;
        }
        List<Integer> messagesStack = user.getMessagesStack();
        return messagesStack != null && !messagesStack.isEmpty();
    }

    public List<Integer> popAllMessagesFromStack(Long userId) {
        MyUser user = getById(userId);
        if (user == null) {
            return Collections.emptyList();
        }
        List<Integer> messages = user.getMessagesStack();
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        user.setMessagesStack(new Stack<>());
        myUsersCacheMap.put(userId, user);
        pushMyUserToDB(user);
        return messages;
    }

    public void pushMyUserToDB(MyUser user) {
        myUserRepository.save(user);
    }

    public void ban(String argument) throws IllegalArgumentException {
        MyUser user;
        try {
            long userId = Long.parseLong(argument);
            user = getById(userId);

        } catch (NumberFormatException e) {
            user = myUserRepository.findByUserName(argument);

        }
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        user.setIsBanned(true);
        myUsersCacheMap.put(user.getId(), user);
        pushMyUserToDB(user);
    }

    public void unban(String argument) throws IllegalArgumentException {
        MyUser user;
        try {
            long userId = Long.parseLong(argument);
            user = getById(userId);

        } catch (NumberFormatException e) {
            user = myUserRepository.findByUserName(argument);
        }
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        user.setIsBanned(false);
        myUsersCacheMap.put(user.getId(), user);
        pushMyUserToDB(user);
    }
}
