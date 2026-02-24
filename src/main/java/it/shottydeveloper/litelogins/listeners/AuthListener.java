package it.shottydeveloper.litelogins.listeners;

import it.shottydeveloper.litelogins.LiteLogins;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AuthListener implements Listener {
    private final LiteLogins plugin;

    public AuthListener(LiteLogins plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getAuthManager().addPlayer(player.getUniqueId());

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0, false, false));
            Location spawn = player.getWorld().getSpawnLocation();
            spawn.setY(player.getWorld().getHighestBlockYAt(spawn) + 1);
            player.teleport(spawn);
        }, 1L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getAuthManager().removePlayer(event.getPlayer().getUniqueId());
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
        if (plugin.getAuthManager().isPending(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cDevi autenticarti prima di scrivere in chat!");
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (plugin.getAuthManager().isPending(event.getPlayer().getUniqueId())) {
            String message = event.getMessage().toLowerCase();
            if (!message.startsWith("/login") && !message.startsWith("/register")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cUsa /login o /register per continuare.");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
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
        if (plugin.getAuthManager().isPending(event.getPlayer().getUniqueId())) {
            Location to = event.getTo();
            if (to == null) return;
            Location spawnLocation = to.getWorld().getSpawnLocation();
            if (to.distanceSquared(spawnLocation) < 16) {
                return;
            }
            if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN ||
                    event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            if (plugin.getAuthManager().isPending(event.getDamager().getUniqueId())) {
                event.setCancelled(true);
            }
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
    public void onDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (plugin.getAuthManager().isPending(event.getEntity().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if(plugin.getAuthManager().isPending(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            if (plugin.getAuthManager().isPending(event.getEntity().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (plugin.getAuthManager().isPending(event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
            plugin.getServer().getScheduler().runTask(plugin, event.getWhoClicked()::closeInventory);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if(!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();

        if (plugin.getAuthManager().isPending(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getServer().getScheduler().runTask(plugin, () -> player.closeInventory());
        }
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