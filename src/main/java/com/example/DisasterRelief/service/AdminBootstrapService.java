package com.example.DisasterRelief.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Bootstraps an ADMIN account on first start-up if the {@code app.admin.*}
 * properties are configured.  This is a one-time operation: if a user with
 * the configured username already exists, no action is taken.
 *
 * <p>Example {@code application.properties} snippet:
 * <pre>
 * app.admin.username=admin
 * app.admin.email=admin@example.com
 * app.admin.password=ChangeMe123!
 * </pre>
 *
 * <p>You can also supply these values via environment variables
 * (Spring Boot's relaxed binding converts {@code APP_ADMIN_PASSWORD} →
 * {@code app.admin.password}), which avoids committing credentials to source control.
 */
@Service
public class AdminBootstrapService {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapService.class);

    @Value("${app.admin.username:#{null}}")
    private String adminUsername;

    @Value("${app.admin.email:#{null}}")
    private String adminEmail;

    @Value("${app.admin.password:#{null}}")
    private String adminPassword;

    private final UserService userService;

    public AdminBootstrapService(UserService userService) {
        this.userService = userService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void bootstrapAdmin() {
        if (adminUsername == null || adminEmail == null || adminPassword == null) {
            log.debug("Admin bootstrap skipped – app.admin.* properties are not configured.");
            return;
        }

        if (userService.getUserByUsername(adminUsername).isPresent()) {
            log.info("Admin bootstrap skipped – user '{}' already exists.", adminUsername);
            return;
        }

        userService.createUser(adminUsername, adminEmail, "ADMIN", adminPassword);
        log.info("Admin account '{}' created successfully.", adminUsername);
    }
}
