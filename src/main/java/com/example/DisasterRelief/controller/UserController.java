package com.example.DisasterRelief.controller;

import com.example.DisasterRelief.Entity.User;
import com.example.DisasterRelief.dto.CreateUserRequest;
import com.example.DisasterRelief.dto.UpdateUserRequest;
import com.example.DisasterRelief.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Strip the hashed password before sending a user object to the client. */
    private Map<String, String> toResponse(User u) {
        return Map.of(
                "id", u.getId(),
                "username", u.getUsername(),
                "email", u.getEmail(),
                "role", u.getRole()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getAllUsers() {
        List<Map<String, String>> users = userService.getAllUsers().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, String>> getUserById(@PathVariable String id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(u -> ResponseEntity.ok(toResponse(u)))
                   .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Self-registration endpoint – open to unauthenticated callers.
     * The {@code password} field is required; the role is fixed to {@code USER}
     * (only an ADMIN can create ADMIN accounts via a separate flow).
     */
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest body) {
        String username = HtmlUtils.htmlEscape(body.getUsername());
        String email = HtmlUtils.htmlEscape(body.getEmail());

        User created = userService.createUser(username, email, "USER", body.getPassword());
        return ResponseEntity.ok(toResponse(created));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id,
                                        @Valid @RequestBody UpdateUserRequest body) {
        String username = HtmlUtils.htmlEscape(body.getUsername());
        String email = HtmlUtils.htmlEscape(body.getEmail());
        String role = HtmlUtils.htmlEscape(body.getRole());

        Optional<User> updated = userService.updateUser(id, username, email, role);
        return updated.map(u -> ResponseEntity.ok(toResponse(u)))
                      .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }
}
