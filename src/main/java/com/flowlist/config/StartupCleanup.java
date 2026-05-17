package com.flowlist.config;

import com.flowlist.repository.PushSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs once on startup to purge invalid / mock push subscriptions from the database.
 * Without this, stale test records cause every push attempt to fail with crypto errors.
 */
@Component
public class StartupCleanup implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupCleanup.class);
    private final PushSubscriptionRepository subRepo;

    public StartupCleanup(PushSubscriptionRepository subRepo) {
        this.subRepo = subRepo;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Delete any test/mock subscription endpoints that have invalid crypto keys
        subRepo.findAll().stream()
            .filter(s -> s.getEndpoint() != null && s.getEndpoint().contains("mock"))
            .forEach(s -> {
                log.info("Removing mock push subscription: {}", s.getEndpoint());
                subRepo.delete(s);
            });
    }
}
