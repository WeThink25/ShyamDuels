package dev.piyush.shyamduels.duel;

import dev.piyush.shyamduels.arena.Arena;
import dev.piyush.shyamduels.kit.Kit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Duel {

    private final UUID duelId;
    private final java.util.List<UUID> team1;
    private final java.util.List<UUID> team2;
    private final java.util.List<UUID> aliveTeam1;
    private final java.util.List<UUID> aliveTeam2;
    // private final dev.piyush.shyamduels.queue.QueueMode mode; // Unused

    private Arena arena;
    private Kit kit;
    private DuelState state;
    private long startTime;

    private int maxRounds = 1;
    private int currentRound = 1;
    // private final java.util.Map<UUID, Integer> playerWins = new
    // java.util.HashMap<>(); // Unused
    private int team1Wins = 0;
    private int team2Wins = 0;

    public Duel(java.util.List<UUID> team1, java.util.List<UUID> team2, dev.piyush.shyamduels.queue.QueueMode mode,
            int maxRounds) {
        this.duelId = UUID.randomUUID();
        this.team1 = new java.util.ArrayList<>(team1);
        this.team2 = new java.util.ArrayList<>(team2);
        this.aliveTeam1 = new java.util.ArrayList<>(team1);
        this.aliveTeam2 = new java.util.ArrayList<>(team2);
        // this.mode = mode;
        this.state = DuelState.STARTING;
        this.startTime = System.currentTimeMillis();
        this.maxRounds = maxRounds;
    }

    public UUID getDuelId() {
        return duelId;
    }

    public java.util.List<UUID> getTeam1() {
        return team1;
    }

    public java.util.List<UUID> getTeam2() {
        return team2;
    }

    public java.util.List<UUID> getAliveTeam1() {
        return aliveTeam1;
    }

    public java.util.List<UUID> getAliveTeam2() {
        return aliveTeam2;
    }

    public void handleDeath(Player p) {
        if (aliveTeam1.contains(p.getUniqueId())) {
            aliveTeam1.remove(p.getUniqueId());
        } else if (aliveTeam2.contains(p.getUniqueId())) {
            aliveTeam2.remove(p.getUniqueId());
        }
    }

    public boolean isRoundOver() {
        return aliveTeam1.isEmpty() || aliveTeam2.isEmpty();
    }

    public int getWinningTeamNumber() {
        if (aliveTeam2.isEmpty() && !aliveTeam1.isEmpty())
            return 1;
        if (aliveTeam1.isEmpty() && !aliveTeam2.isEmpty())
            return 2;
        return 0;
    }

    public boolean hasPlayer(Player p) {
        return team1.contains(p.getUniqueId()) || team2.contains(p.getUniqueId());
    }

    public boolean isTeam1(Player p) {
        return team1.contains(p.getUniqueId());
    }

    public Player getPlayer1() {
        return org.bukkit.Bukkit.getPlayer(team1.get(0));
    }

    public Player getPlayer2() {
        return org.bukkit.Bukkit.getPlayer(team2.get(0));
    }

    public Player getOpponent(Player p) {

        if (team1.contains(p.getUniqueId())) {
            for (UUID uuid : team2) {
                Player opp = org.bukkit.Bukkit.getPlayer(uuid);
                if (opp != null)
                    return opp;
            }
        }
        if (team2.contains(p.getUniqueId())) {
            for (UUID uuid : team1) {
                Player opp = org.bukkit.Bukkit.getPlayer(uuid);
                if (opp != null)
                    return opp;
            }
        }
        return null;
    }

    public Arena getArena() {
        return arena;
    }

    public void setArena(Arena arena) {
        this.arena = arena;
    }

    public Kit getKit() {
        return kit;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
    }

    public DuelState getState() {
        return state;
    }

    public void setState(DuelState state) {
        this.state = state;
    }

    public boolean isInProgress() {
        return state == DuelState.FIGHTING;
    }

    public long getStartTime() {
        return startTime;
    }

    private int matchWinnerTeam = 0;

    public int getMatchWinnerTeam() {
        return matchWinnerTeam;
    }

    public Player getWinner() {
        return null;
    }

    public Player getLoser() {
        return null;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void nextRound() {
        currentRound++;
        aliveTeam1.clear();
        aliveTeam1.addAll(team1);
        aliveTeam2.clear();
        aliveTeam2.addAll(team2);
    }

    public void addRoundWin(int teamNumber) {
        if (teamNumber == 1)
            team1Wins++;
        if (teamNumber == 2)
            team2Wins++;
    }

    public boolean hasWonMatch(int teamNumber) {
        int wins = (teamNumber == 1) ? team1Wins : team2Wins;
        int required = (maxRounds / 2) + 1;
        return wins >= required;
    }

    public int getTeam1Wins() {
        return team1Wins;
    }

    public int getTeam2Wins() {
        return team2Wins;
    }

    public int getWins(Player player) {
        if (team1.contains(player.getUniqueId()))
            return team1Wins;
        if (team2.contains(player.getUniqueId()))
            return team2Wins;
        return 0;
    }

    public void incrementWin(Player player) {
    }

    public boolean hasWonMatch(Player p) {
        if (team1.contains(p.getUniqueId()))
            return hasWonMatch(1);
        if (team2.contains(p.getUniqueId()))
            return hasWonMatch(2);
        return false;
    }

    public enum DuelState {
        STARTING,
        FIGHTING,
        ENDING,
        ENDED
    }
}
