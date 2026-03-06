package dev.piyush.shyamduels.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.kit.Kit;
import dev.piyush.shyamduels.kit.KitManager;
import dev.piyush.shyamduels.util.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@CommandAlias("kit|shyamkits")
@CommandPermission("shyamduels.admin")
public class KitCommand extends BaseCommand {

    private final KitManager kitManager;

    public KitCommand(ShyamDuels plugin) {
        this.kitManager = plugin.getKitManager();
    }

    @Subcommand("create")
    @Syntax("<name>")
    @Description("Create a kit from your inventory")
    public void onCreate(Player player, String name) {
        if (kitManager.getKit(name) != null) {
            MessageUtils.sendMessage(player, "kit.exists", Map.of("name", name));
            return;
        }

        Kit kit = new Kit(name);
        kit.setInventory(player.getInventory().getContents());
        kit.setArmor(player.getInventory().getArmorContents());
        kit.setEffects(new ArrayList<>(player.getActivePotionEffects()));

        kit.setIcon(new ItemStack(Material.DIAMOND_SWORD));

        kitManager.addKit(kit);

        MessageUtils.sendMessage(player, "kit.created", Map.of("name", name));
    }

    @Subcommand("delete")
    @CommandCompletion("@kits")
    public void onDelete(Player player, String name) {
        if (kitManager.getKit(name) == null) {
            MessageUtils.sendMessage(player, "kit.not-found", Map.of("name", name));
            return;
        }
        kitManager.deleteKit(name);
        MessageUtils.sendMessage(player, "kit.deleted", Map.of("name", name));
    }

    @Subcommand("list")
    public void onList(Player player) {
        String kits = kitManager.getKits().stream().map(Kit::getName).collect(Collectors.joining(", "));
        MessageUtils.sendMessage(player, "general.usage", Map.of("usage", "Kits: " + kits));
    }

    @Subcommand("load")
    @CommandCompletion("@kits")
    public void onLoad(Player player, String name) {
        Kit kit = kitManager.getKit(name);
        if (kit == null) {
            MessageUtils.sendMessage(player, "kit.not-found", Map.of("name", name));
            return;
        }

        player.getInventory().setContents(kit.getInventory());
        player.getInventory().setArmorContents(kit.getArmor());
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.addPotionEffects(kit.getEffects());

        MessageUtils.sendMessage(player, "general.usage", Map.of("usage", "Kit loaded."));
    }

    @Subcommand("seticon")
    @CommandCompletion("@kits")
    public void onSetIcon(Player player, String name) {
        Kit kit = kitManager.getKit(name);
        if (kit == null)
            return;

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType().isAir()) {
            MessageUtils.sendRawMessage(player, "<red>Hold an item to set as icon.</red>", Map.of());
            return;
        }

        kit.setIcon(hand.clone());
        kitManager.saveKit(kit);
        MessageUtils.sendMessage(player, "general.usage", Map.of("usage", "Icon set."));
    }

    @Subcommand("setitem")
    @CommandCompletion("@kits")
    @Description("Set the kit icon to held item with its display name")
    public void onSetItem(Player player, String name) {
        Kit kit = kitManager.getKit(name);
        if (kit == null) {
            MessageUtils.sendMessage(player, "kit.not-found", Map.of("name", name));
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType().isAir()) {
            MessageUtils.sendRawMessage(player, "<red>Hold an item to set.</red>", Map.of());
            return;
        }

        kit.setIcon(hand.clone());
        kitManager.saveKit(kit);
        MessageUtils.sendMessage(player, "general.usage", Map.of("usage", "Kit item set to held item."));
    }

    @Subcommand("setinv")
    @CommandCompletion("@kits")
    @Syntax("<name>")
    @Description("Update a kit's inventory with your current items")
    public void onSetInv(Player player, String name) {
        Kit kit = kitManager.getKit(name);
        if (kit == null) {
            MessageUtils.sendMessage(player, "kit.not-found", Map.of("name", name));
            return;
        }

        kit.setInventory(player.getInventory().getContents());
        kit.setArmor(player.getInventory().getArmorContents());
        kit.setEffects(new ArrayList<>(player.getActivePotionEffects()));

        kitManager.saveKit(kit);
        MessageUtils.sendMessage(player, "general.usage",
                Map.of("usage", "Kit inventory updated from your current items."));
    }

    @Subcommand("allowblock")
    @CommandCompletion("@kits")
    @Description("Allow the block in hand to be placed/broken in this kit's duels")
    public void onAllowBlock(Player player, String name) {
        Kit kit = kitManager.getKit(name);
        if (kit == null) {
            MessageUtils.sendMessage(player, "kit.not-found", Map.of("name", name));
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType().isAir()) {
            MessageUtils.sendRawMessage(player, "<red>Hold a block to allow.</red>", Map.of());
            return;
        }

        if (!kit.getBuildWhitelist().contains(hand.getType())) {
            kit.getBuildWhitelist().add(hand.getType());
            kitManager.saveKit(kit);
        }

        MessageUtils.sendMessage(player, "general.usage",
                Map.of("usage", "Added " + hand.getType().name() + " to whitelist for kit " + name));
    }

    @Subcommand("removeblock")
    @CommandCompletion("@kits")
    @Description("Remove the block in hand from whitelist")
    public void onRemoveBlock(Player player, String name) {
        Kit kit = kitManager.getKit(name);
        if (kit == null) {
            MessageUtils.sendMessage(player, "kit.not-found", Map.of("name", name));
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType().isAir()) {
            MessageUtils.sendRawMessage(player, "<red>Hold a block to remove.</red>", Map.of());
            return;
        }

        if (kit.getBuildWhitelist().contains(hand.getType())) {
            kit.getBuildWhitelist().remove(hand.getType());
            kitManager.saveKit(kit);
        }

        MessageUtils.sendMessage(player, "general.usage",
                Map.of("usage", "Removed " + hand.getType().name() + " from whitelist for kit " + name));
    }

    @Subcommand("deleteeditedkits")
    @CommandCompletion("@kits")
    @Syntax("<kitname>")
    @Description("Delete all player edited kits for a specific kit")
    public void onDeleteEditedKits(Player player, String kitName) {
        Kit kit = kitManager.getKit(kitName);
        if (kit == null) {
            MessageUtils.sendMessage(player, "kit.not-found", Map.of("name", kitName));
            return;
        }

        int deleted = kitManager.deleteAllPlayerKits(kitName);
        MessageUtils.sendMessage(player, "kit.edited-kits-deleted",
            Map.of("name", kitName, "count", String.valueOf(deleted)));
    }
}
