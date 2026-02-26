package dev.piyush.shyamduels.queue;

import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.kit.Kit;
import dev.piyush.shyamduels.party.Party;
import dev.piyush.shyamduels.party.PartyState;
import dev.piyush.shyamduels.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QueueManager {

    private final ShyamDuels plugin;
    private final Map<QueueKey, ArrayDeque<UUID>> queues = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerState> playerStates = new ConcurrentHashMap<>();
    private final Map<UUID, QueueKey> playerQueueKeys = new ConcurrentHashMap<>();
    private final Map<UUID, Party> partyQueues = new ConcurrentHashMap<>();

    public QueueManager(ShyamDuels plugin) {
        this.plugin = plugin;
        startMatchmakingTask();
    }

    public void addPlayer(Player player, Kit kit, QueueMode mode) {
        if (playerStates.getOrDefault(player.getUniqueId(), PlayerState.NONE) != PlayerState.NONE) {
            MessageUtils.sendMessage(player, "queue.already-queued");
            return;
        }

        QueueKey key = new QueueKey(kit.getName(), mode);
        queues.computeIfAbsent(key, k -> new ArrayDeque<>()).addLast(player.getUniqueId());
        playerStates.put(player.getUniqueId(), PlayerState.QUEUED);
        playerQueueKeys.put(player.getUniqueId(), key);

        MessageUtils.sendMessage(player, "queue.joined",
                Map.of("kit", kit.getName(), "mode", mode.getDisplay()));

        plugin.getItemManager().giveQueueItems(player);
        plugin.getScoreboardManager().setQueueStartTime(player);
    }

    public void addParty(Party party, Kit kit, QueueMode mode) {
        if (party == null)
            return;

        int requiredSize = mode.getTeamSize();
        if (party.getSize() != requiredSize) {
            Player owner = party.getOwnerPlayer();
            if (owner != null) {
                MessageUtils.sendMessage(owner, "party.wrong-size",
                        Map.of("size", String.valueOf(party.getSize()), "required", String.valueOf(requiredSize)));
            }
            return;
        }

        for (UUID uuid : party.getMembers()) {
            if (playerStates.getOrDefault(uuid, PlayerState.NONE) != PlayerState.NONE) {
                Player owner = party.getOwnerPlayer();
                if (owner != null) {
                    MessageUtils.sendMessage(owner, "queue.party-member-busy");
                }
                return;
            }
        }

        QueueKey key = new QueueKey(kit.getName(), mode);
        party.setState(PartyState.IN_QUEUE);

        for (Player member : party.getOnlineMembers()) {
            queues.computeIfAbsent(key, k -> new ArrayDeque<>()).addLast(member.getUniqueId());
            playerStates.put(member.getUniqueId(), PlayerState.QUEUED);
            playerQueueKeys.put(member.getUniqueId(), key);
            partyQueues.put(member.getUniqueId(), party);

            MessageUtils.sendMessage(member, "queue.joined",
                    Map.of("kit", kit.getName(), "mode", mode.getDisplay()));

            plugin.getItemManager().giveQueueItems(member);
            plugin.getScoreboardManager().setQueueStartTime(member);
        }
    }

    public void removePlayer(Player player) {
        if (playerStates.get(player.getUniqueId()) != PlayerState.QUEUED) {
            return;
        }

        Party party = partyQueues.get(player.getUniqueId());
        if (party != null) {
            removeParty(party);
            return;
        }

        playerStates.put(player.getUniqueId(), PlayerState.NONE);
        playerQueueKeys.remove(player.getUniqueId());

        for (ArrayDeque<UUID> queue : queues.values()) {
            queue.remove(player.getUniqueId());
        }

        MessageUtils.sendMessage(player, "queue.left");
        plugin.getItemManager().giveSpawnItems(player);
        plugin.getScoreboardManager().clearQueueStartTime(player);
    }

    public void removeParty(Party party) {
        if (party == null)
            return;

        party.setState(PartyState.LOBBY);

        for (UUID uuid : party.getMembers()) {
            playerStates.put(uuid, PlayerState.NONE);
            playerQueueKeys.remove(uuid);
            partyQueues.remove(uuid);

            for (ArrayDeque<UUID> queue : queues.values()) {
                queue.remove(uuid);
            }

            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                MessageUtils.sendMessage(player, "queue.left");
                plugin.getItemManager().givePartyItems(player, party);
                plugin.getScoreboardManager().clearQueueStartTime(player);
            }
        }
    }

    public void resetPlayerState(Player player) {
        playerStates.put(player.getUniqueId(), PlayerState.NONE);
        playerQueueKeys.remove(player.getUniqueId());
        partyQueues.remove(player.getUniqueId());
        plugin.getScoreboardManager().clearQueueStartTime(player);
    }

    public int getQueueSize(String kitName, QueueMode mode) {
        QueueKey key = new QueueKey(kitName, mode);
        ArrayDeque<UUID> queue = queues.get(key);
        return queue != null ? queue.size() : 0;
    }

    public int getInGameSize(String kitName, QueueMode mode) {
        return 0;
    }

    public boolean isQueued(Player player) {
        return playerStates.getOrDefault(player.getUniqueId(), PlayerState.NONE) == PlayerState.QUEUED;
    }

    public QueueKey getPlayerQueueKey(Player player) {
        return playerQueueKeys.get(player.getUniqueId());
    }

    private void startMatchmakingTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (QueueKey key : queues.keySet()) {
                ArrayDeque<UUID> queue = queues.get(key);
                QueueMode mode = key.getMode();
                int required = mode.getTotalPlayers();

                if (queue.size() >= required) {
                    attemptMatchStart(key, queue);
                }
            }
        }, 20L, 20L);
    }

    private void attemptMatchStart(QueueKey key, ArrayDeque<UUID> queue) {
        synchronized (queue) {
            QueueMode mode = key.getMode();
            int teamSize = mode.getTeamSize();
            int required = mode.getTotalPlayers();

            if (queue.size() < required) {
                return;
            }

            Kit kit = plugin.getKitManager().getKit(key.getKitName());
            if (kit == null) {
                return;
            }

            dev.piyush.shyamduels.arena.Arena arena = plugin.getArenaManager().getAvailableArena(kit.getName());
            if (arena == null) {
                return;
            }

        List<Player> team1 = new ArrayList<>();
        List<Player> team2 = new ArrayList<>();

        for (int i = 0; i < teamSize; i++) {
            UUID uuid = queue.pollFirst();
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                team1.add(p);
            } else {
                if (uuid != null)
                    playerStates.remove(uuid);

                for (int j = team1.size() - 1; j >= 0; j--)
                    queue.addFirst(team1.get(j).getUniqueId());
                return;
            }
        }

        for (int i = 0; i < teamSize; i++) {
            UUID uuid = queue.pollFirst();
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                team2.add(p);
            } else {
                if (uuid != null)
                    playerStates.remove(uuid);

                for (int j = team2.size() - 1; j >= 0; j--)
                    queue.addFirst(team2.get(j).getUniqueId());
                for (int j = team1.size() - 1; j >= 0; j--)
                    queue.addFirst(team1.get(j).getUniqueId());
                return;
            }
        }

        team1.forEach(p -> {
            playerStates.put(p.getUniqueId(), PlayerState.IN_MATCH);
            playerQueueKeys.remove(p.getUniqueId());
            MessageUtils.sendMessage(p, "queue.found");
            plugin.getScoreboardManager().clearQueueStartTime(p);

            Party party = partyQueues.remove(p.getUniqueId());
            if (party != null) {
                party.setState(PartyState.IN_DUEL);
            }
        });
        team2.forEach(p -> {
            playerStates.put(p.getUniqueId(), PlayerState.IN_MATCH);
            playerQueueKeys.remove(p.getUniqueId());
            MessageUtils.sendMessage(p, "queue.found");
            plugin.getScoreboardManager().clearQueueStartTime(p);

            Party party = partyQueues.remove(p.getUniqueId());
            if (party != null) {
                party.setState(PartyState.IN_DUEL);
            }
        });

            plugin.getDuelManager().startDuel(team1, team2, kit, mode, 1);
        }
    }
}
