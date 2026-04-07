package com.example.DisasterRelief.service;

import com.example.DisasterRelief.Entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final File file = new File("users.json");

    @Cacheable("users")
    public List<User> getAllUsers() {
        return readFromFile();
    }

    public Optional<User> getUserById(String id) {
        return readFromFile().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    @CacheEvict(value = "users", allEntries = true)
    public User createUser(String username, String email, String role) {
        List<User> users = readFromFile();
        User user = new User(UUID.randomUUID().toString(), username, email, role);
        users.add(user);
        writeToFile(users);
        return user;
    }

    @CacheEvict(value = "users", allEntries = true)
    public Optional<User> updateUser(String id, String username, String email, String role) {
        List<User> users = readFromFile();
        for (User u : users) {
            if (u.getId().equals(id)) {
                u.setUsername(username);
                u.setEmail(email);
                u.setRole(role);
                writeToFile(users);
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }

    @CacheEvict(value = "users", allEntries = true)
    public boolean deleteUser(String id) {
        List<User> users = readFromFile();
        boolean removed = users.removeIf(u -> u.getId().equals(id));
        if (removed) {
            writeToFile(users);
        }
        return removed;
    }

    private List<User> readFromFile() {
        try {
            if (!file.exists()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(file, new TypeReference<>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void writeToFile(List<User> users) {
        try {
            objectMapper.writeValue(file, users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
