package dev.piyush.shyamduels.stats;

import dev.piyush.shyamduels.ShyamDuels;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class StatsListener implements Listener {

    private final ShyamDuels plugin;
    private final StatsManager statsManager;

    public StatsListener(ShyamDuels plugin, StatsManager statsManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            statsManager.loadPlayer(event.getPlayer());
            plugin.getSettingsManager().getSettings(event.getPlayer().getUniqueId());
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            statsManager.unloadPlayer(event.getPlayer());
            plugin.getSettingsManager().unloadPlayer(event.getPlayer().getUniqueId());
        });
    }
}
