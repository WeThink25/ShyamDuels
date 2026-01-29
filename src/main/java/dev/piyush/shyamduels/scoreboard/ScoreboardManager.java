package dev.piyush.shyamduels.scoreboard;

import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.duel.Duel;
import dev.piyush.shyamduels.ffa.FFAManager;
import dev.piyush.shyamduels.party.Party;
import dev.piyush.shyamduels.stats.PlayerStats;
import dev.piyush.shyamduels.util.MessageUtils;
import fr.mrmicky.fastboard.adventure.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager {

    private final ShyamDuels plugin;
    private final Map<UUID, FastBoard> boards = new ConcurrentHashMap<>();
    private final Map<UUID, Long> queueStartTimes = new ConcurrentHashMap<>();
    private FileConfiguration scoreboardConfig;
    private int taskId = -1;

    private static final DecimalFormat KDR_FORMAT = new DecimalFormat("0.00");

    public ScoreboardManager(ShyamDuels plugin) {
        this.plugin = plugin;
        loadConfig();
        startUpdateTask();
    }

    public void loadConfig() {
        File file = new File(plugin.getDataFolder(), "scoreboards.yml");
        if (!file.exists()) {
            plugin.saveResource("scoreboards.yml", false);
        }
        scoreboardConfig = YamlConfiguration.loadConfiguration(file);
    }

    public void createBoard(Player player) {
        if (boards.containsKey(player.getUniqueId()))
            return;

        FastBoard board = new FastBoard(player);
        boards.put(player.getUniqueId(), board);
        updateBoard(player);
    }

    public void removeBoard(Player player) {
        FastBoard board = boards.remove(player.getUniqueId());
        if (board != null && !board.isDeleted()) {
            board.delete();
        }
        queueStartTimes.remove(player.getUniqueId());
    }

    public void setQueueStartTime(Player player) {
        queueStartTimes.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void clearQueueStartTime(Player player) {
        queueStartTimes.remove(player.getUniqueId());
    }

    private void startUpdateTask() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        int interval = scoreboardConfig.getInt("update-interval", 20);
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateBoard(player);
            }
        }, 20L, interval).getTaskId();
    }

    public void updateBoard(Player player) {
        FastBoard board = boards.get(player.getUniqueId());
        if (board == null || board.isDeleted())
            return;

        ScoreboardType type = determineType(player);
        String section = type.getConfigSection();

        if (!scoreboardConfig.getBoolean(section + ".enabled", true)) {
            if (!board.isDeleted()) {
                board.delete();
            }
            return;
        }

        String title = scoreboardConfig.getString(section + ".title", "&7Scoreboard");
        List<String> lines = scoreboardConfig.getStringList(section + ".lines");
        board.updateTitle(MessageUtils.parseColors(title));

        List<net.kyori.adventure.text.Component> parsedLines = new ArrayList<>();
        for (String line : lines) {
            String replaced = replacePlaceholders(line, player, type);
            if (replaced.contains("{member_list}")) {
                List<String> memberLines = getMemberListLines(player);
                for (String memberLine : memberLines) {
                    parsedLines.add(MessageUtils.parseColors(memberLine));
                }
            } else {
                parsedLines.add(MessageUtils.parseColors(replaced));
            }
        }

        Set<String> seen = new HashSet<>();
        List<net.kyori.adventure.text.Component> uniqueLines = new ArrayList<>();
        int suffix = 1;
        for (net.kyori.adventure.text.Component comp : parsedLines) {
            String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                    .serialize(comp);
            if (seen.contains(plain)) {
                String spaces = " ".repeat(suffix);
                comp = comp.append(net.kyori.adventure.text.Component.text(spaces));
                suffix++;
            }
            seen.add(plain);
            uniqueLines.add(comp);
        }

        board.updateLines(uniqueLines);
    }

    private ScoreboardType determineType(Player player) {
        if (plugin.getSpectatorManager().isSpectating(player)) {
            return ScoreboardType.SPECTATOR;
        }

        if (plugin.getDuelManager().isInDuel(player)) {
            return ScoreboardType.DUEL;
        }

        if (plugin.getFFAManager().getPlayerState(player) != FFAManager.FFAState.NONE) {
            return ScoreboardType.DUEL;
        }

        Party party = plugin.getPartyManager().getParty(player);
        if (party != null) {
            if (party.isInQueue()) {
                return ScoreboardType.QUEUE;
            }
            return ScoreboardType.PARTY_LOBBY;
        }

        if (queueStartTimes.containsKey(player.getUniqueId())) {
            return ScoreboardType.QUEUE;
        }

        return ScoreboardType.LOBBY;
    }

    private String replacePlaceholders(String line, Player player, ScoreboardType type) {
        line = line.replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()));

        int queued = 0;
        int fighting = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plugin.getDuelManager().isInDuel(p))
                fighting++;
            if (plugin.getFFAManager().getPlayerState(p) != FFAManager.FFAState.NONE)
                fighting++;
            if (plugin.getQueueManager().isQueued(p))
                queued++;
        }
        line = line.replace("{queued}", String.valueOf(queued));
        line = line.replace("{fighting}", String.valueOf(fighting));

        PlayerStats stats = plugin.getStatsManager().getStats(player);
        if (stats != null) {
            line = line.replace("{wins}", String.valueOf(stats.getWins()));
            line = line.replace("{losses}", String.valueOf(stats.getLosses()));
            line = line.replace("{kills}", String.valueOf(stats.getKills()));
            line = line.replace("{deaths}", String.valueOf(stats.getDeaths()));
            double kdr = stats.getDeaths() > 0 ? (double) stats.getKills() / stats.getDeaths() : stats.getKills();
            line = line.replace("{kdr}", KDR_FORMAT.format(kdr));
            line = line.replace("{elo}", String.valueOf(stats.getElo()));
            line = line.replace("{rank}", getRankFromElo(stats.getElo()));
        } else {
            line = line.replace("{wins}", "0");
            line = line.replace("{losses}", "0");
            line = line.replace("{kills}", "0");
            line = line.replace("{deaths}", "0");
            line = line.replace("{kdr}", "0.00");
            line = line.replace("{elo}", "1000");
            line = line.replace("{rank}", "&f[Unranked]");
        }

        Party party = plugin.getPartyManager().getParty(player);
        if (party != null) {
            Player owner = party.getOwnerPlayer();
            line = line.replace("{owner}", owner != null ? owner.getName() : "Unknown");
            line = line.replace("{size}", String.valueOf(party.getSize()));
            line = line.replace("{max_size}", String.valueOf(plugin.getPartyManager().getMaxPartySize(owner)));
            String modeColor = party.isPublic() ? "&a" : "&c";
            line = line.replace("{party_mode}", modeColor + party.getMode().name());
        }

        if (type == ScoreboardType.QUEUE) {
            Long startTime = queueStartTimes.get(player.getUniqueId());
            if (startTime != null) {
                long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                long minutes = elapsed / 60;
                long seconds = elapsed % 60;
                String time = minutes > 0 ? minutes + "m " + seconds + "s" : seconds + "s";
                line = line.replace("{time}", time);
            } else {
                line = line.replace("{time}", "0s");
            }
            dev.piyush.shyamduels.queue.QueueKey queueKey = plugin.getQueueManager().getPlayerQueueKey(player);
            if (queueKey != null) {
                line = line.replace("{kit}", queueKey.getKitName());
                line = line.replace("{mode}", queueKey.getMode().getDisplay());
                line = line.replace("{queue_size}",
                        String.valueOf(
                                plugin.getQueueManager().getQueueSize(queueKey.getKitName(), queueKey.getMode())));
            } else {
                line = line.replace("{kit}", "None");
                line = line.replace("{mode}", "None");
                line = line.replace("{queue_size}", "0");
            }
        }

        Duel duel = plugin.getDuelManager().getDuel(player);
        if (duel != null) {
            line = line.replace("{kit}", duel.getKit() != null ? duel.getKit().getName() : "Unknown");
            line = line.replace("{arena}", duel.getArena() != null ? duel.getArena().getName() : "Unknown");
            line = line.replace("{your_name}", player.getName());
            line = line.replace("{your_ping}", String.valueOf(player.getPing()));

            Player opponent = duel.getOpponent(player);
            if (opponent != null) {
                line = line.replace("{opponent_name}", opponent.getName());
                line = line.replace("{opponent_ping}", String.valueOf(opponent.getPing()));
            } else {
                // Check if it's a team duel with multiple opponents
                List<UUID> opponents = duel.getTeam1().contains(player.getUniqueId()) ? duel.getTeam2()
                        : duel.getTeam1();
                if (opponents.size() > 1) {
                    line = line.replace("{opponent_name}", opponents.size() + " Players");
                    line = line.replace("{opponent_ping}", "-");
                } else if (!opponents.isEmpty()) {
                    Player op = Bukkit.getPlayer(opponents.get(0));
                    if (op != null) {
                        line = line.replace("{opponent_name}", op.getName());
                        line = line.replace("{opponent_ping}", String.valueOf(op.getPing()));
                    } else {
                        line = line.replace("{opponent_name}", "Offline");
                        line = line.replace("{opponent_ping}", "0");
                    }
                } else {
                    line = line.replace("{opponent_name}", "???");
                    line = line.replace("{opponent_ping}", "0");
                }
            }

            line = line.replace("{round}", String.valueOf(duel.getCurrentRound()));
            line = line.replace("{max_rounds}", String.valueOf(duel.getMaxRounds()));
        } else if (plugin.getFFAManager().getPlayerState(player) != FFAManager.FFAState.NONE) {
            dev.piyush.shyamduels.arena.Arena ffaArena = plugin.getFFAManager().getPlayerArena(player);
            dev.piyush.shyamduels.kit.Kit ffaKit = plugin.getFFAManager().getPlayerKit(player);

            line = line.replace("{kit}", ffaKit != null ? ffaKit.getName() : "FFA");
            line = line.replace("{arena}", ffaArena != null ? ffaArena.getName() : "FFA Arena");
            line = line.replace("{your_name}", player.getName());
            line = line.replace("{your_ping}", String.valueOf(player.getPing()));
            line = line.replace("{opponent_name}", "FFA Mode");
            line = line.replace("{opponent_ping}", "-");
            line = line.replace("{round}", "1");
            line = line.replace("{max_rounds}", "1");
        }

        if (type == ScoreboardType.SPECTATOR) {
            Duel spectatedDuel = plugin.getSpectatorManager().getDuel(player);
            if (spectatedDuel != null) {
                line = line.replace("{kit}",
                        spectatedDuel.getKit() != null ? spectatedDuel.getKit().getName() : "Unknown");
                line = line.replace("{arena}",
                        spectatedDuel.getArena() != null ? spectatedDuel.getArena().getName() : "Unknown");

                List<UUID> team1 = spectatedDuel.getTeam1();
                List<UUID> team2 = spectatedDuel.getTeam2();

                String player1 = team1.isEmpty() ? "???" : getPlayerName(team1.get(0));
                String player2 = team2.isEmpty() ? "???" : getPlayerName(team2.get(0));

                line = line.replace("{player1}", player1);
                line = line.replace("{player2}", player2);
                line = line.replace("{spectator_count}",
                        String.valueOf(plugin.getSpectatorManager().getSpectatorCount(spectatedDuel)));
            }
        }

        return line;
    }

    private List<String> getMemberListLines(Player player) {
        Party party = plugin.getPartyManager().getParty(player);
        if (party == null)
            return Collections.singletonList("&7None");

        List<String> lines = new ArrayList<>();
        int count = 0;
        for (Player member : party.getOnlineMembers()) {
            if (count >= 5) {
                lines.add("&7+" + (party.getSize() - 5) + " more...");
                break;
            }
            String prefix = party.isOwner(member.getUniqueId()) ? "&d★ " : "&7• ";
            lines.add(prefix + "&f" + member.getName());
            count++;
        }
        return lines;
    }

    private String getPlayerName(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null ? player.getName() : "???";
    }

    private String getRankFromElo(int elo) {
        if (elo >= 2000)
            return "&6Champion";
        if (elo >= 1600)
            return "&bDiamond";
        if (elo >= 1400)
            return "&dPlatinum";
        if (elo >= 1200)
            return "&eGold";
        if (elo >= 1000)
            return "&7Silver";
        return "&8Bronze";
    }

    public void reload() {
        loadConfig();
        startUpdateTask();
    }

    public void shutdown() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        for (FastBoard board : boards.values()) {
            if (!board.isDeleted()) {
                board.delete();
            }
        }
        boards.clear();
    }

    public enum ScoreboardType {
        LOBBY("lobby"),
        PARTY_LOBBY("party-lobby"),
        QUEUE("queue"),
        DUEL("duel"),
        SPECTATOR("spectator");

        private final String configSection;

        ScoreboardType(String configSection) {
            this.configSection = configSection;
        }

        public String getConfigSection() {
            return configSection;
        }
    }
}
