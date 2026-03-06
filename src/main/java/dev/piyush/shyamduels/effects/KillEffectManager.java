package dev.piyush.shyamduels.effects;

import dev.piyush.shyamduels.ShyamDuels;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KillEffectManager {
    
    private final ShyamDuels plugin;
    private final Map<UUID, KillEffect> playerEffects = new ConcurrentHashMap<>();
    
    public KillEffectManager(ShyamDuels plugin) {
        this.plugin = plugin;
    }
    
    public KillEffect getPlayerEffect(UUID playerUuid) {
        return playerEffects.getOrDefault(playerUuid, KillEffect.NONE);
    }
    
    public void setPlayerEffect(UUID playerUuid, KillEffect effect) {
        if (effect == KillEffect.NONE) {
            playerEffects.remove(playerUuid);
        } else {
            playerEffects.put(playerUuid, effect);
        }
    }
    
    public boolean canUseEffect(Player player, KillEffect effect) {
        int playerElo = plugin.getStatsManager().getStats(player.getUniqueId()).getElo();
        return effect.canUse(playerElo);
    }
    
    public void playEffect(Player killer, Location victimLocation) {
        if (killer == null || victimLocation == null) return;
        
        KillEffect effect = getPlayerEffect(killer.getUniqueId());
        if (effect != null && effect != KillEffect.NONE) {
            effect.play(victimLocation);
        }
    }
    
    public KillEffect[] getUnlockedEffects(Player player) {
        int playerElo = plugin.getStatsManager().getStats(player.getUniqueId()).getElo();
        return java.util.Arrays.stream(KillEffect.values())
            .filter(effect -> effect.canUse(playerElo))
            .toArray(KillEffect[]::new);
    }
    
    public KillEffect[] getLockedEffects(Player player) {
        int playerElo = plugin.getStatsManager().getStats(player.getUniqueId()).getElo();
        return java.util.Arrays.stream(KillEffect.values())
            .filter(effect -> !effect.canUse(playerElo))
            .toArray(KillEffect[]::new);
    }
}
