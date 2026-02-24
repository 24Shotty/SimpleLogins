package it.shottydeveloper.litelogins.config;

import it.shottydeveloper.litelogins.LiteLogins;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final LiteLogins plugin;
    private FileConfiguration config;

    public ConfigManager(LiteLogins plugin) {
        this.plugin = plugin;
        reload();
    }
    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    public String getDatabaseType() {
        return config.getString("database.type", "mysql").toLowerCase();
    }
    public  String getHostName() {
        return config.getString("database.host", "localhost");
    }
    public int getDatabasePort() {
        return config.getInt("database.port", 3306);
    }
    public String getDatabaseName() {
        return config.getString("database.name", "litelogins");
    }
    public String getDatabaseUser() {
        return config.getString("database.username", "root");
    }
    public String getDatabasePass() {
        return config.getString("database.password", "password");
    }
    public String getTablePrefix() {
        return config.getString("database.table_prefix", "lg_");
    }
    public int getPoolSize() {
        return config.getInt("database.pool_size", 10);
    }
    public int getConnectionTimeOut() {
        return config.getInt("database.connection_timeout", 30000);
    }
    public int getMinPassLength() {
        return config.getInt("settings.password_min_length", 6);
    }
}