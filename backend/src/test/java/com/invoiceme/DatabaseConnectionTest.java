package com.invoiceme;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DatabaseConnectionTest extends BaseIntegrationTest {

    @Test
    void shouldConnectToDatabase() {
        // Test passes if database connection works
        assertTrue(BaseIntegrationTest.postgres.isRunning());
    }
}

