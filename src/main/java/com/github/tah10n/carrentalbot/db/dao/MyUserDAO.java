package com.github.tah10n.carrentalbot.db.dao;

import com.github.tah10n.carrentalbot.db.entity.MyUser;
import com.github.tah10n.carrentalbot.db.repository.MyUserRepository;
import java.util.Collections;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Stack;

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

    public void addMessageToStack(Long userId, Integer messageId) {
        MyUser user = myUserRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }
        List<Integer> messagesStack = user.getMessagesStack();
        if (messagesStack == null) {
            messagesStack = new Stack<>();
        }
        messagesStack.add(messageId);
        user.setMessagesStack(messagesStack);
        myUserRepository.save(user);
    }

    public boolean isMessageStackFilled(Long userId) {
        MyUser user = myUserRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        List<Integer> messagesStack = user.getMessagesStack();
        return messagesStack != null && !messagesStack.isEmpty();
    }

    public Integer getMessageFromStack(Long myUserId) {
        MyUser user = myUserRepository.findById(myUserId).orElse(null);
        if (user == null) {
            return null;
        }
        Stack<Integer> messagesStack = (Stack<Integer>) user.getMessagesStack();
        if (messagesStack == null) {
            return null;
        }
        return messagesStack.pop();
    }

    public List<Integer> popAllMessagesFromStack(Long myUserId) {
        MyUser user = myUserRepository.findById(myUserId).orElse(null);
        if (user == null) {
            return Collections.emptyList();
        }
        List<Integer> messages = user.getMessagesStack();
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        user.setMessagesStack(new Stack<>());
        myUserRepository.save(user);
        return messages;
    }
}
