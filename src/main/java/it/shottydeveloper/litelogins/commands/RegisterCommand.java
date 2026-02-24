package it.shottydeveloper.litelogins.commands;

import it.shottydeveloper.litelogins.LiteLogins;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class RegisterCommand implements CommandExecutor {
    private final LiteLogins plugin;

    public RegisterCommand(LiteLogins plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        UUID uuid = player.getUniqueId();
        String playerName = player.getName();

        if (!plugin.getAuthManager().isPending(uuid)) {
            player.sendMessage(plugin.getMessagesManager().get("messages.already_logged_in"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getMessagesManager().get("messages.register_incorrect_syntax"));
            return true;
        }

        String password = args[0];
        String confirm = args[1];

        if (password.length() < plugin.getConfigManager().getMinPassLength()) {
            player.sendMessage(plugin.getMessagesManager().get("messages.register_password_too_short"));
            return true;
        }

        if (!password.equals(confirm)) {
            player.sendMessage(plugin.getMessagesManager().get("messages.password_no_match"));
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (plugin.getDatabaseManager().getPasswordHash(playerName) != null) {
                player.sendMessage(plugin.getMessagesManager().get("messages.already_registered"));
                return;
            }

            try {
                String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
                plugin.getDatabaseManager().saveUser(uuid, playerName, hashed);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) {
                        plugin.getAuthManager().removePlayer(uuid);
                        player.removePotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS);
                        player.setWalkSpeed(0.2f);
                        player.sendMessage(plugin.getMessagesManager().get("messages.registration_completed"));
                    }
                });
            } catch (Exception e) {
                plugin.getLogger().severe("Database Error: " + e.getMessage());
                player.sendMessage(plugin.getMessagesManager().get("messages.internal_error"));
            }
        });

        return true;
    }
}