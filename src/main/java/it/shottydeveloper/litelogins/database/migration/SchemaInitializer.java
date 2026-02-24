package it.shottydeveloper.litelogins.database.migration;

import it.shottydeveloper.litelogins.database.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaInitializer {
    private final DatabaseManager databaseManager;
    private final String prefix;

    public SchemaInitializer(DatabaseManager databaseManager, String prefix) {
        this.databaseManager = databaseManager;
        this.prefix = prefix;
    }
    public void initialize() {
        try (Connection conn = databaseManager.getConnection()){
            createUsersTable(conn);
            databaseManager.getPlugin().getLogger().info("Schema del Database inizializzato con successo!");
        } catch (SQLException e) {
            databaseManager.getPlugin().getLogger().info("Errore nel'inizializzazione dello schema: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void createUsersTable(Connection conn) throws SQLException {
        String tableName = prefix + "users";

        String sql = """
            CREATE TABLE IF NOT EXISTS `%s` (
                `player_uuid` VARCHAR(36) NOT NULL,
                `player_name` VARCHAR(16) NOT NULL,
                `player_password` VARCHAR(255) NOT NULL,
                `last_login` BIGINT NOT NULL,
                `registration_date` BIGINT NOT NULL,
                PRIMARY KEY (`player_uuid`),
                UNIQUE KEY `uk_player_name` (`player_name`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """.formatted(tableName);

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }

    }
}
