package it.shottydeveloper.litelogins;

import it.shottydeveloper.litelogins.commands.LoginCommand;
import it.shottydeveloper.litelogins.commands.RegisterCommand;
import it.shottydeveloper.litelogins.config.ConfigManager;
import it.shottydeveloper.litelogins.config.MessagesManager;
import it.shottydeveloper.litelogins.database.DatabaseManager;
import it.shottydeveloper.litelogins.listeners.AuthListener;
import it.shottydeveloper.litelogins.managers.AuthManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LiteLogins extends JavaPlugin {

    private MessagesManager messagesManager;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private AuthManager authManager;

    @Override
    public void onEnable() {
        if (!loadConfigurations()) {
            disablePlugin("Errore critico nel caricamento delle configurazioni!");
            return;
        }

        if (!initDatabase()) {
            disablePlugin("Impossibile connettersi al Database! Controlla config.yml");
            return;
        }

        this.authManager = new AuthManager();

        registerCommands();
        getServer().getPluginManager().registerEvents(new AuthListener(this), this);

        getLogger().info("LiteLogins abilitato correttamente!");
    }

    private boolean loadConfigurations() {
        try {
            saveDefaultConfig();
            this.configManager = new ConfigManager(this);
            this.messagesManager = new MessagesManager(this);
            getLogger().info("Files di configurazione caricati correttamente!");
            return true;
        } catch (Exception e) {
            getLogger().severe("Errore nel caricamento dei Files di configurazione: " + e.getMessage());
            return false;
        }
    }

    private boolean initDatabase() {
        try {
            this.databaseManager = new DatabaseManager(this);
            return databaseManager.initialize();
        } catch (Exception e) {
            getLogger().severe("Errore nella connessione al Database: " + e.getMessage());
            return false;
        }
    }

    private void registerCommands() {
        try {
            getCommand("login").setExecutor(new LoginCommand(this));
            getCommand("register").setExecutor(new RegisterCommand(this));
        } catch (NullPointerException e) {
            getLogger().warning("Comandi non trovati nel plugin.yml!");
        }
    }

    private void disablePlugin(String message) {
        getLogger().severe(message);
        getServer().getPluginManager().disablePlugin(this);
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("LiteLogins disabilitato!");
    }

    public MessagesManager getMessagesManager() { return messagesManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public AuthManager getAuthManager() { return authManager; }
}