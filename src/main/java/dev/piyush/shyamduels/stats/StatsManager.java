package dev.piyush.shyamduels.stats;

import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.database.PlayerStatsDao;
import dev.piyush.shyamduels.util.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StatsManager {

    private final ShyamDuels plugin;
    private final PlayerStatsDao statsDao;
    private final Map<UUID, PlayerStats> statsCache = new ConcurrentHashMap<>();

    private final int baseEloGain;
    private final int baseEloLoss;
    private final int killEloGain;
    private final int minElo;

    public StatsManager(ShyamDuels plugin, PlayerStatsDao statsDao) {
        this.plugin = plugin;
        this.statsDao = statsDao;

        this.baseEloGain = plugin.getConfig().getInt("elo.base-gain", 25);
        this.baseEloLoss = plugin.getConfig().getInt("elo.base-loss", 20);
        this.killEloGain = plugin.getConfig().getInt("elo.kill-gain", 5);
        this.minElo = plugin.getConfig().getInt("elo.min-elo", 0);

        if (isEloEnabled()) {
            Rank.loadRanks(plugin);
        }
    }

    public PlayerStats getStats(UUID uuid) {
        return statsCache.computeIfAbsent(uuid, statsDao::loadStats);
    }

    public PlayerStats getStats(Player player) {
        return getStats(player.getUniqueId());
    }

    public void loadPlayer(Player player) {
        PlayerStats stats = statsDao.loadStats(player.getUniqueId());
        stats.setLastJoin(System.currentTimeMillis());
        statsCache.put(player.getUniqueId(), stats);
    }

    public void savePlayer(Player player) {
        PlayerStats stats = statsCache.get(player.getUniqueId());
        if (stats != null) {
            stats.updatePlaytime();
            statsDao.saveStats(stats);
        }
    }

    public void unloadPlayer(Player player) {
        savePlayer(player);
        statsCache.remove(player.getUniqueId());
    }

    public void saveAllPlayers() {
        for (PlayerStats stats : statsCache.values()) {
            stats.updatePlaytime();
            statsDao.saveStats(stats);
        }
    }

    public void recordKill(Player killer, Player victim) {
        if (!isEloEnabled()) {
            PlayerStats killerStats = getStats(killer);
            killerStats.addKill();
            return;
        }

        PlayerStats killerStats = getStats(killer);

        Rank killerOldRank = Rank.getRankForElo(killerStats.getElo());

        killerStats.addKill();
        killerStats.addElo(killEloGain);

        Rank killerNewRank = Rank.getRankForElo(killerStats.getElo());
        checkRankChange(killer, killerOldRank, killerNewRank);
    }

    public void recordDeath(Player victim, Player killer) {
        PlayerStats victimStats = getStats(victim);
        victimStats.addDeath();
    }

    public void recordWin(Player winner) {
        if (!isEloEnabled()) {
            PlayerStats stats = getStats(winner);
            stats.addWin();
            return;
        }

        PlayerStats stats = getStats(winner);
        Rank oldRank = Rank.getRankForElo(stats.getElo());

        stats.addWin();
        stats.addElo(baseEloGain);

        Rank newRank = Rank.getRankForElo(stats.getElo());
        checkRankChange(winner, oldRank, newRank);
    }

    public void recordLoss(Player loser) {
        if (!isEloEnabled()) {
            PlayerStats stats = getStats(loser);
            stats.addLoss();
            return;
        }

        PlayerStats stats = getStats(loser);
        Rank oldRank = Rank.getRankForElo(stats.getElo());

        stats.addLoss();

        int eloLoss = (int) (baseEloLoss * oldRank.getLossMultiplier());
        stats.removeElo(eloLoss);

        if (stats.getElo() < minElo) {
            stats.setElo(minElo);
        }

        Rank newRank = Rank.getRankForElo(stats.getElo());
        checkRankChange(loser, oldRank, newRank);
    }

    private void checkRankChange(Player player, Rank oldRank, Rank newRank) {
        if (oldRank == null || newRank == null || oldRank.getId().equals(newRank.getId())) {
            return;
        }

        boolean isPromotion = newRank.getOrder() > oldRank.getOrder();

        String titleKey = isPromotion ? "ranks.rank-up.title" : "ranks.rank-down.title";
        String subtitleKey = isPromotion ? "ranks.rank-up.subtitle" : "ranks.rank-down.subtitle";
        String titleText = MessageUtils.get(titleKey);
        String subtitleText = MessageUtils.get(subtitleKey);
        titleText = titleText.replace("<rank>", newRank.getDisplayName())
                .replace("<rank_color>", "")
                .replace("</rank_color>", "");
        String rankColor = newRank.getColoredName();
        String colorTag = "";
        if (rankColor.startsWith("&") && rankColor.length() > 2) {
            char colorChar = rankColor.charAt(1);
            colorTag = switch (colorChar) {
                case '0' -> "<black>";
                case '1' -> "<dark_blue>";
                case '2' -> "<dark_green>";
                case '3' -> "<dark_aqua>";
                case '4' -> "<dark_red>";
                case '5' -> "<dark_purple>";
                case '6' -> "<gold>";
                case '7' -> "<gray>";
                case '8' -> "<dark_gray>";
                case '9' -> "<blue>";
                case 'a' -> "<green>";
                case 'b' -> "<aqua>";
                case 'c' -> "<red>";
                case 'd' -> "<light_purple>";
                case 'e' -> "<yellow>";
                case 'f' -> "<white>";
                default -> "<white>";
            };
        }

        subtitleText = subtitleText.replace("<rank>", newRank.getDisplayName())
                .replace("<rank_color>", colorTag)
                .replace("</rank_color>", colorTag.isEmpty() ? "" : colorTag.replace("<", "</"));

        Component title = MessageUtils.parse(titleText, Map.of());
        Component subtitle = MessageUtils.parse(subtitleText, Map.of());

        Title.Times times = Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofMillis(3000),
                Duration.ofMillis(500));

        player.showTitle(Title.title(title, subtitle, times));

        if (isPromotion) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1.2f);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
        }

        plugin.getLogger().info(
                player.getName() + " rank changed: " + oldRank.getDisplayName() + " -> " + newRank.getDisplayName());
    }

    public Rank getRank(Player player) {
        return Rank.getRankForElo(getStats(player).getElo());
    }

    public boolean isEloEnabled() {
        return plugin.getConfig().getBoolean("elo.enabled", true)
                && plugin.getConfig().getBoolean("elo.rank-system", true);
    }

    public PlayerStatsDao getStatsDao() {
        return statsDao;
    }
}
