package dev.piyush.shyamduels.ffa;

import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.arena.Arena;
import dev.piyush.shyamduels.kit.Kit;
import dev.piyush.shyamduels.kit.PlayerKit;
import dev.piyush.shyamduels.util.FaweUtils;
import dev.piyush.shyamduels.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("deprecation")
public class FFAManager {

    private final ShyamDuels plugin;
    private final Map<UUID, FFAState> playerStates = new ConcurrentHashMap<>();
    private final Map<String, Long> arenaResetTimers = new ConcurrentHashMap<>();
    private final Map<String, BukkitTask> arenaTasks = new HashMap<>();

    public FFAManager(ShyamDuels plugin) {
        this.plugin = plugin;
        startResetTasks();
    }

    public void reload() {
        arenaTasks.values().forEach(BukkitTask::cancel);
        arenaTasks.clear();
        startResetTasks();
    }

    private void startResetTasks() {
        long interval = plugin.getConfig().getInt("ffa.reset-interval-seconds", 300) * 20L;

        for (Arena arena : plugin.getArenaManager().getArenas()) {
            if (arena.getType() == Arena.ArenaType.FFA) {
                scheduleArenaReset(arena, interval);
            }
        }
    }

    private void scheduleArenaReset(Arena arena, long intervalTicks) {
        arenaResetTimers.put(arena.getName(), System.currentTimeMillis() + (intervalTicks * 50));

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            resetArena(arena);
            arenaResetTimers.put(arena.getName(), System.currentTimeMillis() + (intervalTicks * 50));
        }, intervalTicks, intervalTicks);

        arenaTasks.put(arena.getName(), task);
    }

    public long getSecondsUntilReset(Arena arena) {
        Long resetTime = arenaResetTimers.get(arena.getName());
        if (resetTime == null)
            return 0;
        return Math.max(0, (resetTime - System.currentTimeMillis()) / 1000);
    }

    public void resetArena(Arena arena) {
        MessageUtils.broadcast("ffa.arena-resetting", Map.of("arena", arena.getName()));
        for (Map.Entry<UUID, FFAState> entry : playerStates.entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p != null && isInArena(p, arena)) {
                Location spawn = arena.getSpawn1();
                if (spawn != null) {
                    p.teleport(spawn);
                    MessageUtils.sendMessage(p, "ffa.arena-reset-tp");
                }
            }
        }
        FaweUtils.pasteSchematic(arena);
    }

    private final Map<UUID, String> playerArenaMap = new ConcurrentHashMap<>();

    public boolean isInArena(Player p, Arena arena) {
        return arena.getName().equals(playerArenaMap.get(p.getUniqueId()));
    }

    public void joinFFA(Player player, Arena arena) {
        if (playerStates.containsKey(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "ffa.already-joined");
            return;
        }
        if (arena.getAllowedKits().isEmpty()) {
            MessageUtils.sendMessage(player, "ffa.no-kit-configured", Map.of("arena", arena.getName()));
            return;
        }

        String kitName = arena.getAllowedKits().iterator().next();

        Kit kit = plugin.getKitManager().getKit(kitName);
        if (kit == null) {
            MessageUtils.sendMessage(player, "kit.not-found", Map.of("name", kitName));
            return;
        }
        Location spawn = null;
        if (arena.getSpawn1() != null && arena.getSpawn2() != null) {
            spawn = Math.random() < 0.5 ? arena.getSpawn1() : arena.getSpawn2();
        } else if (arena.getSpawn1() != null) {
            spawn = arena.getSpawn1();
        } else if (arena.getSpawn2() != null) {
            spawn = arena.getSpawn2();
        }

        if (spawn == null) {
            MessageUtils.sendMessage(player, "ffa.no-spawn", Map.of("arena", arena.getName()));
            return;
        }

        player.teleport(spawn);
        playerStates.put(player.getUniqueId(), FFAState.FFA_STARTING);
        playerArenaMap.put(player.getUniqueId(), arena.getName());
        final Kit finalKit = kit;
        new Runnable() {
            int count = 3;

            @Override
            public void run() {
                if (!player.isOnline() || !playerStates.containsKey(player.getUniqueId()))
                    return;

                if (count > 0) {
                    player.sendTitle(MessageUtils.color("&c" + count), "", 0, 20, 10);
                    MessageUtils.sendActionBar(player, "ffa.starting-in", Map.of("time", String.valueOf(count)));
                    player.sendMessage(MessageUtils.color("&eStarting in &c" + count + "..."));
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                    count--;
                    Bukkit.getScheduler().runTaskLater(plugin, this, 20L);
                } else {
                    player.sendTitle(MessageUtils.color("&c&lFIGHT!"), "", 5, 20, 5);
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1.5f);
                    playerStates.put(player.getUniqueId(), FFAState.IN_FFA);
                    applyFFAKit(player, finalKit);
                }
            }
        }.run();
    }

    private void applyFFAKit(Player player, Kit kit) {
        PlayerKit edited = plugin.getKitManager().getPlayerKit(player.getUniqueId(), kit.getName());
        if (edited != null) {
            edited.apply(player);
        } else {
            kit.apply(player);
        }
    }

    public void leaveFFA(Player player) {
        if (!playerStates.containsKey(player.getUniqueId()))
            return;

        playerStates.remove(player.getUniqueId());
        playerArenaMap.remove(player.getUniqueId());

        player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
        teleportToLobby(player);
        plugin.getItemManager().giveSpawnItems(player);
    }

    public void handleDeath(Player player) {
        if (!playerStates.containsKey(player.getUniqueId()))
            return;

        leaveFFA(player);
        player.sendTitle(MessageUtils.color("&c&lYOU DIED"), MessageUtils.color("&7Returned to lobby"), 5, 40, 10);
    }

    public void teleportToLobby(Player player) {
        String worldName = plugin.getConfig().getString("lobby.world");
        if (worldName == null)
            return;

        double x = plugin.getConfig().getDouble("lobby.x");
        double y = plugin.getConfig().getDouble("lobby.y");
        double z = plugin.getConfig().getDouble("lobby.z");
        float yaw = (float) plugin.getConfig().getDouble("lobby.yaw");
        float pitch = (float) plugin.getConfig().getDouble("lobby.pitch");

        org.bukkit.World w = Bukkit.getWorld(worldName);
        if (w != null) {
            player.teleport(new Location(w, x, y, z, yaw, pitch));
        }
    }

    public FFAState getPlayerState(Player p) {
        return playerStates.getOrDefault(p.getUniqueId(), FFAState.NONE);
    }

    public int getPlayerCount(Arena arena) {
        int count = 0;
        for (String arenaName : playerArenaMap.values()) {
            if (arenaName.equals(arena.getName()))
                count++;
        }
        return count;
    }

    public Arena getPlayerArena(Player player) {
        String arenaName = playerArenaMap.get(player.getUniqueId());
        if (arenaName == null)
            return null;
        return plugin.getArenaManager().getArena(arenaName);
    }

    public Kit getPlayerKit(Player player) {
        Arena arena = getPlayerArena(player);
        if (arena == null || arena.getAllowedKits().isEmpty())
            return null;
        return plugin.getKitManager().getKit(arena.getAllowedKits().iterator().next());
    }

    public enum FFAState {
        NONE, FFA_STARTING, IN_FFA
    }
}
