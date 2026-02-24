package it.shottydeveloper.litelogins.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.shottydeveloper.litelogins.LiteLogins;
import it.shottydeveloper.litelogins.database.migration.SchemaInitializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        HikariConfig config = new HikariConfig();

        String host = cfg.getHostName();
        int port = cfg.getDatabasePort();
        String database = cfg.getDatabaseName();
        String username = cfg.getDatabaseUser();
        String password = cfg.getDatabasePass();
        int poolSize = cfg.getPoolSize();
        long connectionTimeout = cfg.getConnectionTimeOut();
        String dbType = cfg.getDatabaseType();
        String jdbcPrefix = "mysql".equals(dbType) ? "jdbc:mysql" : "jdbc:mariadb";
        String jdbcUrl = String.format(
                "%s://%s:%d/%s?useSSL=false&characterEncoding=UTF-8&autoReconnect=true",
                jdbcPrefix, host, port, database
        );
        if ("mariadb".equals(dbType)) {
            config.setDriverClassName("org.mariadb.jdbc.Driver");
        } else {
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        }
        config.setJdbcUrl(jdbcUrl);

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(poolSize);
        config.setConnectionTimeout(connectionTimeout);
        config.setPoolName("SuperClans-Pool");

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        try {
            dataSource = new HikariDataSource(config);

            try (Connection testConn = getConnection()) {
                if (testConn != null && !testConn.isClosed()) {
                    String dbLabel = "mysql".equals(dbType) ? "MySQL" : "MariaDB";
                    plugin.getLogger().info("Connessione a " + dbLabel + " riuscita!");
                }
            }

            SchemaInitializer schemaInitializer = new SchemaInitializer(this, tablePrefix);
            schemaInitializer.initialize();

            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Errore nella connessione al database: " + e.getMessage());
            return false;
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Il pool di connessioni non è disponibile!");
        }
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Pool di connessioni chiuso correttamente.");
        }
    }

    public boolean isConnected() {
        if (dataSource == null || dataSource.isClosed()) return false;
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public String getPasswordHash(String playerName) {
        String sql = "SELECT player_password FROM `" + tablePrefix + "users` WHERE player_name = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, playerName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("player_password");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Errore durante il recupero della password: " + e.getMessage());
        }
        return null;
    }
    public void saveUser(java.util.UUID uuid, String name, String hashedPassword) {
        String sql = "INSERT INTO `" + tablePrefix + "users` (player_uuid, player_name, player_password, last_login, registration_date) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, name);
            pstmt.setString(3, hashedPassword);
            pstmt.setLong(4, System.currentTimeMillis());
            pstmt.setLong(5, System.currentTimeMillis());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Errore critico durante il salvataggio dell'utente " + name + "!");
            e.printStackTrace();
        }
    }

    public String getTablePrefix() {
        return tablePrefix;
    }
    public LiteLogins getPlugin() {
        return plugin;
    }
}