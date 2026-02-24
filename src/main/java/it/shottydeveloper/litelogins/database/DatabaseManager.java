package it.shottydeveloper.litelogins.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.shottydeveloper.litelogins.LiteLogins;
import it.shottydeveloper.litelogins.database.migration.SchemaInitializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {

    private final LiteLogins plugin;
    private HikariDataSource dataSource;
    private String tablePrefix;

    public DatabaseManager(LiteLogins plugin) {
        this.plugin = plugin;
    }

    public boolean initialize() {
        var cfg = plugin.getConfigManager();
        this.tablePrefix = cfg.getTablePrefix();

        try {
            HikariConfig config = new HikariConfig();

            String dbType = cfg.getDatabaseType();
            boolean isMariaDb = "mariadb".equals(dbType);
            String driverClass = isMariaDb
                    ? "it.shottydeveloper.litelogins.libs.mariadb.jdbc.Driver"
                    : "it.shottydeveloper.litelogins.libs.mysql.cj.jdbc.Driver";
            String jdbcPrefix = isMariaDb ? "jdbc:mariadb" : "jdbc:mysql";
            String jdbcUrl = String.format(
                    "%s://%s:%d/%s?useSSL=false&characterEncoding=UTF-8&autoReconnect=true&serverTimezone=UTC",
                    jdbcPrefix, cfg.getHostName(), cfg.getDatabasePort(), cfg.getDatabaseName()
            );

            Class.forName(driverClass);

            config.setJdbcUrl(jdbcUrl);
            config.setDriverClassName(driverClass);
            config.setUsername(cfg.getDatabaseUser());
            config.setPassword(cfg.getDatabasePass());
            config.setMaximumPoolSize(cfg.getPoolSize());
            config.setConnectionTimeout(cfg.getConnectionTimeout());
            config.setPoolName("LiteLogins-Pool");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            this.dataSource = new HikariDataSource(config);

            try (Connection conn = dataSource.getConnection()) {
                plugin.getLogger().info("Connessione al database (" + dbType + ") stabilita con successo.");
            }

            new SchemaInitializer(this, tablePrefix).initialize();
            return true;

        } catch (ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "Driver JDBC non trovato: " + e.getMessage(), e);
            return false;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Impossibile inizializzare il database!", e);
            return false;
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("DataSource non disponibile.");
        }
        return dataSource.getConnection();
    }

    public String getPasswordHash(UUID uuid) {
        String sql = "SELECT player_password FROM `" + tablePrefix + "users` WHERE player_uuid = ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("player_password");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Errore query getPasswordHash per UUID: " + uuid, e);
        }
        return null;
    }

    public void saveUser(UUID uuid, String name, String hashedPassword) {
        String sql = "INSERT INTO `" + tablePrefix + "users` " +
                "(player_uuid, player_name, player_password, last_login, registration_date) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE player_name = ?, player_password = ?, last_login = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            long now = System.currentTimeMillis();
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, name);
            pstmt.setString(3, hashedPassword);
            pstmt.setLong(4, now);
            pstmt.setLong(5, now);
            pstmt.setString(6, name);
            pstmt.setString(7, hashedPassword);
            pstmt.setLong(8, now);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Errore salvataggio utente: " + name, e);
        }
    }

    public void updateLastLogin(UUID uuid) {
        String sql = "UPDATE `" + tablePrefix + "users` SET last_login = ? WHERE player_uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, System.currentTimeMillis());
            pstmt.setString(2, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Errore aggiornamento last_login per UUID: " + uuid, e);
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public LiteLogins getPlugin() {
        return plugin;
    }
}
