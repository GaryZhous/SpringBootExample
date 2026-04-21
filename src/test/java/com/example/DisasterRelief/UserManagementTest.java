package com.example.DisasterRelief;

import com.example.DisasterRelief.Entity.User;
import com.example.DisasterRelief.repository.UserRepository;
import com.example.DisasterRelief.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserManagementTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    @AfterEach
    void cleanup() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        userRepository.deleteAll();
    }

    @Test
    void createUserReturnsPersistentUser() {
        User user = userService.createUser("alice", "alice@example.com", "USER");

        assertNotNull(user.getId(), "id should be assigned");
        assertEquals("alice", user.getUsername());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("USER", user.getRole());

        List<User> all = userService.getAllUsers();
        assertEquals(1, all.size());
    }

    @Test
    void getAllUsersIsCached() {
        userService.createUser("bob", "bob@example.com", "USER");
        userService.getAllUsers();

        assertNotNull(cacheManager.getCache("users").get(SimpleKey.EMPTY),
                "users cache should be populated after first call");
    }

    @Test
    void createUserEvictsCache() {
        userService.getAllUsers();
        assertNotNull(cacheManager.getCache("users").get(SimpleKey.EMPTY));

        userService.createUser("carol", "carol@example.com", "ADMIN");

        assertNull(cacheManager.getCache("users").get(SimpleKey.EMPTY),
                "users cache should be evicted after create");
    }

    @Test
    void getUserByIdReturnsCorrectUser() {
        User created = userService.createUser("dave", "dave@example.com", "USER");

        Optional<User> found = userService.getUserById(created.getId());

        assertTrue(found.isPresent());
        assertEquals("dave", found.get().getUsername());
    }

    @Test
    void getUserByIdReturnsEmptyForMissingId() {
        Optional<User> found = userService.getUserById("nonexistent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void updateUserModifiesFields() {
        User created = userService.createUser("eve", "eve@example.com", "USER");

        Optional<User> updated = userService.updateUser(created.getId(), "eve-updated", "eve2@example.com", "ADMIN");

        assertTrue(updated.isPresent());
        assertEquals("eve-updated", updated.get().getUsername());
        assertEquals("eve2@example.com", updated.get().getEmail());
        assertEquals("ADMIN", updated.get().getRole());
    }

    @Test
    void updateNonExistentUserReturnsEmpty() {
        Optional<User> updated = userService.updateUser("no-such-id", "x", "x@x.com", "USER");
        assertFalse(updated.isPresent());
    }

    @Test
    void deleteUserRemovesFromPersistence() {
        User created = userService.createUser("frank", "frank@example.com", "USER");

        boolean deleted = userService.deleteUser(created.getId());

        assertTrue(deleted);
        assertTrue(userService.getAllUsers().isEmpty());
    }

    @Test
    void deleteNonExistentUserReturnsFalse() {
        boolean deleted = userService.deleteUser("no-such-id");
        assertFalse(deleted);
    }

    @Test
    void deleteUserEvictsCache() {
        User created = userService.createUser("grace", "grace@example.com", "USER");
        userService.getAllUsers();
        assertNotNull(cacheManager.getCache("users").get(SimpleKey.EMPTY));

        userService.deleteUser(created.getId());

        assertNull(cacheManager.getCache("users").get(SimpleKey.EMPTY),
                "users cache should be evicted after delete");
    }
}
