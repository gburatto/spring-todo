package com.utfpr.todo.users;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import at.favre.lib.crypto.bcrypt.BCrypt;

@RestController
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserModel user) {

        UserModel userModel = userRepository.findByUsername(user.getUsername());

        if (userModel != null){
            //throw new RuntimeException("Username already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Collections.singletonMap("error", "Username already exists")
            );
        }

        String hashedPassword = BCrypt.withDefaults().hashToString(12, user.getPassword().toCharArray());

        user.setPassword(hashedPassword);
        
        UserModel newUser = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {

        List<UserModel> users = userRepository.findAll();
        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Collections.singletonMap("error", "No users found")
            );
        }

        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable UUID id) {

        Optional<UserModel> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Collections.singletonMap("error", "User not found")
            );
        }

        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody UserModel updatedUserInfo) {

        Optional<UserModel> user = userRepository.findById(id);
        UserModel userModel = userRepository.findByUsername(updatedUserInfo.getUsername());
        UserModel currentUser = userRepository.getReferenceById(id);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Collections.singletonMap("error", "User not found")
            );
        }

        if (userModel != null && userModel != currentUser){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Collections.singletonMap("error", "Username already exists")
            );
        }
        

        String hashedPassword = BCrypt.withDefaults().hashToString(12, updatedUserInfo.getPassword().toCharArray());

        updatedUserInfo.setPassword(hashedPassword);
        
        
        currentUser.setUsername(updatedUserInfo.getUsername());
        currentUser.setName(updatedUserInfo.getName());
        currentUser.setEmail(updatedUserInfo.getEmail());
        currentUser.setPassword(updatedUserInfo.getPassword());
        UserModel updatedUser = userRepository.save(currentUser);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {

        Optional<UserModel> existingUser = userRepository.findById(id);
        UserModel user = userRepository.getReferenceById(id);
        if (existingUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Collections.singletonMap("error", "User not found")
            );
        }

        userRepository.delete(user);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patch(@PathVariable UUID id, @RequestBody UserModel updatedUserInfo) {

        Optional<UserModel> user = userRepository.findById(id);
        UserModel userModel = userRepository.findByUsername(updatedUserInfo.getUsername());
        UserModel currentUser = userRepository.getReferenceById(id);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Collections.singletonMap("error", "User not found")
            );
        }

        if (updatedUserInfo.getUsername() != null) {
            if (userModel != null && userModel != currentUser){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Collections.singletonMap("error", "Username already exists")
                );
            }

            currentUser.setUsername(updatedUserInfo.getUsername());
        }

        if (updatedUserInfo.getName() != null) {
            currentUser.setName(updatedUserInfo.getName());
        }

        if (updatedUserInfo.getEmail() != null) {
            currentUser.setEmail(updatedUserInfo.getEmail());
        }

        if (updatedUserInfo.getPassword() != null) {
            String hashedPassword = BCrypt.withDefaults().hashToString(12, updatedUserInfo.getPassword().toCharArray());

            updatedUserInfo.setPassword(hashedPassword);
            currentUser.setPassword(updatedUserInfo.getPassword());
        }

        UserModel updatedUser = userRepository.save(currentUser);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }
}


