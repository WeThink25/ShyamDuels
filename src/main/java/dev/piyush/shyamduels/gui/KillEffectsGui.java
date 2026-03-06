package dev.piyush.shyamduels.gui;

import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.effects.KillEffect;
import dev.piyush.shyamduels.util.MessageUtils;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class KillEffectsGui extends FastInv {
    
    private final ShyamDuels plugin;
    private final Player player;
    private int page = 0;
    private static final int ITEMS_PER_PAGE = 45;
    
    public KillEffectsGui(ShyamDuels plugin, Player player) {
        this(plugin, player, 0);
    }
    
    public KillEffectsGui(ShyamDuels plugin, Player player, int page) {
        super(54, MessageUtils.color("&8Kill Effects - Page " + (page + 1)));
        this.plugin = plugin;
        this.player = player;
        this.page = page;
        
        setupItems();
    }
    
    @SuppressWarnings("deprecation")
    private void setupItems() {
        KillEffect[] allEffects = KillEffect.values();
        KillEffect currentEffect = plugin.getKillEffectManager().getPlayerEffect(player.getUniqueId());
        int playerElo = plugin.getStatsManager().getStats(player.getUniqueId()).getElo();
        
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allEffects.length);
        
        for (int i = startIndex; i < endIndex; i++) {
            KillEffect effect = allEffects[i];
            int slot = i - startIndex;
            
            setItem(slot, createEffectItem(effect, currentEffect, playerElo), e -> {
                if (plugin.getKillEffectManager().canUseEffect(player, effect)) {
                    plugin.getKillEffectManager().setPlayerEffect(player.getUniqueId(), effect);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                    MessageUtils.sendMessage(player, "effects.selected", 
                        java.util.Map.of("effect", effect.getDisplayName()));
                    
                    effect.play(player.getLocation());
                    
                    setupItems();
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    MessageUtils.sendMessage(player, "effects.locked", 
                        java.util.Map.of("effect", effect.getDisplayName(), 
                                       "elo", String.valueOf(effect.getRequiredElo())));
                }
            });
        }
        
        if (page > 0) {
            setItem(48, createNavigationItem(Material.ARROW, "&aPrevious Page"), e -> {
                new KillEffectsGui(plugin, player, page - 1).open(player);
            });
        }
        
        if (endIndex < allEffects.length) {
            setItem(50, createNavigationItem(Material.ARROW, "&aNext Page"), e -> {
                new KillEffectsGui(plugin, player, page + 1).open(player);
            });
        }
        
        setItem(49, createInfoItem(playerElo), e -> {});
    }
    
    @SuppressWarnings("deprecation")
    private ItemStack createEffectItem(KillEffect effect, KillEffect currentEffect, int playerElo) {
        boolean unlocked = effect.canUse(playerElo);
        boolean selected = effect == currentEffect;
        
        Material material = unlocked ? effect.getIcon() : Material.GRAY_DYE;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        String name = unlocked ? "&a" + effect.getDisplayName() : "&c" + effect.getDisplayName() + " &7(Locked)";
        if (selected) {
            name = "&e&l✔ " + effect.getDisplayName() + " &7(Selected)";
        }
        
        meta.setDisplayName(MessageUtils.color(name));
        
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.color("&7" + effect.getDescription()));
        lore.add("");
        
        if (unlocked) {
            if (selected) {
                lore.add(MessageUtils.color("&a&l✔ Currently Selected"));
            } else {
                lore.add(MessageUtils.color("&eClick to select"));
            }
        } else {
            lore.add(MessageUtils.color("&cRequired ELO: &f" + effect.getRequiredElo()));
            lore.add(MessageUtils.color("&7Your ELO: &f" + playerElo));
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    @SuppressWarnings("deprecation")
    private ItemStack createNavigationItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.color(name));
        item.setItemMeta(meta);
        return item;
    }
    
    @SuppressWarnings("deprecation")
    private ItemStack createInfoItem(int playerElo) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.color("&b&lYour Stats"));
        
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.color("&7Your ELO: &f" + playerElo));
        lore.add("");
        
        int unlocked = 0;
        int locked = 0;
        for (KillEffect effect : KillEffect.values()) {
            if (effect.canUse(playerElo)) {
                unlocked++;
            } else {
                locked++;
            }
        }
        
        lore.add(MessageUtils.color("&aUnlocked Effects: &f" + unlocked));
        lore.add(MessageUtils.color("&cLocked Effects: &f" + locked));
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
