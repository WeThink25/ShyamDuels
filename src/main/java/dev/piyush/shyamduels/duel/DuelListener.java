package dev.piyush.shyamduels.duel;

import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.arena.Arena;
import dev.piyush.shyamduels.stats.StatsManager;
import dev.piyush.shyamduels.util.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DuelListener implements Listener {

    private final ShyamDuels plugin;
    private final DuelManager duelManager;

    public DuelListener(ShyamDuels plugin) {
        this.plugin = plugin;
        this.duelManager = plugin.getDuelManager();
    }

    @EventHandler
    public void onDamage(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim) || !(event.getDamager() instanceof Player attacker)) {
            return;
        }

        Duel duel = duelManager.getDuel(victim);
        if (duel == null || !duel.isInProgress()) {
            return;
        }

        if (duel.isTeam1(victim) == duel.isTeam1(attacker)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player loser = event.getEntity();
        Duel duel = duelManager.getDuel(loser);
        if (duel == null)
            return;

        event.getDrops().clear();
        event.setDroppedExp(0);
        event.setKeepInventory(true);
        duel.handleDeath(loser);
        Player killer = loser.getKiller();
        StatsManager statsManager = plugin.getStatsManager();
        if (killer != null && duel.hasPlayer(killer)) {
            statsManager.recordKill(killer, loser);
        }
        statsManager.recordDeath(loser, killer);

        if (duel.isRoundOver()) {
            int winningTeam = duel.getWinningTeamNumber();
            duelManager.markEnding(duel, winningTeam);
        } else {
            loser.setGameMode(org.bukkit.GameMode.SPECTATOR);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                loser.spigot().respawn();
                if (duel.getArena().getCenter() != null) {
                    loser.teleport(duel.getArena().getCenter());
                }
                loser.setGameMode(org.bukkit.GameMode.SPECTATOR);
            });
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> loser.spigot().respawn());
    }

    @EventHandler
    public void onRespawn(org.bukkit.event.player.PlayerRespawnEvent event) {
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player loser = event.getPlayer();
        Duel duel = duelManager.getDuel(loser);
        if (duel == null)
            return;
        duel.handleDeath(loser);

        if (duel.isRoundOver()) {
            int winningTeam = duel.getWinningTeamNumber();
            duelManager.markEnding(duel, winningTeam);
        }

    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Duel duel = duelManager.getDuel(event.getPlayer());
        if (duel != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(org.bukkit.event.player.PlayerMoveEvent event) {
        Duel duel = duelManager.getDuel(event.getPlayer());
        if (duel == null)
            return;

        if (duel.getState() == Duel.DuelState.STARTING) {
            if (event.getTo().getBlockX() != event.getFrom().getBlockX()
                    || event.getTo().getBlockZ() != event.getFrom().getBlockZ()) {
                org.bukkit.Location newLoc = event.getFrom();
                newLoc.setPitch(event.getTo().getPitch());
                newLoc.setYaw(event.getTo().getYaw());
                event.setTo(newLoc);
            }
        } else if (duel.getState() == Duel.DuelState.FIGHTING) {
            Arena arena = duel.getArena();
            if (arena != null) {
                org.bukkit.Location pLoc = event.getTo();
                org.bukkit.Location c1 = arena.getCorner1();
                org.bukkit.Location c2 = arena.getCorner2();

                double minX = Math.min(c1.getX(), c2.getX());
                double maxX = Math.max(c1.getX(), c2.getX());
                double minZ = Math.min(c1.getZ(), c2.getZ());
                double maxZ = Math.max(c1.getZ(), c2.getZ());

                if (pLoc.getX() < minX || pLoc.getX() > maxX || pLoc.getZ() < minZ || pLoc.getZ() > maxZ) {
                    org.bukkit.Location center = arena.getCenter();
                    if (center == null) {
                        center = new org.bukkit.Location(c1.getWorld(), (minX + maxX) / 2, c1.getY(),
                                (minZ + maxZ) / 2);
                        center.setYaw(pLoc.getYaw());
                        center.setPitch(pLoc.getPitch());
                    }
                    event.setTo(center);
                    event.getPlayer().teleport(center);
                }
            }
        }
    }

    @EventHandler
    public void onTeleport(org.bukkit.event.player.PlayerTeleportEvent event) {
        Duel duel = duelManager.getDuel(event.getPlayer());
        if (duel != null && duel.getState() == Duel.DuelState.STARTING) {
            if (event.getCause() == org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.ENDER_PEARL
                    || event.getCause() == org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        Duel duel = duelManager.getDuel(event.getPlayer());
        if (duel != null && duel.getState() == Duel.DuelState.STARTING) {
            if (event.hasItem()) {
                org.bukkit.Material type = event.getItem().getType();
                if (type == org.bukkit.Material.ENDER_PEARL || type == org.bukkit.Material.CHORUS_FRUIT) {
                    event.setCancelled(true);
                    event.getPlayer().updateInventory();
                }
            }
        }
    }

    @EventHandler
    public void onElytra(org.bukkit.event.entity.EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof Player player) {
            Duel duel = duelManager.getDuel(player);
            if (duel != null && duel.getState() == Duel.DuelState.STARTING) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent event) {
        Duel duel = duelManager.getDuel(event.getPlayer());
        if (duel == null)
            return;

        if (duel.getState() != Duel.DuelState.FIGHTING) {
            event.setCancelled(true);
            return;
        }

        Arena arena = duel.getArena();
        if (arena == null || !arena.isBuildEnabled()) {
            event.setCancelled(true);
            MessageUtils.sendActionBar(event.getPlayer(), "duel.build.disabled",
                    java.util.Map.of());
            return;
        }
        if (!duel.getKit().getBuildWhitelist().isEmpty()
                && !duel.getKit().getBuildWhitelist().contains(event.getBlock().getType())) {
            event.setCancelled(true);
            MessageUtils.sendActionBar(event.getPlayer(), "duel.build.restricted",
                    java.util.Map.of());
            return;
        }
    }

    @EventHandler
    public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent event) {
        Duel duel = duelManager.getDuel(event.getPlayer());
        if (duel == null)
            return;

        if (duel.getState() != Duel.DuelState.FIGHTING) {
            event.setCancelled(true);
            return;
        }

        Arena arena = duel.getArena();
        if (arena == null || !arena.isBuildEnabled()) {
            event.setCancelled(true);
            MessageUtils.sendActionBar(event.getPlayer(), "duel.build.disabled",
                    java.util.Map.of());
            return;
        }
        if (!duel.getKit().getBuildWhitelist().isEmpty()
                && !duel.getKit().getBuildWhitelist().contains(event.getBlock().getType())) {
            event.setCancelled(true);
            MessageUtils.sendActionBar(event.getPlayer(), "duel.build.restricted",
                    java.util.Map.of());
            return;
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onCommand(org.bukkit.event.player.PlayerCommandPreprocessEvent event) {
        Duel duel = duelManager.getDuel(event.getPlayer());
        if (duel == null)
            return;

        String cmd = event.getMessage().toLowerCase();
        if (cmd.startsWith("/ffa leave"))
            return;
        if (cmd.startsWith("/leavefight"))
            return;
        if (cmd.startsWith("/leave"))
            return;
        if (cmd.startsWith("/spawn"))
            return;
        if (event.getPlayer().hasPermission("shyamduels.admin"))
            return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(
                MessageUtils.color("&cYou cannot use commands in a duel. Use /leavefight or /spawn to forfeit."));
    }
}
