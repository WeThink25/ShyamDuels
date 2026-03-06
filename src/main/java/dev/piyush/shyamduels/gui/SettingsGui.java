package dev.piyush.shyamduels.gui;

import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.settings.PlayerSettings;
import dev.piyush.shyamduels.settings.PlayerSettings.SettingType;
import dev.piyush.shyamduels.util.MessageUtils;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SettingsGui extends FastInv {
    
    private final ShyamDuels plugin;
    private final Player player;
    
    public SettingsGui(ShyamDuels plugin, Player player) {
        super(27, MessageUtils.color("&8Player Settings"));
        this.plugin = plugin;
        this.player = player;
        
        setupItems();
    }
    
    @SuppressWarnings("deprecation")
    private void setupItems() {
        PlayerSettings settings = plugin.getSettingsManager().getSettings(player.getUniqueId());
        
        setItem(10, createSettingItem(SettingType.AUTO_GG, settings), e -> {
            toggleSetting(SettingType.AUTO_GG);
        });
        
        setItem(12, createSettingItem(SettingType.DEATH_MESSAGES, settings), e -> {
            toggleSetting(SettingType.DEATH_MESSAGES);
        });
        
        setItem(14, createSettingItem(SettingType.KILLSTREAK_MESSAGES, settings), e -> {
            toggleSetting(SettingType.KILLSTREAK_MESSAGES);
        });
        
        setItem(16, createSettingItem(SettingType.MATCH_START_SOUNDS, settings), e -> {
            toggleSetting(SettingType.MATCH_START_SOUNDS);
        });
    }
    
    @SuppressWarnings("deprecation")
    private ItemStack createSettingItem(SettingType type, PlayerSettings settings) {
        boolean enabled = settings.getValue(type);
        Material material = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        String status = enabled ? "&aEnabled" : "&cDisabled";
        
        String name = "";
        List<String> lore = new ArrayList<>();
        
        switch (type) {
            case AUTO_GG:
                name = "&bAuto GG";
                lore.add("&7Automatically send 'GG' after matches");
                lore.add("");
                lore.add("&7Status: " + status);
                lore.add("&eClick to toggle");
                break;
            case DEATH_MESSAGES:
                name = "&bDeath Messages";
                lore.add("&7Show death messages in chat");
                lore.add("");
                lore.add("&7Status: " + status);
                lore.add("&eClick to toggle");
                break;
            case KILLSTREAK_MESSAGES:
                name = "&bKillstreak Messages";
                lore.add("&7Show killstreak announcements");
                lore.add("");
                lore.add("&7Status: " + status);
                lore.add("&eClick to toggle");
                break;
            case MATCH_START_SOUNDS:
                name = "&bMatch Start Sounds";
                lore.add("&7Play sounds during match countdown");
                lore.add("");
                lore.add("&7Status: " + status);
                lore.add("&eClick to toggle");
                break;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.color(name));
        
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(MessageUtils.color(line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private void toggleSetting(SettingType type) {
        PlayerSettings settings = plugin.getSettingsManager().getSettings(player.getUniqueId());
        settings.toggle(type);
        plugin.getSettingsManager().saveSettings(settings);
        
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        MessageUtils.sendMessage(player, "settings.toggled", 
            Map.of("setting", type.name().toLowerCase().replace("_", " "),
                   "status", settings.getValue(type) ? "enabled" : "disabled"));
        
        setupItems();
    }
}
