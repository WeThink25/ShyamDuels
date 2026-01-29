package dev.piyush.shyamduels.spectate;

import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.arena.Arena;
import dev.piyush.shyamduels.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SpectatorListener implements Listener {

    private final ShyamDuels plugin;

    public SpectatorListener(ShyamDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (!plugin.getSpectatorManager().isSpectating(player)) {
            return;
        }

        Arena arena = plugin.getSpectatorManager().getSpectatingArena(player);
        if (arena == null) {
            return;
        }

        Location pLoc = e.getTo();
        Location c1 = arena.getCorner1();
        Location c2 = arena.getCorner2();

        double minX = Math.min(c1.getX(), c2.getX());
        double maxX = Math.max(c1.getX(), c2.getX());
        double minZ = Math.min(c1.getZ(), c2.getZ());
        double maxZ = Math.max(c1.getZ(), c2.getZ());

        if (pLoc.getX() < minX || pLoc.getX() > maxX || pLoc.getZ() < minZ || pLoc.getZ() > maxZ) {
            Location center = arena.getCenter();
            if (center == null) {
                center = new Location(c1.getWorld(), (minX + maxX) / 2, c1.getY(), (minZ + maxZ) / 2);
                center.setYaw(pLoc.getYaw());
                center.setPitch(pLoc.getPitch());
            }
            player.teleport(center);
            MessageUtils.sendMessage(player, "spectate.boundary");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (plugin.getSpectatorManager().isSpectating(e.getPlayer())) {
            plugin.getSpectatorManager().leaveSpectate(e.getPlayer());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline() && player.getGameMode() != GameMode.SURVIVAL) {
                if (!plugin.getDuelManager().isInDuel(player)
                        && plugin.getFFAManager()
                                .getPlayerState(player) == dev.piyush.shyamduels.ffa.FFAManager.FFAState.NONE) {
                    player.setGameMode(GameMode.SURVIVAL);
                }
            }
            if (!plugin.getDuelManager().isInDuel(player)
                    && plugin.getFFAManager()
                            .getPlayerState(player) == dev.piyush.shyamduels.ffa.FFAManager.FFAState.NONE
                    && !plugin.getSpectatorManager().isSpectating(player)) {
                String worldName = plugin.getConfig().getString("lobby.world", "world");
                double x = plugin.getConfig().getDouble("lobby.x", 0);
                double y = plugin.getConfig().getDouble("lobby.y", 64);
                double z = plugin.getConfig().getDouble("lobby.z", 0);
                float yaw = (float) plugin.getConfig().getDouble("lobby.yaw", 0);
                float pitch = (float) plugin.getConfig().getDouble("lobby.pitch", 0);

                org.bukkit.World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    Location lobbyLoc = new Location(world, x, y, z, yaw, pitch);
                    player.teleport(lobbyLoc);
                }
            }
        });
    }

    @EventHandler
    public void onTeleport(org.bukkit.event.player.PlayerTeleportEvent e) {
        if (e.getCause() == org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.SPECTATE
                && plugin.getSpectatorManager().isSpectating(e.getPlayer())) {
            e.setCancelled(true);
        }
    }
}
