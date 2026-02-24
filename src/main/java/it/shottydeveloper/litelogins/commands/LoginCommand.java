package it.shottydeveloper.litelogins.commands;

import it.shottydeveloper.litelogins.LiteLogins;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class LoginCommand implements CommandExecutor {
    private final LiteLogins plugin;

    public LoginCommand(LiteLogins plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        UUID uuid = player.getUniqueId();
        String playerName = player.getName();

        if (args.length < 1) {
            player.sendMessage(plugin.getMessagesManager().get("messages.login_incorrect_syntax"));
            return true;
        }

        if (!plugin.getAuthManager().isPending(uuid)) {
            player.sendMessage(plugin.getMessagesManager().get("messages.already_logged_in"));
            return true;
        }

        final String inputPass = args[0];

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String savedHash = plugin.getDatabaseManager().getPasswordHash(playerName);

            if (savedHash == null) {
                player.sendMessage(plugin.getMessagesManager().get("messages.not_registered"));
                return;
            }

            if (BCrypt.checkpw(inputPass, savedHash)) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) {
                        plugin.getAuthManager().removePlayer(uuid);
                        player.removePotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS);
                        player.setWalkSpeed(0.2f);
                        player.sendMessage(plugin.getMessagesManager().get("messages.login_successful"));
                    }
                });
            } else {
                player.sendMessage(plugin.getMessagesManager().get("messages.wrong_password"));
            }
        });

        return true;
    }
}