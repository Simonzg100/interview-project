package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
public class UserController {

    // TODO: wire in the user repository (~ 1 line)
    @Autowired
    private UserRepository userRepository;

    @PutMapping("/user")
    public ResponseEntity<?> createUser(@RequestBody CreateUserPayload payload) {
        // TODO: Create an user entity with information given in the payload, store it in the database
        //       and return the id of the user in 200 OK response
        String name = payload.getName();
        String email = payload.getEmail();
        // 1. validate the input
        if (name == null || name.isEmpty() || email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Name and email must not be empty");
        }

        // 2. validate the format of email
        if (!email.matches("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$")) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }

        // 3. check if the email is already in use
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already in use");
        }

        // 4. create the user
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setCreditCards(new ArrayList<>());
        userRepository.save(user);
        return ResponseEntity.ok(user.getId());
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {
        // TODO: Return 200 OK if a user with the given ID exists, and the deletion is successful
        //       Return 400 Bad Request if a user with the ID does not exist
        //       The response body could be anything you consider appropriate
        // 1. check if the user exists
        if (userRepository.findById(userId).isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }
        // 2. delete the user1
        userRepository.deleteById(userId);

        return ResponseEntity.ok("User deleted");
    }
}
