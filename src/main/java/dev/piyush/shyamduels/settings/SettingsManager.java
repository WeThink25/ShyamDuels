package dev.piyush.shyamduels.settings;

import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.database.PlayerSettingsDao;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SettingsManager {
    
    private final PlayerSettingsDao settingsDao;
    private final Map<UUID, PlayerSettings> settingsCache = new ConcurrentHashMap<>();
    
    public SettingsManager(ShyamDuels plugin, PlayerSettingsDao settingsDao) {
        this.settingsDao = settingsDao;
    }
    
    public PlayerSettings getSettings(UUID playerUuid) {
        return settingsCache.computeIfAbsent(playerUuid, uuid -> {
            PlayerSettings settings = settingsDao.loadSettings(uuid);
            if (settings == null) {
                settings = new PlayerSettings(uuid);
                settingsDao.saveSettings(settings);
            }
            return settings;
        });
    }
    
    public void saveSettings(PlayerSettings settings) {
        settingsCache.put(settings.getPlayerUuid(), settings);
        settingsDao.saveSettings(settings);
    }
    
    public void unloadPlayer(UUID playerUuid) {
        PlayerSettings settings = settingsCache.remove(playerUuid);
        if (settings != null) {
            settingsDao.saveSettings(settings);
        }
    }
    
    public void saveAll() {
        for (PlayerSettings settings : settingsCache.values()) {
            settingsDao.saveSettings(settings);
        }
    }
}
