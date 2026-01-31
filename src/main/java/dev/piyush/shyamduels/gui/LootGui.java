package dev.piyush.shyamduels.gui;

import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.util.MessageUtils;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LootGui extends FastInv {

    private final ShyamDuels plugin;
    private final List<ItemStack> drops;
    private boolean closed = false;

    public LootGui(ShyamDuels plugin, Player player, String victimName, List<ItemStack> drops) {
        super(54, "Looting: " + victimName);
        this.plugin = plugin;
        this.drops = drops;

        initializeItems();
    }

    private void initializeItems() {
        int slot = 0;
        for (ItemStack item : drops) {
            if (item != null && item.getType() != Material.AIR && slot < 54) {
                getInventory().setItem(slot++, item);
            }
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == getInventory()) {
            event.setCancelled(false);
        } else if (event.getClickedInventory() == event.getWhoClicked().getInventory()) {
            event.setCancelled(false);
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        closed = true;
    }
}
