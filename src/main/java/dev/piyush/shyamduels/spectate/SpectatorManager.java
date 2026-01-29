package dev.piyush.shyamduels.spectate;

import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.arena.Arena;
import dev.piyush.shyamduels.duel.Duel;
import dev.piyush.shyamduels.ffa.FFAManager;
import dev.piyush.shyamduels.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;

import org.bukkit.entity.Player;

import java.util.*;

public class SpectatorManager {

    private final ShyamDuels plugin;
    private final Map<UUID, Duel> spectatorToDuel = java.util.Collections.synchronizedMap(new java.util.HashMap<>());

    public SpectatorManager(ShyamDuels plugin) {
        this.plugin = plugin;
    }

    public void joinSpectate(Player spectator, Player target) {
        if (plugin.getDuelManager().isInDuel(spectator)) {
            MessageUtils.sendMessage(spectator, "spectate.already-in-match");
            return;
        }

        if (plugin.getFFAManager().getPlayerState(spectator) != FFAManager.FFAState.NONE) {
            MessageUtils.sendMessage(spectator, "spectate.already-in-match");
            return;
        }

        if (isSpectating(spectator)) {
            MessageUtils.sendMessage(spectator, "spectate.already-spectating");
            return;
        }
        Duel duel = plugin.getDuelManager().getDuel(target);
        if (duel == null) {
            if (plugin.getFFAManager().getPlayerState(target) != FFAManager.FFAState.NONE) {
                joinSpectateFFA(spectator, target);
                return;
            }

            MessageUtils.sendMessage(spectator, "spectate.player-not-in-match");
            return;
        }
        spectatorToDuel.put(spectator.getUniqueId(), duel);
        spectator.setGameMode(GameMode.SPECTATOR);
        spectator.teleport(target.getLocation());

        MessageUtils.sendMessage(spectator, "spectate.joined", Map.of("player", target.getName()));
    }

    private void joinSpectateFFA(Player spectator, Player target) {
        Arena arena = plugin.getFFAManager().getPlayerArena(target);
        if (arena == null) {
            MessageUtils.sendMessage(spectator, "spectate.player-not-in-match");
            return;
        }
        spectatorToDuel.put(spectator.getUniqueId(), null);

        spectator.setGameMode(GameMode.SPECTATOR);
        spectator.teleport(target.getLocation());

        MessageUtils.sendMessage(spectator, "spectate.joined", Map.of("player", target.getName()));
    }

    public void leaveSpectate(Player spectator) {
        if (!isSpectating(spectator)) {
            return;
        }

        spectatorToDuel.remove(spectator.getUniqueId());
        plugin.getFFAManager().teleportToLobby(spectator);
        spectator.setGameMode(GameMode.SURVIVAL);

        MessageUtils.sendMessage(spectator, "spectate.left");
    }

    public void endSpectateForDuel(Duel duel) {
        List<UUID> toRemove = new ArrayList<>();

        for (Map.Entry<UUID, Duel> entry : spectatorToDuel.entrySet()) {
            if (entry.getValue() != null && entry.getValue().equals(duel)) {
                toRemove.add(entry.getKey());
            }
        }

        for (UUID uuid : toRemove) {
            Player spectator = Bukkit.getPlayer(uuid);
            if (spectator != null) {
                spectatorToDuel.remove(uuid);
                MessageUtils.sendMessage(spectator, "spectate.match-ended");
                plugin.getFFAManager().teleportToLobby(spectator);
                spectator.setGameMode(GameMode.SURVIVAL);
            }
        }
    }

    public boolean isSpectating(Player player) {
        return spectatorToDuel.containsKey(player.getUniqueId());
    }

    public Duel getDuel(Player spectator) {
        return spectatorToDuel.get(spectator.getUniqueId());
    }

    public int getSpectatorCount(Duel duel) {
        return (int) spectatorToDuel.values().stream()
                .filter(d -> d != null && d.equals(duel))
                .count();
    }

    public List<Duel> getActiveDuels() {
        return plugin.getDuelManager().getActiveDuels();
    }

    public Arena getSpectatingArena(Player spectator) {
        Duel duel = spectatorToDuel.get(spectator.getUniqueId());
        if (duel == null) {
            return null;
        }
        return duel.getArena();
    }
}
