package org.example.service;

import org.example.entity.User;
import org.example.repository.UserRepository;

import java.util.UUID;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void deleteUserById(UUID id) {
        User user = userRepository.getUserById(id);
        userRepository.delete(user);
    }

    public User getUserById(UUID id) {
        return userRepository.getUserById(id);
    }

    public User getUserByUsername(String username) {
        return userRepository.getUserByUsername(username);
    }

    public void createUser(User user) {
        userRepository.save(user);
    }

}
