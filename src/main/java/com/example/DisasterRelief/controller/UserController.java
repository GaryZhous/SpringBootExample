package com.example.DisasterRelief.controller;

import com.example.DisasterRelief.Entity.User;
import com.example.DisasterRelief.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> body) {
        String username = HtmlUtils.htmlEscape(body.getOrDefault("username", ""));
        String email = HtmlUtils.htmlEscape(body.getOrDefault("email", ""));
        String role = HtmlUtils.htmlEscape(body.getOrDefault("role", "USER"));

        if (username.isBlank() || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "username and email are required"));
        }

        User created = userService.createUser(username, email, role);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id,
                                        @RequestBody Map<String, String> body) {
        String username = HtmlUtils.htmlEscape(body.getOrDefault("username", ""));
        String email = HtmlUtils.htmlEscape(body.getOrDefault("email", ""));
        String role = HtmlUtils.htmlEscape(body.getOrDefault("role", "USER"));

        if (username.isBlank() || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "username and email are required"));
        }

        Optional<User> updated = userService.updateUser(id, username, email, role);
        return updated.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }
}

