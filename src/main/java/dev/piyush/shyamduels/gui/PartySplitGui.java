package dev.piyush.shyamduels.gui;

import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.party.Party;
import dev.piyush.shyamduels.party.PartySplitManager;
import dev.piyush.shyamduels.util.MessageUtils;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PartySplitGui extends FastInv {

    private final ShyamDuels plugin;
    private final Player player;
    private final Party party;
    private final PartySplitManager.PartySplit split;

    public PartySplitGui(ShyamDuels plugin, Player player, Party party) {
        super(54, MessageUtils.parseLegacy("&d&lParty Split"));
        this.plugin = plugin;
        this.player = player;
        this.party = party;

        if (!plugin.getPartySplitManager().hasSplit(party)) {
            plugin.getPartySplitManager().createSplit(party);
        }
        this.split = plugin.getPartySplitManager().getSplit(party);

        setupItems();
    }

    private void setupItems() {
        ItemStack border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();

        for (int i = 0; i < getInventory().getSize(); i++) {
            setItem(i, border);
        }

        ItemStack teamABorder = new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                .name(MessageUtils.parseLegacy("&c&lTeam A"))
                .build();

        ItemStack teamBBorder = new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE)
                .name(MessageUtils.parseLegacy("&9&lTeam B"))
                .build();

        for (int i = 0; i < 9; i++) {
            setItem(i, teamABorder);
        }
        for (int i = 45; i < 54; i++) {
            setItem(i, teamBBorder);
        }

        List<UUID> members = new ArrayList<>(party.getMembers());
        int[] memberSlots = { 19, 20, 21, 22, 23, 24, 25 };
        int idx = 0;

        for (UUID uuid : members) {
            if (idx >= memberSlots.length)
                break;
            Player member = Bukkit.getPlayer(uuid);
            if (member == null)
                continue;

            String teamStatus;
            Material mat;
            if (split.isInTeamA(uuid)) {
                teamStatus = "&c[Team A]";
                mat = Material.RED_WOOL;
            } else if (split.isInTeamB(uuid)) {
                teamStatus = "&9[Team B]";
                mat = Material.BLUE_WOOL;
            } else {
                teamStatus = "&7[Unassigned]";
                mat = Material.WHITE_WOOL;
            }

            ItemStack head = new ItemBuilder(mat)
                    .name(MessageUtils.parseLegacy("&e" + member.getName()))
                    .lore(
                            MessageUtils.parseLegacy("&7"),
                            MessageUtils.parseLegacy(teamStatus),
                            MessageUtils.parseLegacy("&7"),
                            MessageUtils.parseLegacy("&aLeft-click: Team A"),
                            MessageUtils.parseLegacy("&9Right-click: Team B"))
                    .build();

            int slot = memberSlots[idx];
            UUID memberUuid = uuid;
            setItem(slot, head, e -> {
                if (e.isLeftClick()) {
                    split.addToTeamA(memberUuid);
                } else if (e.isRightClick()) {
                    split.addToTeamB(memberUuid);
                }
                setupItems();
            });
            idx++;
        }

        setItem(37, new ItemBuilder(Material.HOPPER)
                .name(MessageUtils.parseLegacy("&e&lAuto Split"))
                .lore(
                        MessageUtils.parseLegacy("&7"),
                        MessageUtils.parseLegacy("&7Randomly assign members"),
                        MessageUtils.parseLegacy("&7to Team A and Team B"))
                .build(), e -> {
                    split.autoSplit();
                    setupItems();
                });

        boolean valid = split.isValid();
        Material startMat = valid ? Material.LIME_DYE : Material.GRAY_DYE;
        String startName = valid ? "&a&lStart Duel" : "&c&lTeams Not Ready";

        setItem(40, new ItemBuilder(startMat)
                .name(MessageUtils.parseLegacy(startName))
                .lore(
                        MessageUtils.parseLegacy("&7"),
                        MessageUtils.parseLegacy("&7Team A: &c" + split.getTeamA().size()),
                        MessageUtils.parseLegacy("&7Team B: &9" + split.getTeamB().size()),
                        MessageUtils.parseLegacy("&7"),
                        MessageUtils.parseLegacy(valid ? "&eClick to select kit!" : "&cTeams must be equal!"))
                .build(), e -> {
                    if (split.isValid()) {
                        player.closeInventory();
                        new PartySplitKitGui(plugin, player, party, split).open(player);
                    }
                });

        setItem(43, new ItemBuilder(Material.BARRIER)
                .name(MessageUtils.parseLegacy("&c&lCancel"))
                .lore(MessageUtils.parseLegacy("&7Cancel and return"))
                .build(), e -> {
                    plugin.getPartySplitManager().removeSplit(party);
                    player.closeInventory();
                });
    }

    @Override
    protected void onClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        event.setCancelled(true);
        super.onClick(event);
    }
}
