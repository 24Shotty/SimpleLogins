package it.shottydeveloper.litelogins.listeners;

import it.shottydeveloper.litelogins.LiteLogins;
import it.shottydeveloper.litelogins.models.AuthUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class AuthListener implements Listener {

    private final LiteLogins plugin;

    public AuthListener(LiteLogins plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        plugin.getAuthManager().storeSpeed(uuid, player.getWalkSpeed());

        AuthUser authUser = new AuthUser(uuid, player.getName(), null);
        plugin.getAuthManager().loadUser(authUser);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String hash = plugin.getDatabaseManager().getPasswordHash(uuid);

            authUser.setPasswordHash(hash);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;

                player.setWalkSpeed(0f);
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0, false, false));

                Location spawn = player.getWorld().getSpawnLocation();
                spawn.setY(player.getWorld().getHighestBlockYAt(spawn) + 1.2);
                player.teleport(spawn);

                if (hash == null) {
                    player.sendMessage(plugin.getMessagesManager().get("messages.register_welcome"));
                } else {
                    player.sendMessage(plugin.getMessagesManager().get("messages.login_welcome"));
                }
            });
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getAuthManager().unloadUser(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getAuthManager().isPending(player.getUniqueId())) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            event.setTo(from.clone().setDirection(to.getDirection()));
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!plugin.getAuthManager().isPending(event.getPlayer().getUniqueId())) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(plugin.getMessagesManager().get("messages.chat_blocked"));
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.getAuthManager().isPending(event.getPlayer().getUniqueId())) return;

        String message = event.getMessage().toLowerCase();
        if (!message.startsWith("/login") && !message.startsWith("/register")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessagesManager().get("messages.command_blocked"));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (plugin.getAuthManager().isPending(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {
        if (plugin.getAuthManager().isPending(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (!plugin.getAuthManager().isPending(event.getPlayer().getUniqueId())) return;
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (plugin.getAuthManager().isPending(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (plugin.getAuthManager().isPending(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (plugin.getAuthManager().isPending(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (plugin.getAuthManager().isPending(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (plugin.getAuthManager().isPending(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (plugin.getAuthManager().isPending(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!plugin.getAuthManager().isPending(event.getWhoClicked().getUniqueId())) return;
        event.setCancelled(true);
        Bukkit.getScheduler().runTask(plugin, event.getWhoClicked()::closeInventory);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!plugin.getAuthManager().isPending(player.getUniqueId())) return;
        event.setCancelled(true);
        Bukkit.getScheduler().runTask(plugin, player::closeInventory);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (plugin.getAuthManager().isPending(event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (plugin.getAuthManager().isPending(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}