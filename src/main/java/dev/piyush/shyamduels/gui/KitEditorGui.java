package dev.piyush.shyamduels.gui;

import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.config.GuiConfigLoader;
import dev.piyush.shyamduels.kit.Kit;
import dev.piyush.shyamduels.kit.PlayerKit;
import dev.piyush.shyamduels.util.ItemBuilder;
import dev.piyush.shyamduels.util.MessageUtils;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;

public class KitEditorGui extends FastInv {

    private static final String GUI_KEY = "kit-editor";

    private final ShyamDuels plugin;
    private final Player player;
    private final Kit kit;
    private final PlayerKit playerKit;
    private final ItemStack[] backupInventory;
    private final ItemStack[] backupArmor;
    private final ItemStack backupOffhand;
    private boolean skipCloseTitle = false;

    private int slotInfo;
    private int slotSave;
    private int slotReset;
    private int slotExit;
    private int slotOffhand;
    private int[] slotArmor;

    public KitEditorGui(ShyamDuels plugin, Player player, Kit kit) {
        super(getGuiLoader().getSize(GUI_KEY),
                getGuiLoader().getTitle(GUI_KEY, Map.of("kit", kit.getName())));
        this.plugin = plugin;
        this.player = player;
        this.kit = kit;
        this.playerKit = plugin.getKitManager().getPlayerKit(player.getUniqueId(), kit.getName());
        this.backupInventory = player.getInventory().getContents();
        this.backupArmor = player.getInventory().getArmorContents();
        this.backupOffhand = player.getInventory().getItemInOffHand();

        loadSlots();
        initializeItems();

        addCloseHandler(e -> {
            if (!skipCloseTitle) {
                MessageUtils.sendTitle(player, "gui.kit-editor.title.exit");
            }
        });
    }

    private static GuiConfigLoader getGuiLoader() {
        return ShyamDuels.getInstance().getGuiConfigLoader();
    }

    private void loadSlots() {
        GuiConfigLoader loader = getGuiLoader();
        slotInfo = loader.getSlot(GUI_KEY, "info");
        slotSave = loader.getSlot(GUI_KEY, "save");
        slotReset = loader.getSlot(GUI_KEY, "reset");
        slotExit = loader.getSlot(GUI_KEY, "exit");
        slotOffhand = loader.getSlot(GUI_KEY, "offhand");
        slotArmor = loader.getSlots(GUI_KEY, "slots.armor");
        if (slotArmor.length < 4) {
            slotArmor = new int[] { 0, 1, 2, 3 };
        }
    }

    private void initializeItems() {
        GuiConfigLoader loader = getGuiLoader();

        ItemStack[] invContent = kit.getInventory();
        ItemStack[] armorContent = kit.getArmor();
        ItemStack offhandContent = kit.getOffhand();

        if (playerKit != null) {
            invContent = playerKit.getInventory();
            armorContent = playerKit.getArmor();
            offhandContent = playerKit.getOffhand();
        }

        if (armorContent != null && armorContent.length >= 4) {
            setItem(slotArmor[0], armorContent[0]);
            setItem(slotArmor[1], armorContent[1]);
            setItem(slotArmor[2], armorContent[2]);
            setItem(slotArmor[3], armorContent[3]);
        }

        if (offhandContent != null && offhandContent.getType() != Material.AIR) {
            setItem(slotOffhand, offhandContent);
        } else {
            setItem(slotOffhand, loader.buildItemFromSection(GUI_KEY, "offhand", Material.SHIELD, Map.of()));
        }

        if (invContent != null) {
            for (int i = 0; i < Math.min(invContent.length, 36); i++) {
                if (invContent[i] != null) {
                    setItem(9 + i, invContent[i]);
                }
            }
        }

        setItem(slotInfo, loader.buildItemFromSection(GUI_KEY, "info", Material.BOOK,
                Map.of("kit", kit.getName())));
        setItem(slotSave, loader.buildItemFromSection(GUI_KEY, "save", Material.LIME_DYE, Map.of()));
        setItem(slotReset, loader.buildItemFromSection(GUI_KEY, "reset", Material.ORANGE_DYE, Map.of()));
        setItem(slotExit, loader.buildItemFromSection(GUI_KEY, "exit", Material.BARRIER, Map.of()));

        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 45; i < 54; i++) {
            if (getInventory().getItem(i) == null) {
                setItem(i, glass);
            }
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        boolean isArmorSlot = false;
        for (int armSlot : slotArmor) {
            if (slot == armSlot) {
                isArmorSlot = true;
                break;
            }
        }

        if (isArmorSlot || slot == slotOffhand || (slot >= 9 && slot <= 44)) {
            event.setCancelled(false);
        } else {
            event.setCancelled(true);
        }

        if (slot >= 45 && slot < 54 && slot != slotSave && slot != slotReset && slot != slotExit) {
            event.setCancelled(true);
            return;
        }

        if (slot == slotSave) {
            saveKit();
            return;
        }
        if (slot == slotReset) {
            plugin.getKitManager().resetPlayerKit(player.getUniqueId(), kit.getName());
            MessageUtils.sendMessage(player, "gui.kit-editor.messages.reset");
            player.closeInventory();
            return;
        }
        if (slot == slotExit) {
            restoreInventory();
            player.closeInventory();
            return;
        }
        if (slot == slotInfo) {
            return;
        }

        if (isArmorSlot && event.isRightClick()) {
            event.setCancelled(true);
            if (player.hasPermission("shyamduels.vip")) {
                ItemStack item = getInventory().getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    if (item.getItemMeta() instanceof org.bukkit.inventory.meta.ArmorMeta) {
                        skipCloseTitle = true;
                        new ArmorTrimGui(plugin, player, item, (trimmedItem) -> {
                            skipCloseTitle = false;
                            if (trimmedItem != null) {
                                setItem(slot, trimmedItem);
                            }
                            this.open(player);
                        }).open(player);
                    } else {
                        MessageUtils.sendMessage(player, "gui.kit-editor.messages.hold-armor");
                    }
                } else {
                    MessageUtils.sendMessage(player, "gui.kit-editor.messages.hold-armor");
                }
            } else {
                MessageUtils.sendMessage(player, "gui.kit-editor.messages.no-permission");
            }
        }
    }

    @SuppressWarnings("all")
    public void handleDrag(InventoryDragEvent event) {
        Set<Integer> slots = event.getRawSlots();
        for (int rawSlot : slots) {
            if (rawSlot >= getInventory().getSize()) {
                continue;
            }

            boolean isArmorSlot = false;
            for (int armSlot : slotArmor) {
                if (rawSlot == armSlot) {
                    isArmorSlot = true;
                    break;
                }
            }

            if (isArmorSlot || rawSlot == slotOffhand || (rawSlot >= 9 && rawSlot <= 44)) {
                continue;
            }

            event.setCancelled(true);
            return;
        }
    }

    private void saveKit() {
        ItemStack[] armor = new ItemStack[4];
        armor[0] = getInventory().getItem(slotArmor[0]);
        armor[1] = getInventory().getItem(slotArmor[1]);
        armor[2] = getInventory().getItem(slotArmor[2]);
        armor[3] = getInventory().getItem(slotArmor[3]);

        ItemStack offhand = getInventory().getItem(slotOffhand);
        if (offhand != null && offhand.getType() == Material.SHIELD
                && offhand.hasItemMeta() && offhand.getItemMeta().hasDisplayName()) {
            @SuppressWarnings("deprecation")
            String dn = offhand.getItemMeta().getDisplayName();
            if (dn.contains("Offhand")) {
                offhand = null;
            }
        }

        ItemStack[] inv = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            inv[i] = getInventory().getItem(9 + i);
        }

        PlayerKit newPk = new PlayerKit(player.getUniqueId(), kit.getName());
        newPk.setInventory(inv);
        newPk.setArmor(armor);
        newPk.setOffhand(offhand);

        plugin.getKitManager().savePlayerKit(newPk);
        MessageUtils.sendMessage(player, "gui.kit-editor.messages.saved");
        plugin.getKitManager().savePlayerKit(newPk);
        MessageUtils.sendMessage(player, "gui.kit-editor.messages.saved");
        restoreInventory();
        player.closeInventory();
    }

    private void restoreInventory() {
        player.getInventory().setContents(backupInventory);
        player.getInventory().setArmorContents(backupArmor);
        player.getInventory().setItemInOffHand(backupOffhand);
        player.updateInventory();
    }
}
