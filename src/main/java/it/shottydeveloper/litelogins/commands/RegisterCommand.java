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

public class RegisterCommand implements CommandExecutor {

    private final LiteLogins plugin;

    public RegisterCommand(LiteLogins plugin) {
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

        AuthUser authUser = plugin.getAuthManager().getUser(uuid);
        if (authUser == null) {
            player.sendMessage(plugin.getMessagesManager().get("messages.internal_error"));
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (authUser.getPasswordHash() != null) {
                syncMessage(player, "messages.already_registered");
                return;
            }

            try {
                String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
                plugin.getDatabaseManager().saveUser(uuid, player.getName(), hashed);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()) return;
                    float previousSpeed = plugin.getAuthManager().popStoredSpeed(uuid);
                    authUser.setPasswordHash(hashed);
                    authUser.setAuthenticated(true);
                    player.removePotionEffect(PotionEffectType.BLINDNESS);
                    player.setWalkSpeed(previousSpeed);
                    player.sendMessage(plugin.getMessagesManager().get("messages.registration_completed"));
                });
            } catch (Exception e) {
                plugin.getLogger().severe("Database Error durante registrazione: " + e.getMessage());
                player.sendMessage(plugin.getMessagesManager().get("messages.internal_error"));
            }
        });

        return true;
    }
    private void syncMessage(Player player, String messageKey) {
        Bukkit.getScheduler().runTask(plugin, () -> {
          if (player.isOnline()) {
              player.sendMessage(plugin.getMessagesManager().get(messageKey));
          }
        });
    }
}
