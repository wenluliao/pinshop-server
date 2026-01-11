package com.flashbuy.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Database Schema Initializer
 * Executes schema-tables.sql on first run if tables don't exist
 */
@Component
@Order(0)  // Run before DataInitializer
@ConditionalOnBean(DataSource.class)
public class SchemaInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaInitializer.class);

    private final DataSource dataSource;

    @Value("classpath:schema-init.sql")
    private Resource schemaResource;

    public SchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("========================================");
        log.info("SchemaInitializer starting...");
        log.info("========================================");

        try (Connection conn = dataSource.getConnection()) {
            log.info("✓ Got database connection");

            // Check if product_spu table exists
            try (var stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT 1 FROM product_spu LIMIT 1");
                log.info("✓ Database tables already exist, skipping schema initialization");
                return;
            } catch (Exception e) {
                log.info("→ Tables don't exist, will initialize schema...");
            }

            // Read and execute SQL file
            log.info("→ Reading schema-tables.sql...");
            String sql = readSchemaFile();
            log.info("→ Executing schema SQL...");
            executeSchemaSql(conn, sql);

            log.info("✓ Schema initialized successfully!");
            log.info("========================================");

        } catch (Exception e) {
            log.error("✗ Failed to initialize schema", e);
            throw e;
        }
    }

    private String readSchemaFile() throws IOException {
        try {
            var is = schemaResource.getInputStream();
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            log.info("Loaded schema-tables.sql ({} bytes)", content.length());
            return content;
        } catch (Exception e) {
            log.error("Failed to read schema-tables.sql", e);
            throw e;
        }
    }

    private void executeSchemaSql(Connection conn, String sql) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            // Split by semicolon and execute each statement
            String[] statements = sql.split(";");
            int executed = 0;

            for (String statement : statements) {
                String trimmed = statement.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    continue;
                }

                try {
                    stmt.execute(trimmed);
                    executed++;
                } catch (Exception e) {
                    log.warn("Failed to execute statement (continuing): {}",
                            trimmed.substring(0, Math.min(100, trimmed.length())));
                }
            }

            log.info("Executed {} SQL statements", executed);
        }
    }
}
