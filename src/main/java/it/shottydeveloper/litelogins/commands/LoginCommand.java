package it.shottydeveloper.litelogins.commands;

import it.shottydeveloper.litelogins.LiteLogins;
import it.shottydeveloper.litelogins.models.AuthUser;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
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

        if (!plugin.getAuthManager().isPending(uuid)) {
            player.sendMessage(plugin.getMessagesManager().get("messages.already_logged_in"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getMessagesManager().get("messages.login_incorrect_syntax"));
            return true;
        }

        AuthUser authUser = plugin.getAuthManager().getUser(uuid);
        if (authUser == null) {
            player.sendMessage(plugin.getMessagesManager().get("messages.internal_error"));
            return true;
        }

        String inputPass = args[0];

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String savedHash = authUser.getPasswordHash();

            if (savedHash == null) {
                player.sendMessage(plugin.getMessagesManager().get("messages.not_registered"));
                return;
            }

            if (!BCrypt.checkpw(inputPass, savedHash)) {
                player.sendMessage(plugin.getMessagesManager().get("messages.wrong_password"));
                return;
            }

            plugin.getDatabaseManager().updateLastLogin(uuid);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;
                float previousSpeed = plugin.getAuthManager().popStoredSpeed(uuid);
                authUser.setAuthenticated(true);
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                player.setWalkSpeed(previousSpeed);
                player.sendMessage(plugin.getMessagesManager().get("messages.login_successful"));
            });
        });

        return true;
    }
}