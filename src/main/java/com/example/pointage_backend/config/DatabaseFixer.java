package com.example.pointage_backend.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseFixer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseFixer.class);
    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void fixConstraints() {
        try {
            logger.info("Attempting to drop problematic FK constraint: pointages_valide_par_foreign");
            jdbcTemplate.execute("ALTER TABLE pointages DROP CONSTRAINT IF EXISTS pointages_valide_par_foreign");
            logger.info("FK constraint dropped successfully (or did not exist).");
        } catch (Exception e) {
            logger.warn("Failed to drop constraint pointages_valide_par_foreign. Legacy DB schema might need manual fix: {}", e.getMessage());
        }
    }
}
