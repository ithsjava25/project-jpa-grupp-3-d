package org.example;

import jakarta.persistence.EntityManager;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.example.util.JpaUtil;

public class App {
    public static void main(String[] args) {
        try (EntityManager em = JpaUtil.getEntityManager()) {

            UserRepository userRepository = new UserRepository(em);
            UserService userService = new UserService(userRepository);

            User user = new User("testUser");

            //Operation 1
            userService.createUser(user);

            User saved = userService.getUserByUsername("testUser");
            System.out.println(saved);

            //Operation 2
            userService.deleteUserById(saved.getId());
        }
    }
}
