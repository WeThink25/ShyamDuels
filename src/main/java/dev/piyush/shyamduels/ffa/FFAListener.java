package dev.piyush.shyamduels.ffa;

import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.arena.Arena;
import dev.piyush.shyamduels.kit.Kit;
import dev.piyush.shyamduels.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FFAListener implements Listener {

    private final ShyamDuels plugin;
    private final FFAManager ffaManager;
    private final Map<UUID, Long> actionBarCooldowns = new ConcurrentHashMap<>();
    private static final long ACTIONBAR_COOLDOWN_MS = 2000;

    public FFAListener(ShyamDuels plugin, FFAManager ffaManager) {
        this.plugin = plugin;
        this.ffaManager = ffaManager;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (ffaManager.getPlayerState(player) == FFAManager.FFAState.IN_FFA) {
            boolean dropsEnabled = plugin.getConfig().getBoolean("item-drops.enabled", false);

            if (dropsEnabled && player.getKiller() != null) {
                Player killer = player.getKiller();
                if (ffaManager.getPlayerState(killer) == FFAManager.FFAState.IN_FFA) {
                    java.util.List<org.bukkit.inventory.ItemStack> drops = new java.util.ArrayList<>(e.getDrops());
                    final String victimName = player.getName();

                    int delay = plugin.getConfig().getInt("item-drops.looting-delay", 5);
                    MessageUtils.sendMessage(killer,
                            "&eLooting " + victimName + "'s items in " + delay + " seconds...");

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (killer.isOnline()) {
                            new dev.piyush.shyamduels.gui.LootGui(plugin, killer, victimName, drops).open(killer);
                        }
                    }, delay * 20L);
                }
            }

            e.getDrops().clear();
            e.setDroppedExp(0);

            Bukkit.getScheduler().runTaskLater(plugin, () -> player.spigot().respawn(), 1L);
            Bukkit.getScheduler().runTaskLater(plugin, () -> ffaManager.handleDeath(player), 2L);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (ffaManager.getPlayerState(e.getPlayer()) != FFAManager.FFAState.NONE) {
            ffaManager.leaveFFA(e.getPlayer());
        }
        actionBarCooldowns.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p) {
            if (ffaManager.getPlayerState(p) == FFAManager.FFAState.FFA_STARTING) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (ffaManager.getPlayerState(e.getPlayer()) == FFAManager.FFAState.FFA_STARTING) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        FFAManager.FFAState state = ffaManager.getPlayerState(e.getPlayer());
        if (state != FFAManager.FFAState.NONE) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (ffaManager.getPlayerState(e.getPlayer()) == FFAManager.FFAState.FFA_STARTING) {
            if (e.getTo().getBlockX() != e.getFrom().getBlockX()
                    || e.getTo().getBlockZ() != e.getFrom().getBlockZ()) {
                org.bukkit.Location newLoc = e.getFrom();
                newLoc.setPitch(e.getTo().getPitch());
                newLoc.setYaw(e.getTo().getYaw());
                e.setTo(newLoc);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        FFAManager.FFAState state = ffaManager.getPlayerState(e.getPlayer());
        if (state != FFAManager.FFAState.NONE) {
            String cmd = e.getMessage().toLowerCase();

            if (cmd.startsWith("/ffa leave"))
                return;
            if (cmd.startsWith("/leavefight"))
                return;
            if (cmd.startsWith("/leave"))
                return;
            if (cmd.startsWith("/spawn"))
                return;
            if (e.getPlayer().hasPermission("shyamduels.admin"))
                return;

            e.setCancelled(true);
            e.getPlayer()
                    .sendMessage(MessageUtils.color("&cYou cannot use commands in FFA. Use /leavefight or /spawn."));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        FFAManager.FFAState state = ffaManager.getPlayerState(e.getPlayer());
        if (state == FFAManager.FFAState.NONE)
            return;

        if (state == FFAManager.FFAState.FFA_STARTING) {
            e.setCancelled(true);
            return;
        }

        Arena arena = ffaManager.getPlayerArena(e.getPlayer());
        if (arena == null || !arena.isBuildEnabled()) {
            e.setCancelled(true);
            sendCooldownActionBar(e.getPlayer(), "duel.build.disabled");
            return;
        }

        Kit kit = ffaManager.getPlayerKit(e.getPlayer());
        if (kit != null && !kit.getBuildWhitelist().isEmpty()
                && !kit.getBuildWhitelist().contains(e.getBlock().getType())) {
            e.setCancelled(true);
            sendCooldownActionBar(e.getPlayer(), "duel.build.restricted");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        FFAManager.FFAState state = ffaManager.getPlayerState(e.getPlayer());
        if (state == FFAManager.FFAState.NONE)
            return;

        if (state == FFAManager.FFAState.FFA_STARTING) {
            e.setCancelled(true);
            return;
        }

        Arena arena = ffaManager.getPlayerArena(e.getPlayer());
        if (arena == null || !arena.isBuildEnabled()) {
            e.setCancelled(true);
            sendCooldownActionBar(e.getPlayer(), "duel.build.disabled");
            return;
        }

        Kit kit = ffaManager.getPlayerKit(e.getPlayer());
        if (kit != null && !kit.getBuildWhitelist().isEmpty()
                && !kit.getBuildWhitelist().contains(e.getBlock().getType())) {
            e.setCancelled(true);
            sendCooldownActionBar(e.getPlayer(), "duel.build.restricted");
        }
    }

    private void sendCooldownActionBar(Player player, String path) {
        long now = System.currentTimeMillis();
        Long last = actionBarCooldowns.get(player.getUniqueId());
        if (last == null || now - last > ACTIONBAR_COOLDOWN_MS) {
            actionBarCooldowns.put(player.getUniqueId(), now);
            MessageUtils.sendActionBar(player, path, Map.of());
        }
    }
}
