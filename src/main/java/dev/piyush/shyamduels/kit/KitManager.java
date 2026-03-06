package dev.piyush.shyamduels.kit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.database.KitDao;
import dev.piyush.shyamduels.database.PlayerKitDao;
import org.bukkit.Bukkit;

import java.util.Collection;

public class KitManager {
    private final ShyamDuels plugin;

    private final Cache<String, Kit> kitCache;
    private final KitDao kitDao;
    private final PlayerKitDao playerKitDao;
    private final Cache<java.util.UUID, java.util.Map<String, PlayerKit>> playerKitCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(1, java.util.concurrent.TimeUnit.HOURS)
            .build();

    public KitManager(ShyamDuels plugin) {
        this.plugin = plugin;
        this.kitDao = new KitDao(plugin.getDatabaseManager());
        this.playerKitDao = new PlayerKitDao(plugin.getDatabaseManager());
        this.kitCache = Caffeine.newBuilder()
                .maximumSize(100)
                .build();

        loadKits();
    }

    public void loadKits() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            java.util.List<Kit> loaded = kitDao.loadKits();
            Bukkit.getScheduler().runTask(plugin, () -> {
                loaded.forEach(this::addKit);
                plugin.getLogger().info("Loaded " + loaded.size() + " kits.");
            });
        });
    }

    public void createKit(String name) {
        Kit kit = new Kit(name);
        kitCache.put(name.toLowerCase(), kit);
        saveKit(kit);
    }

    public void addKit(Kit kit) {
        kitCache.put(kit.getName().toLowerCase(), kit);
        saveKit(kit);
    }

    public void saveKit(Kit kit) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            kitDao.saveKit(kit);
        });
    }

    public Kit getKit(String name) {
        return kitCache.getIfPresent(name.toLowerCase());
    }

    public void deleteKit(String name) {
        kitCache.invalidate(name.toLowerCase());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> kitDao.deleteKit(name));
    }

    public Collection<Kit> getKits() {
        return kitCache.asMap().values();
    }

    public PlayerKit getPlayerKit(java.util.UUID uuid, String kitName) {
        java.util.Map<String, PlayerKit> userKits = playerKitCache.get(uuid,
                k -> new java.util.concurrent.ConcurrentHashMap<>());

        if (userKits.containsKey(kitName.toLowerCase())) {
            return userKits.get(kitName.toLowerCase());
        }

        PlayerKit loaded = playerKitDao.getPlayerKit(uuid, kitName);
        if (loaded != null) {
            userKits.put(kitName.toLowerCase(), loaded);
        }
        return loaded;
    }


    public void savePlayerKit(PlayerKit playerKit) {
        java.util.Map<String, PlayerKit> userKits = playerKitCache.get(playerKit.getPlayerUuid(),
                k -> new java.util.concurrent.ConcurrentHashMap<>());
        userKits.put(playerKit.getKitName().toLowerCase(), playerKit);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            playerKitDao.savePlayerKit(playerKit);
        });
    }


    
    public void resetPlayerKit(java.util.UUID uuid, String kitName) {
        java.util.Map<String, PlayerKit> userKits = playerKitCache.getIfPresent(uuid);
        if (userKits != null) {
            userKits.remove(kitName.toLowerCase());
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            playerKitDao.deletePlayerKit(uuid, kitName.toLowerCase());
        });
    }

    public int deleteAllPlayerKits(String kitName) {
        int count = 0;
        for (java.util.UUID uuid : playerKitCache.asMap().keySet()) {
            java.util.Map<String, PlayerKit> userKits = playerKitCache.getIfPresent(uuid);
            if (userKits != null && userKits.containsKey(kitName.toLowerCase())) {
                userKits.remove(kitName.toLowerCase());
                count++;
            }
        }

        final int finalCount = count;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            playerKitDao.deleteAllPlayerKitsForKit(kitName);
        });

        return finalCount;
    }

}
