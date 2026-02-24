package it.shottydeveloper.litelogins.config;

import it.shottydeveloper.litelogins.LiteLogins;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.bukkit.ChatColor;

public class MessagesManager {
    private final LiteLogins plugin;
    private FileConfiguration messages;
    private String prefix;

    public MessagesManager(LiteLogins plugin) {
        this.plugin = plugin;
        reload();
    }
    public String getLoginIncorrectSyntax() {
        return messages.getString("messages.login_incorrect_syntax", "Uso corretto: /login <tuaPassword> <ripetiTuaPassword>!");
    }
    public void reload() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            messages.setDefaults(defaults);
        }
        prefix = color(messages.getString("prefix", "&8[&5LiteLogins]"));
    }
    public String get(String path) {
        String raw = messages.getString(path, "&cMessaggio non trovato: " + path);
        return prefix + color(raw);
    }
    public String getRaw(String path) {
        String raw = messages.getString(path, "&cMessaggio non trovato: " + path);
        return color(raw);
    }
    public String get(String path, String... replacements) {
        String message = get(path);

        for (int i = 0; i + 1 < replacements.length; i+= 2) {
            message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
        }
        return message;
    }
    public String getRaw(String path, String... replacements) {
        String message = getRaw(path);

        for (int i = 0; i + 1 < replacements.length; i += 2) {
            message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
        }

        return message;
    }

    public String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public String getPrefix() {
        return prefix;
    }
}
