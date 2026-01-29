package dev.piyush.shyamduels.duel;

import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.arena.Arena;
import dev.piyush.shyamduels.kit.Kit;
import dev.piyush.shyamduels.kit.PlayerKit;
import dev.piyush.shyamduels.util.FaweUtils;
import dev.piyush.shyamduels.util.MessageUtils;
import dev.piyush.shyamduels.queue.QueueMode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DuelManager {
    private final ShyamDuels plugin;
    private final Map<UUID, Duel> activeDuels = new ConcurrentHashMap<>();
    private final Map<UUID, Duel> endingDuels = new ConcurrentHashMap<>();

    public void markEnding(Duel duel, int winningTeamNumber) {
        duel.setState(Duel.DuelState.ENDING);
        duel.addRoundWin(winningTeamNumber);
        duel.getTeam1().forEach(uuid -> endingDuels.put(uuid, duel));
        duel.getTeam2().forEach(uuid -> endingDuels.put(uuid, duel));
        if (duel.hasWonMatch(winningTeamNumber)) {
            String winTitleKey = "duel.title.win";
            String loseTitleKey = "duel.title.loss";

            sendTeamTitle(duel, winningTeamNumber, winTitleKey, winningTeamNumber == 1 ? 2 : 1, loseTitleKey);
            playTeamSound(duel, winningTeamNumber, org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            playTeamSound(duel, winningTeamNumber == 1 ? 2 : 1, org.bukkit.Sound.ENTITY_VILLAGER_DEATH, 1f, 1f);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> finishDuel(duel), 60L);
        } else {
            String winTitleKey = "duel.title.round-win";
            String loseTitleKey = "duel.title.round-loss";

            sendTeamTitle(duel, winningTeamNumber, winTitleKey, winningTeamNumber == 1 ? 2 : 1, loseTitleKey);
            playTeamSound(duel, winningTeamNumber, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
            playTeamSound(duel, winningTeamNumber == 1 ? 2 : 1, org.bukkit.Sound.ENTITY_ITEM_BREAK, 1f, 0.8f);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> resetRound(duel), 40L);
        }
    }

    private void sendTeamTitle(Duel duel, int winTeam, String winKey, int loseTeam, String loseKey) {
        net.kyori.adventure.title.Title.Times times = net.kyori.adventure.title.Title.Times.times(
                java.time.Duration.ofMillis(200), java.time.Duration.ofMillis(4000), java.time.Duration.ofMillis(1000));

        net.kyori.adventure.text.Component winComp = MessageUtils.parseOrLegacy(MessageUtils.get(winKey), Map.of());
        net.kyori.adventure.text.Component loseComp = MessageUtils.parseOrLegacy(MessageUtils.get(loseKey), Map.of());

        net.kyori.adventure.title.Title winTitle = net.kyori.adventure.title.Title.title(
                winComp,
                net.kyori.adventure.text.Component.empty(), times);

        net.kyori.adventure.title.Title loseTitle = net.kyori.adventure.title.Title.title(
                loseComp,
                net.kyori.adventure.text.Component.empty(), times);

        List<UUID> winners = (winTeam == 1) ? duel.getTeam1() : duel.getTeam2();
        List<UUID> losers = (loseTeam == 1) ? duel.getTeam1() : duel.getTeam2();

        winners.forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null)
                p.showTitle(winTitle);
        });

        losers.forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null)
                p.showTitle(loseTitle);
        });
    }

    private void playTeamSound(Duel duel, int teamNum, org.bukkit.Sound sound, float volume, float pitch) {
        List<UUID> team = (teamNum == 1) ? duel.getTeam1() : duel.getTeam2();
        team.forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null)
                p.playSound(p.getLocation(), sound, volume, pitch);
        });
    }

    public void resetRound(Duel duel) {
        duel.getTeam1().forEach(endingDuels::remove);
        duel.getTeam2().forEach(endingDuels::remove);
        if (duel.getArena() != null) {
            FaweUtils.pasteSchematic(duel.getArena());
        }
        duel.nextRound();
        duel.setState(Duel.DuelState.STARTING);
        duel.getTeam1().forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                if (duel.getArena().getSpawn1() != null)
                    p.teleport(duel.getArena().getSpawn1());
                applyKit(p, duel.getKit());
            }
        });

        duel.getTeam2().forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                if (duel.getArena().getSpawn2() != null)
                    p.teleport(duel.getArena().getSpawn2());
                applyKit(p, duel.getKit());
            }
        });

        startDuelCountdown(duel);
    }

    public Duel getEndingDuel(Player player) {
        return endingDuels.remove(player.getUniqueId());
    }

    public void finishDuel(Duel duel) {
        int wTeam = duel.hasWonMatch(1) ? 1 : (duel.hasWonMatch(2) ? 2 : 0);
        dev.piyush.shyamduels.stats.StatsManager statsManager = plugin.getStatsManager();
        List<UUID> winners = wTeam == 1 ? duel.getTeam1() : (wTeam == 2 ? duel.getTeam2() : Collections.emptyList());
        List<UUID> losers = wTeam == 1 ? duel.getTeam2() : (wTeam == 2 ? duel.getTeam1() : Collections.emptyList());

        winners.forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                statsManager.recordWin(p);
            }
        });

        losers.forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                statsManager.recordLoss(p);
            }
        });
        String kitName = duel.getKit().getName();
        String winnerNames = (wTeam == 1 ? duel.getTeam1() : duel.getTeam2()).stream()
                .map(Bukkit::getPlayer).filter(java.util.Objects::nonNull).map(Player::getName)
                .collect(Collectors.joining(", "));
        String loserNames = (wTeam == 1 ? duel.getTeam2() : duel.getTeam1()).stream()
                .map(Bukkit::getPlayer).filter(java.util.Objects::nonNull).map(Player::getName)
                .collect(Collectors.joining(", "));

        net.kyori.adventure.text.Component summary = net.kyori.adventure.text.Component.text()
                .append(MessageUtils.parse("<dark_gray><st>----------------------------------------</st>", Map.of()))
                .append(net.kyori.adventure.text.Component.newline())
                .append(MessageUtils.parse("<gold><bold>Match Summary</bold></gold>", Map.of()))
                .append(net.kyori.adventure.text.Component.newline())
                .append(MessageUtils.parse("<yellow>Winners: <white>" + (wTeam != 0 ? winnerNames : "None"), Map.of()))
                .append(net.kyori.adventure.text.Component.newline())
                .append(MessageUtils.parse("<yellow>Losers: <white>" + (wTeam != 0 ? loserNames : "None"), Map.of()))
                .append(net.kyori.adventure.text.Component.newline())
                .append(MessageUtils.parse("<yellow>Kit: <white>" + kitName, Map.of()))
                .append(net.kyori.adventure.text.Component.newline())
                .append(MessageUtils.parse("<dark_gray><st>----------------------------------------</st>", Map.of()))
                .build();

        org.bukkit.Location spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
        java.util.stream.Stream.concat(duel.getTeam1().stream(), duel.getTeam2().stream()).forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.teleport(spawn);
                resetPlayer(p);
                p.sendMessage(summary);
                activeDuels.remove(uuid);
                invites.remove(uuid);
                endingDuels.remove(uuid);
                plugin.getQueueManager().resetPlayerState(p);

                dev.piyush.shyamduels.party.Party party = plugin.getPartyManager().getParty(p);
                if (party != null) {
                    plugin.getItemManager().givePartyItems(p, party);
                } else {
                    plugin.getItemManager().giveSpawnItems(p);
                }
            }
        });
        plugin.getSpectatorManager().endSpectateForDuel(duel);
        duel.setState(Duel.DuelState.ENDED);
        if (duel.getArena() != null) {
            duel.getArena().setStatus(Arena.ArenaStatus.REGENERATING);
            FaweUtils.pasteSchematic(duel.getArena());
        }
    }

    public void forceEnd(Duel duel) {
        finishDuel(duel);
    }

    public void forfeitDuel(Player player) {
        Duel duel = activeDuels.get(player.getUniqueId());
        if (duel == null)
            return;
        int forfeitTeam = duel.getTeam1().contains(player.getUniqueId()) ? 1 : 2;
        int winnerTeam = forfeitTeam == 1 ? 2 : 1;
        int neededWins = (duel.getMaxRounds() / 2) + 1;
        for (int i = 0; i < neededWins; i++) {
            duel.addRoundWin(winnerTeam);
        }

        finishDuel(duel);
    }

    private void resetPlayer(Player p) {
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        p.setHealth(20);
        p.setFoodLevel(20);
        p.getActivePotionEffects().forEach(e -> p.removePotionEffect(e.getType()));
        p.setFireTicks(0);
        p.setGameMode(org.bukkit.GameMode.SURVIVAL);
    }

    private final Map<UUID, InviteData> invites = new ConcurrentHashMap<>();
    private final Map<UUID, PartyInviteData> partyInvites = new ConcurrentHashMap<>();

    private record InviteData(UUID senderId, Kit kit, int rounds) {
    }

    private record PartyInviteData(UUID senderPartyId, UUID targetPartyId, Kit kit, long timestamp) {
    }

    public DuelManager(ShyamDuels plugin) {
        this.plugin = plugin;
    }

    public void sendPartyDuelInvite(dev.piyush.shyamduels.party.Party senderParty,
            dev.piyush.shyamduels.party.Party targetParty, Kit kit) {
        if (senderParty == null || targetParty == null)
            return;

        Player senderOwner = senderParty.getOwnerPlayer();
        Player targetOwner = targetParty.getOwnerPlayer();

        if (senderOwner == null || targetOwner == null) {
            if (senderOwner != null) {
                MessageUtils.sendMessage(senderOwner, "party.target-party-offline");
            }
            return;
        }

        if (!senderParty.isInLobby() || !targetParty.isInLobby()) {
            MessageUtils.sendMessage(senderOwner, "party.party-busy");
            return;
        }

        if (senderParty.getSize() != targetParty.getSize()) {
            MessageUtils.sendMessage(senderOwner, "party.size-mismatch",
                    Map.of("your_size", String.valueOf(senderParty.getSize()),
                            "their_size", String.valueOf(targetParty.getSize())));
            return;
        }

        PartyInviteData invite = new PartyInviteData(
                senderParty.getPartyId(), targetParty.getPartyId(), kit, System.currentTimeMillis());
        partyInvites.put(targetOwner.getUniqueId(), invite);

        MessageUtils.sendMessage(senderOwner, "party.duel-request-sent",
                Map.of("party", targetOwner.getName()));

        net.kyori.adventure.text.Component inviteMsg = net.kyori.adventure.text.Component.text()
                .append(MessageUtils.parse(
                        MessageUtils.get("party.duel-request-received"),
                        Map.of("party", senderOwner.getName(), "kit", kit.getName())))
                .append(net.kyori.adventure.text.Component.newline())
                .append(net.kyori.adventure.text.Component
                        .text("[ACCEPT DUEL]", net.kyori.adventure.text.format.NamedTextColor.GREEN,
                                net.kyori.adventure.text.format.TextDecoration.BOLD)
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent
                                .runCommand("/party duel accept"))
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent
                                .showText(net.kyori.adventure.text.Component.text("Click to accept party duel",
                                        net.kyori.adventure.text.format.NamedTextColor.GRAY))))
                .build();

        targetOwner.sendMessage(inviteMsg);
    }

    public void acceptPartyDuelInvite(Player acceptor) {
        PartyInviteData invite = partyInvites.remove(acceptor.getUniqueId());
        if (invite == null) {
            MessageUtils.sendMessage(acceptor, "party.no-duel-invite");
            return;
        }

        long elapsed = System.currentTimeMillis() - invite.timestamp();
        if (elapsed > 60000) {
            MessageUtils.sendMessage(acceptor, "party.duel-invite-expired");
            return;
        }

        dev.piyush.shyamduels.party.Party senderParty = plugin.getPartyManager().getPartyById(invite.senderPartyId());
        dev.piyush.shyamduels.party.Party targetParty = plugin.getPartyManager().getPartyById(invite.targetPartyId());

        if (senderParty == null || targetParty == null) {
            MessageUtils.sendMessage(acceptor, "party.party-no-longer-exists");
            return;
        }

        if (!senderParty.isInLobby() || !targetParty.isInLobby()) {
            MessageUtils.sendMessage(acceptor, "party.party-busy");
            return;
        }

        startPartyDuel(senderParty, targetParty, invite.kit());
    }

    public void startPartyDuel(dev.piyush.shyamduels.party.Party team1Party,
            dev.piyush.shyamduels.party.Party team2Party, Kit kit) {
        List<Player> team1 = team1Party.getOnlineMembers();
        List<Player> team2 = team2Party.getOnlineMembers();

        team1Party.setState(dev.piyush.shyamduels.party.PartyState.IN_DUEL);
        team2Party.setState(dev.piyush.shyamduels.party.PartyState.IN_DUEL);

        dev.piyush.shyamduels.queue.QueueMode mode = switch (team1.size()) {
            case 2 -> dev.piyush.shyamduels.queue.QueueMode.TWO_V_TWO;
            case 3 -> dev.piyush.shyamduels.queue.QueueMode.THREE_V_THREE;
            case 4 -> dev.piyush.shyamduels.queue.QueueMode.FOUR_V_FOUR;
            default -> dev.piyush.shyamduels.queue.QueueMode.ONE_V_ONE;
        };

        startDuel(team1, team2, kit, mode, 1);
    }

    public void sendInvite(Player sender, Player target, Kit kit, int rounds) {
        if (activeDuels.containsKey(sender.getUniqueId()) || activeDuels.containsKey(target.getUniqueId())) {
            MessageUtils.sendMessage(sender, "duel.already-in-match", Map.of());
            return;
        }

        invites.put(target.getUniqueId(), new InviteData(sender.getUniqueId(), kit, rounds));
        MessageUtils.sendMessage(sender, "duel.invite.sent",
                Map.of("player", target.getName(), "kit", kit.getName(), "rounds", String.valueOf(rounds)));

        net.kyori.adventure.text.Component inviteMsg = net.kyori.adventure.text.Component.text()
                .append(MessageUtils.parse(
                        MessageUtils.get("duel.invite.received"),
                        Map.of("player", sender.getName(), "kit", kit.getName(), "rounds", String.valueOf(rounds))))
                .append(net.kyori.adventure.text.Component.newline())
                .append(net.kyori.adventure.text.Component
                        .text("[ACCEPT DUEL]", net.kyori.adventure.text.format.NamedTextColor.GREEN,
                                net.kyori.adventure.text.format.TextDecoration.BOLD)
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent
                                .runCommand("/duel accept " + sender.getName()))
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent
                                .showText(net.kyori.adventure.text.Component.text("Click to accept duel",
                                        net.kyori.adventure.text.format.NamedTextColor.GRAY))))
                .build();

        target.sendMessage(inviteMsg);
        MessageUtils.sendActionBar(target, "duel.invite.action-bar", Map.of("player", sender.getName()));
    }

    public void acceptInvite(Player acceptor, Player sender) {
        if (!invites.containsKey(acceptor.getUniqueId())) {
            MessageUtils.sendMessage(acceptor, "duel.no-invite", Map.of());
            return;
        }

        InviteData data = invites.get(acceptor.getUniqueId());
        if (!data.senderId().equals(sender.getUniqueId())) {
            MessageUtils.sendMessage(acceptor, "duel.no-invite", Map.of());
            return;
        }
        invites.remove(acceptor.getUniqueId());
        startDuel(Collections.singletonList(sender), Collections.singletonList(acceptor), data.kit(),
                QueueMode.ONE_V_ONE, data.rounds());
    }

    public void startDuel(Player p1, Player p2, Kit kit, int rounds) {
        startDuel(Collections.singletonList(p1), Collections.singletonList(p2), kit, QueueMode.ONE_V_ONE, rounds);
    }

    public void startDuel(List<Player> team1, List<Player> team2, Kit kit, QueueMode mode, int rounds) {
        Arena arena = plugin.getArenaManager().getAvailableArena(kit.getName());
        if (arena == null) {
            team1.forEach(p -> MessageUtils.sendMessage(p, "duel.no-arenas"));
            team2.forEach(p -> MessageUtils.sendMessage(p, "duel.no-arenas"));
            return;
        }

        arena.setStatus(Arena.ArenaStatus.IN_USE);

        List<UUID> t1Ids = team1.stream().map(Player::getUniqueId).toList();
        List<UUID> t2Ids = team2.stream().map(Player::getUniqueId).toList();

        Duel duel = new Duel(t1Ids, t2Ids, mode, rounds);
        duel.setArena(arena);
        duel.setKit(kit);
        team1.forEach(p -> {
            if (arena.getSpawn1() != null)
                p.teleport(arena.getSpawn1());
            applyKit(p, kit);
            activeDuels.put(p.getUniqueId(), duel);
        });

        team2.forEach(p -> {
            if (arena.getSpawn2() != null)
                p.teleport(arena.getSpawn2());
            applyKit(p, kit);
            activeDuels.put(p.getUniqueId(), duel);
        });

        startDuelCountdown(duel);
    }

    private void startDuelCountdown(Duel duel) {
        new org.bukkit.scheduler.BukkitRunnable() {
            int seconds = 5;

            @Override
            public void run() {
                if (duel.getState() == Duel.DuelState.ENDED) {
                    this.cancel();
                    return;
                }

                if (seconds > 0) {
                    net.kyori.adventure.title.Title title = net.kyori.adventure.title.Title.title(
                            net.kyori.adventure.text.Component.text(seconds,
                                    net.kyori.adventure.text.format.NamedTextColor.RED),
                            net.kyori.adventure.text.Component.empty());

                    playGlobalSound(duel, org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                    sendGlobalTitle(duel, title);

                    seconds--;
                } else {
                    duel.setState(Duel.DuelState.FIGHTING);

                    net.kyori.adventure.title.Title title = net.kyori.adventure.title.Title.title(
                            net.kyori.adventure.text.Component.text("FIGHT!",
                                    net.kyori.adventure.text.format.NamedTextColor.GREEN),
                            net.kyori.adventure.text.Component.empty());

                    playGlobalSound(duel, org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                    sendGlobalTitle(duel, title);
                    java.util.stream.Stream.concat(duel.getTeam1().stream(), duel.getTeam2().stream()).forEach(uuid -> {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null)
                            MessageUtils.sendMessage(p, "duel.started",
                                    Map.of("kit", duel.getKit().getName(), "player1", "Team 1", "player2", "Team 2"));
                    });

                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void playGlobalSound(Duel duel, org.bukkit.Sound sound, float v, float p) {
        java.util.stream.Stream.concat(duel.getTeam1().stream(), duel.getTeam2().stream()).forEach(uuid -> {
            Player pl = Bukkit.getPlayer(uuid);
            if (pl != null)
                pl.playSound(pl.getLocation(), sound, v, p);
        });
    }

    private void sendGlobalTitle(Duel duel, net.kyori.adventure.title.Title title) {
        java.util.stream.Stream.concat(duel.getTeam1().stream(), duel.getTeam2().stream()).forEach(uuid -> {
            Player pl = Bukkit.getPlayer(uuid);
            if (pl != null)
                pl.showTitle(title);
        });
    }

    public boolean isInDuel(Player player) {
        return activeDuels.containsKey(player.getUniqueId());
    }

    public Duel getDuel(Player player) {
        return activeDuels.get(player.getUniqueId());
    }

    private void applyKit(Player p, Kit kit) {
        PlayerKit playerKit = plugin.getKitManager().getPlayerKit(p.getUniqueId(), kit.getName());
        if (playerKit != null) {
            p.getInventory().setContents(playerKit.getInventory());
            p.getInventory().setArmorContents(playerKit.getArmor());
            if (playerKit.getOffhand() != null)
                p.getInventory().setItemInOffHand(playerKit.getOffhand());
        } else {
            p.getInventory().setContents(kit.getInventory());
            p.getInventory().setArmorContents(kit.getArmor());
        }
        p.getActivePotionEffects().forEach(e -> p.removePotionEffect(e.getType()));
        p.addPotionEffects(kit.getEffects());
        p.setHealth(20);
        p.setFoodLevel(20);
    }

    public Player getOpponent(Player player) {
        Duel duel = getDuel(player);
        return duel != null ? duel.getOpponent(player) : null;
    }

    public String getSelfRelation(Player player) {
        return MessageUtils.parseLegacy("<green>[A]</green>");
    }

    public String getRelationPlaceholder(Player viewer, Player target) {
        Duel duel = getDuel(viewer);
        if (duel == null || !duel.hasPlayer(target))
            return "§f";
        boolean viewerInTeam1 = duel.getTeam1().contains(viewer.getUniqueId());
        boolean targetInTeam1 = duel.getTeam1().contains(target.getUniqueId());

        if (viewerInTeam1 == targetInTeam1) {
            return MessageUtils.parseLegacy("<green>[A]</green>");
        } else {
            return MessageUtils.parseLegacy("<red>[O]</red>");
        }
    }

    public List<Duel> getActiveDuels() {
        return activeDuels.values().stream()
                .distinct()
                .collect(Collectors.toList());
    }
}
