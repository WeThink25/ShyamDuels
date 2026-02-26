package dev.piyush.shyamduels.party;

import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PartyManager {

    private final ShyamDuels plugin;
    private final Map<UUID, Party> playerToParty = new ConcurrentHashMap<>();
    private final Map<UUID, Party> parties = new ConcurrentHashMap<>();
    private final Map<UUID, PartyInvite> pendingInvites = new ConcurrentHashMap<>();

    public PartyManager(ShyamDuels plugin) {
        this.plugin = plugin;
        startInviteCleanupTask();
    }

    public Party createParty(Player owner) {
        if (isInParty(owner)) {
            MessageUtils.sendMessage(owner, "party.already-in-party");
            return null;
        }

        Party party = new Party(owner.getUniqueId());
        parties.put(party.getPartyId(), party);
        playerToParty.put(owner.getUniqueId(), party);

        MessageUtils.sendMessage(owner, "party.created");

        plugin.getItemManager().givePartyItems(owner, party);

        return party;
    }

    public void disbandParty(Party party) {
        if (party == null)
            return;

        for (UUID uuid : party.getMembers()) {
            playerToParty.remove(uuid);
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                MessageUtils.sendMessage(player, "party.disbanded");
                plugin.getItemManager().giveSpawnItems(player);
            }
        }

        parties.remove(party.getPartyId());
        cleanupInvitesForParty(party.getPartyId());
    }

    public void invitePlayer(Player sender, Player target) {
        Party party = getParty(sender);

        if (party == null) {
            MessageUtils.sendMessage(sender, "party.not-in-party");
            return;
        }

        if (!party.isOwner(sender.getUniqueId())) {
            MessageUtils.sendMessage(sender, "party.not-owner");
            return;
        }

        if (isInParty(target)) {
            MessageUtils.sendMessage(sender, "party.target-already-in-party", Map.of("player", target.getName()));
            return;
        }

        int maxSize = getMaxPartySize(sender);
        if (party.getSize() >= maxSize) {
            MessageUtils.sendMessage(sender, "party.party-full");
            return;
        }

        if (hasPendingInvite(target)) {
            MessageUtils.sendMessage(sender, "party.target-has-pending-invite", Map.of("player", target.getName()));
            return;
        }

        long expiration = plugin.getConfig().getLong("party.invite-expiration", 60);
        PartyInvite invite = new PartyInvite(party.getPartyId(), sender.getUniqueId(), target.getUniqueId(),
                expiration);
        pendingInvites.put(target.getUniqueId(), invite);

        MessageUtils.sendMessage(sender, "party.invited", Map.of("player", target.getName()));

        net.kyori.adventure.text.Component inviteMsg = net.kyori.adventure.text.Component.text()
                .append(MessageUtils.parse(
                        MessageUtils.get("party.received-invite"),
                        Map.of("player", sender.getName())))
                .append(net.kyori.adventure.text.Component.newline())
                .append(net.kyori.adventure.text.Component
                        .text("[ACCEPT]", net.kyori.adventure.text.format.NamedTextColor.GREEN,
                                net.kyori.adventure.text.format.TextDecoration.BOLD)
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/party accept"))
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent
                                .showText(net.kyori.adventure.text.Component.text("Click to accept",
                                        net.kyori.adventure.text.format.NamedTextColor.GRAY))))
                .append(net.kyori.adventure.text.Component.text(" "))
                .append(net.kyori.adventure.text.Component
                        .text("[DENY]", net.kyori.adventure.text.format.NamedTextColor.RED,
                                net.kyori.adventure.text.format.TextDecoration.BOLD)
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/party deny"))
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent
                                .showText(net.kyori.adventure.text.Component.text("Click to deny",
                                        net.kyori.adventure.text.format.NamedTextColor.GRAY))))
                .build();

        target.sendMessage(inviteMsg);
    }

    public void acceptInvite(Player player) {
        PartyInvite invite = pendingInvites.remove(player.getUniqueId());

        if (invite == null || invite.isExpired()) {
            MessageUtils.sendMessage(player, "party.no-invite");
            return;
        }

        if (isInParty(player)) {
            MessageUtils.sendMessage(player, "party.already-in-party");
            return;
        }

        Party party = parties.get(invite.getPartyId());
        if (party == null) {
            MessageUtils.sendMessage(player, "party.party-no-longer-exists");
            return;
        }

        Player sender = Bukkit.getPlayer(invite.getSenderId());
        int maxSize = sender != null ? getMaxPartySize(sender) : getMaxPartySize(party.getOwnerPlayer());
        if (party.getSize() >= maxSize) {
            MessageUtils.sendMessage(player, "party.party-full");
            return;
        }

        party.addMember(player.getUniqueId());
        playerToParty.put(player.getUniqueId(), party);

        Player ownerPlayer = party.getOwnerPlayer();
        String ownerName = ownerPlayer != null ? ownerPlayer.getName() : "Unknown";
        MessageUtils.sendMessage(player, "party.joined", Map.of("player", ownerName));

        party.broadcast(
                MessageUtils.parse(MessageUtils.get("party.player-joined"), Map.of("player", player.getName())));

        plugin.getItemManager().givePartyItems(player, party);
    }

    public void denyInvite(Player player) {
        PartyInvite invite = pendingInvites.remove(player.getUniqueId());
        if (invite == null) {
            MessageUtils.sendMessage(player, "party.no-invite");
            return;
        }

        MessageUtils.sendMessage(player, "party.invite-denied");

        Player sender = Bukkit.getPlayer(invite.getSenderId());
        if (sender != null) {
            MessageUtils.sendMessage(sender, "party.invite-denied-sender", Map.of("player", player.getName()));
        }
    }

    public void joinPublicParty(Player player, Player targetOwner) {
        if (isInParty(player)) {
            MessageUtils.sendMessage(player, "party.already-in-party");
            return;
        }

        Party party = getParty(targetOwner);
        if (party == null) {
            MessageUtils.sendMessage(player, "party.target-not-in-party");
            return;
        }

        if (!party.isPublic()) {
            MessageUtils.sendMessage(player, "party.party-private");
            return;
        }

        if (!party.isInLobby()) {
            if (party.isInQueue()) {
                MessageUtils.sendMessage(player, "party.party-in-queue");
            } else if (party.isInDuel()) {
                MessageUtils.sendMessage(player, "party.party-in-duel");
            }
            return;
        }

        int maxSize = getMaxPartySize(party.getOwnerPlayer());
        if (party.getSize() >= maxSize) {
            MessageUtils.sendMessage(player, "party.party-full");
            return;
        }

        party.addMember(player.getUniqueId());
        playerToParty.put(player.getUniqueId(), party);

        MessageUtils.sendMessage(player, "party.joined", Map.of("player", targetOwner.getName()));
        party.broadcast(
                MessageUtils.parse(MessageUtils.get("party.player-joined"), Map.of("player", player.getName())));

        plugin.getItemManager().givePartyItems(player, party);
    }

    public void leaveParty(Player player) {
        Party party = getParty(player);
        if (party == null) {
            MessageUtils.sendMessage(player, "party.not-in-party");
            return;
        }

        party.removeMember(player.getUniqueId());
        playerToParty.remove(player.getUniqueId());

        MessageUtils.sendMessage(player, "party.left");
        plugin.getItemManager().giveSpawnItems(player);

        party.broadcast(MessageUtils.parse(MessageUtils.get("party.player-left"), Map.of("player", player.getName())));

        if (party.isOwner(player.getUniqueId())) {
            handleOwnerLeave(party);
        }

        if (party.getSize() == 0) {
            parties.remove(party.getPartyId());
        }
    }

    private void handleOwnerLeave(Party party) {
        boolean transferOwnership = plugin.getConfig().getBoolean("party.transfer-ownership-on-leave", true);

        if (transferOwnership && party.getSize() > 0) {
            UUID newOwner = party.getNextOwnerCandidate();
            if (newOwner != null) {
                party.setOwner(newOwner);
                Player newOwnerPlayer = Bukkit.getPlayer(newOwner);
                String newOwnerName = newOwnerPlayer != null ? newOwnerPlayer.getName() : "Unknown";
                party.broadcast(MessageUtils.parse(MessageUtils.get("party.new-owner"),
                        Map.of("player", newOwnerName)));
                
                if (newOwnerPlayer != null) {
                    plugin.getItemManager().givePartyItems(newOwnerPlayer, party);
                }
                
                for (Player member : party.getOnlineMembers()) {
                    if (!member.getUniqueId().equals(newOwner)) {
                        plugin.getItemManager().givePartyItems(member, party);
                    }
                }
                return;
            }
        }

        disbandParty(party);
    }

    public void kickPlayer(Player owner, Player target) {
        Party party = getParty(owner);
        if (party == null) {
            MessageUtils.sendMessage(owner, "party.not-in-party");
            return;
        }

        if (!party.isOwner(owner.getUniqueId())) {
            MessageUtils.sendMessage(owner, "party.not-owner");
            return;
        }

        if (!party.isMember(target.getUniqueId())) {
            MessageUtils.sendMessage(owner, "party.player-not-in-party", Map.of("player", target.getName()));
            return;
        }

        if (party.isOwner(target.getUniqueId())) {
            MessageUtils.sendMessage(owner, "party.cannot-kick-self");
            return;
        }

        party.removeMember(target.getUniqueId());
        playerToParty.remove(target.getUniqueId());

        MessageUtils.sendMessage(target, "party.kicked");
        plugin.getItemManager().giveSpawnItems(target);

        party.broadcast(
                MessageUtils.parse(MessageUtils.get("party.player-kicked"), Map.of("player", target.getName())));
    }

    public void setPublic(Player player, boolean isPublic) {
        Party party = getParty(player);
        if (party == null) {
            MessageUtils.sendMessage(player, "party.not-in-party");
            return;
        }

        if (!party.isOwner(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "party.not-owner");
            return;
        }

        party.setMode(isPublic ? PartyMode.PUBLIC : PartyMode.PRIVATE);
        MessageUtils.sendMessage(player, isPublic ? "party.now-public" : "party.now-private");
    }

    public void toggleChat(Player player) {
        Party party = getParty(player);
        if (party == null) {
            MessageUtils.sendMessage(player, "party.not-in-party");
            return;
        }

        party.toggleChat();
        MessageUtils.sendMessage(player, party.isChatEnabled() ? "party.chat-enabled" : "party.chat-disabled");
    }

    public boolean isInParty(Player player) {
        return playerToParty.containsKey(player.getUniqueId());
    }

    public Party getParty(Player player) {
        return playerToParty.get(player.getUniqueId());
    }

    public Party getPartyById(UUID partyId) {
        return parties.get(partyId);
    }

    public Collection<Party> getAllParties() {
        return Collections.unmodifiableCollection(parties.values());
    }

    public List<Party> getPublicParties() {
        List<Party> publicParties = new ArrayList<>();
        for (Party party : parties.values()) {
            if (party.isPublic() && party.isInLobby()) {
                publicParties.add(party);
            }
        }
        return publicParties;
    }

    public int getMaxPartySize(Player player) {
        if (player == null) {
            return plugin.getConfig().getInt("party.default-max-size", 4);
        }

        String vipPerm = plugin.getConfig().getString("party.vip-permission", "shyamduels.vip");
        if (player.hasPermission(vipPerm)) {
            return plugin.getConfig().getInt("party.vip-max-size", 8);
        }

        return plugin.getConfig().getInt("party.default-max-size", 4);
    }

    public boolean hasPendingInvite(Player player) {
        PartyInvite invite = pendingInvites.get(player.getUniqueId());
        if (invite != null && invite.isExpired()) {
            pendingInvites.remove(player.getUniqueId());
            return false;
        }
        return invite != null;
    }

    private void cleanupInvitesForParty(UUID partyId) {
        pendingInvites.entrySet().removeIf(entry -> entry.getValue().getPartyId().equals(partyId));
    }

    private void startInviteCleanupTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            pendingInvites.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }, 20L * 30, 20L * 30);
    }

    public void handlePlayerDisconnect(Player player) {
        Party party = getParty(player);
        if (party == null)
            return;

        party.removeMember(player.getUniqueId());
        playerToParty.remove(player.getUniqueId());

        party.broadcast(MessageUtils.parse(MessageUtils.get("party.player-left"), Map.of("player", player.getName())));

        if (party.isOwner(player.getUniqueId())) {
            handleOwnerLeave(party);
        }

        if (party.getSize() == 0) {
            parties.remove(party.getPartyId());
        }
    }
}
