package com.example.DisasterRelief.service;

import com.example.DisasterRelief.Entity.User;
import com.example.DisasterRelief.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Cacheable("users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @CacheEvict(value = "users", allEntries = true)
    public User createUser(String username, String email, String role) {
        return createUser(username, email, role, null);
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public User createUser(String username, String email, String role, String rawPassword) {
        String hashedPassword = (rawPassword != null && !rawPassword.isBlank())
                ? passwordEncoder.encode(rawPassword)
                : null;
        User user = new User(UUID.randomUUID().toString(), username, email, role, hashedPassword);
        return userRepository.save(user);
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public Optional<User> updateUser(String id, String username, String email, String role) {
        return userRepository.findById(id).map(u -> {
            u.setUsername(username);
            u.setEmail(email);
            u.setRole(role);
            return userRepository.save(u);
        });
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public boolean deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }
}

