package com.egor.crud_api_checkpoint.controller;

import com.egor.crud_api_checkpoint.model.User;
import com.egor.crud_api_checkpoint.repository.UserRepository;
import com.egor.crud_api_checkpoint.request.UserRequest;
import com.egor.crud_api_checkpoint.response.AuthResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public Iterable<User> getUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    public User createUser(@RequestBody UserRequest userRequest) {
        User newUser = new User();
        newUser.setEmail(userRequest.getEmail());
        newUser.setPassword(userRequest.getPassword());
        return userRepository.save(newUser);
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable(name = "id") Long id) {
        return userRepository.findById(id).get();
    }

    @PatchMapping("/{id}")
    public User patchUser(@PathVariable(name = "id") Long id, @RequestBody UserRequest userRequest) {
        User patchedUser = userRepository.findById(id).get();
        if (userRequest.getEmail() != null) patchedUser.setEmail(userRequest.getEmail());
        if (userRequest.getPassword() != null) patchedUser.setPassword(userRequest.getPassword());
        return userRepository.save(patchedUser);
    }

    @DeleteMapping("/{id}")
    public Map<String, Long> deleteUser(@PathVariable(name = "id") Long id) {
        userRepository.deleteById(id);
        Map<String, Long> result = new HashMap<>();
        result.put("count", userRepository.count());
        return result;
    }

    @PostMapping("/authenticate")
    public AuthResponse authenticateUser(@RequestBody UserRequest userRequest) {
        User userToAuth = userRepository.findByEmail(userRequest.getEmail()).get();
        if (userToAuth.getPassword().equals(userRequest.getPassword())) {
            return new AuthResponse(true, userToAuth);
        }
        return new AuthResponse(false);
    }
}
