package ru.company.booking;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционный тест на реальной PostgreSQL (Testcontainers): миграции Flyway применяются
 * без ошибок, а схема поддерживает бронирование с участниками и каскадное удаление.
 * Без Docker тест пропускается.
 */
@Testcontainers(disabledWithoutDocker = true)
class FlywayMigrationPostgresTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine");

    @Test
    void migrationsApplyAndSchemaWorksOnPostgres() throws Exception {
        MigrateResult result = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();

        assertThat(result.migrationsExecuted).isGreaterThanOrEqualTo(2);

        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("INSERT INTO bookings (id, room_id, user_id, title, start_time, end_time, status) "
                    + "VALUES ('b-1', 'room-1', 'user-1', 'Planning', now() + interval '1 day', "
                    + "now() + interval '1 day 1 hour', 'CONFIRMED')");
            statement.executeUpdate("INSERT INTO booking_participants (booking_id, participant_id) VALUES ('b-1', 'user-2')");

            try (ResultSet rs = statement.executeQuery(
                    "SELECT count(*) FROM booking_participants WHERE booking_id = 'b-1'")) {
                rs.next();
                assertThat(rs.getInt(1)).isEqualTo(1);
            }

            statement.executeUpdate("DELETE FROM bookings WHERE id = 'b-1'");

            try (ResultSet rs = statement.executeQuery(
                    "SELECT count(*) FROM booking_participants WHERE booking_id = 'b-1'")) {
                rs.next();
                assertThat(rs.getInt(1)).isZero();
            }
        }
    }
}
