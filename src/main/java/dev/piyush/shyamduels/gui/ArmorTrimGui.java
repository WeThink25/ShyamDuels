package dev.piyush.shyamduels.gui;

import dev.piyush.shyamduels.util.ItemBuilder;
import fr.mrmicky.fastinv.FastInv;
import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.util.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public class ArmorTrimGui extends FastInv {

    private ItemStack item;
    private final Consumer<ItemStack> callback;

    private TrimMaterial selectedMaterial = TrimMaterial.REDSTONE;
    private TrimPattern selectedPattern = TrimPattern.COAST;

    private static final int SLOT_PREVIEW = 4;
    private static final int SLOT_APPLY = 8;
    private static final int SLOT_CANCEL = 0;

    private final List<TrimMaterial> materials = Arrays.asList(
            TrimMaterial.QUARTZ, TrimMaterial.IRON, TrimMaterial.NETHERITE, TrimMaterial.REDSTONE,
            TrimMaterial.COPPER, TrimMaterial.GOLD, TrimMaterial.EMERALD, TrimMaterial.DIAMOND,
            TrimMaterial.LAPIS, TrimMaterial.AMETHYST);

    private final List<TrimPattern> patterns = Arrays.asList(
            TrimPattern.SENTRY, TrimPattern.DUNE, TrimPattern.COAST, TrimPattern.WILD,
            TrimPattern.WARD, TrimPattern.EYE, TrimPattern.VEX, TrimPattern.TIDE,
            TrimPattern.SNOUT, TrimPattern.RIB, TrimPattern.SPIRE, TrimPattern.WAYFINDER,
            TrimPattern.SHAPER, TrimPattern.SILENCE, TrimPattern.RAISER, TrimPattern.HOST);

    public ArmorTrimGui(ShyamDuels plugin, Player player, ItemStack itemToEdit, Consumer<ItemStack> callback) {
        super(54, MessageUtils.color(MessageUtils.get("gui.armor-trim.title")));
        this.item = itemToEdit.clone();
        this.callback = callback;

        if (item.getItemMeta() instanceof ArmorMeta am && am.hasTrim()) {
            this.selectedMaterial = am.getTrim().getMaterial();
            this.selectedPattern = am.getTrim().getPattern();
        }

        updateInventory();
    }

    @SuppressWarnings("deprecation")
    private void updateInventory() {
        ItemStack preview = item.clone();
        if (preview.getItemMeta() instanceof ArmorMeta am) {
            am.setTrim(new ArmorTrim(selectedMaterial, selectedPattern));
            preview.setItemMeta(am);
        }
        setItem(SLOT_PREVIEW, preview);

        setItem(SLOT_APPLY, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                .name(MessageUtils.color(MessageUtils.get("gui.armor-trim.items.apply.name")))
                .lore(MessageUtils.getList("gui.armor-trim.items.apply.lore").stream().map(MessageUtils::color)
                        .toArray(String[]::new))
                .build());
        setItem(SLOT_CANCEL,
                new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                        .name(MessageUtils.color(MessageUtils.get("gui.armor-trim.items.cancel.name")))
                        .lore(MessageUtils.getList("gui.armor-trim.items.cancel.lore").stream().map(MessageUtils::color)
                                .toArray(String[]::new))
                        .build());

        List<TrimMaterial> allMaterials = List.of(
                TrimMaterial.QUARTZ, TrimMaterial.IRON, TrimMaterial.NETHERITE, TrimMaterial.REDSTONE,
                TrimMaterial.COPPER, TrimMaterial.GOLD, TrimMaterial.EMERALD, TrimMaterial.DIAMOND,
                TrimMaterial.LAPIS, TrimMaterial.AMETHYST);

        for (int i = 0; i < allMaterials.size(); i++) {
            TrimMaterial mat = allMaterials.get(i);
            int mIndex;
            if (i < 9) {
                mIndex = 9 + i;
            } else {
                mIndex = 5;
            }

            Material iconMat = getMaterialIcon(mat);
            boolean isSelected = mat.equals(selectedMaterial);

            String key = mat.getKey().getKey();
            String nameFormat = isSelected ? MessageUtils.get("gui.armor-trim.status.format.selected")
                    : MessageUtils.get("gui.armor-trim.status.format.unselected");
            String statusLore = isSelected ? MessageUtils.get("gui.armor-trim.status.selected")
                    : MessageUtils.get("gui.armor-trim.status.select-material");

            setItem(mIndex, new ItemBuilder(iconMat)
                    .name(MessageUtils.color(nameFormat.replace("<name>", formatKey(key))))
                    .lore(MessageUtils.color(statusLore))
                    .glow(isSelected)
                    .build());
        }

        List<TrimPattern> patterns = List.of(
                TrimPattern.SENTRY, TrimPattern.DUNE, TrimPattern.COAST, TrimPattern.WILD, TrimPattern.WARD,
                TrimPattern.EYE, TrimPattern.VEX, TrimPattern.TIDE, TrimPattern.SNOUT, TrimPattern.RIB,
                TrimPattern.SPIRE, TrimPattern.WAYFINDER, TrimPattern.SHAPER, TrimPattern.SILENCE, TrimPattern.RAISER,
                TrimPattern.HOST);

        int pIndex = 18;
        for (TrimPattern pat : patterns) {
            if (pIndex > 44)
                break;

            boolean isSelected = pat.equals(selectedPattern);
            Material iconMat = getTemplateMaterial(pat);
            String key = pat.getKey().getKey();

            String nameFormat = isSelected ? MessageUtils.get("gui.armor-trim.status.format.selected")
                    : MessageUtils.get("gui.armor-trim.status.format.unselected");
            String statusLore = isSelected ? MessageUtils.get("gui.armor-trim.status.selected")
                    : MessageUtils.get("gui.armor-trim.status.select-pattern");

            setItem(pIndex, new ItemBuilder(iconMat)
                    .name(MessageUtils.color(nameFormat.replace("<name>", formatKey(key))))
                    .lore(MessageUtils.color(statusLore))
                    .glow(isSelected)
                    .flags(org.bukkit.inventory.ItemFlag.HIDE_POTION_EFFECTS)
                    .build());
            pIndex++;
        }
    }

    private Material getTemplateMaterial(TrimPattern pattern) {
        if (pattern == TrimPattern.SENTRY)
            return Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE;
        if (pattern == TrimPattern.DUNE)
            return Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE;
        if (pattern == TrimPattern.COAST)
            return Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE;
        if (pattern == TrimPattern.WILD)
            return Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE;
        if (pattern == TrimPattern.WARD)
            return Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE;
        if (pattern == TrimPattern.EYE)
            return Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE;
        if (pattern == TrimPattern.VEX)
            return Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE;
        if (pattern == TrimPattern.TIDE)
            return Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE;
        if (pattern == TrimPattern.SNOUT)
            return Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE;
        if (pattern == TrimPattern.RIB)
            return Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE;
        if (pattern == TrimPattern.SPIRE)
            return Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE;
        if (pattern == TrimPattern.WAYFINDER)
            return Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE;
        if (pattern == TrimPattern.SHAPER)
            return Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE;
        if (pattern == TrimPattern.SILENCE)
            return Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE;
        if (pattern == TrimPattern.RAISER)
            return Material.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE;
        if (pattern == TrimPattern.HOST)
            return Material.HOST_ARMOR_TRIM_SMITHING_TEMPLATE;
        return Material.PAPER;
    }

    private Material getMaterialIcon(TrimMaterial tm) {
        if (tm == TrimMaterial.QUARTZ)
            return Material.QUARTZ;
        if (tm == TrimMaterial.IRON)
            return Material.IRON_INGOT;
        if (tm == TrimMaterial.NETHERITE)
            return Material.NETHERITE_INGOT;
        if (tm == TrimMaterial.REDSTONE)
            return Material.REDSTONE;
        if (tm == TrimMaterial.COPPER)
            return Material.COPPER_INGOT;
        if (tm == TrimMaterial.GOLD)
            return Material.GOLD_INGOT;
        if (tm == TrimMaterial.EMERALD)
            return Material.EMERALD;
        if (tm == TrimMaterial.DIAMOND)
            return Material.DIAMOND;
        if (tm == TrimMaterial.LAPIS)
            return Material.LAPIS_LAZULI;
        if (tm == TrimMaterial.AMETHYST)
            return Material.AMETHYST_SHARD;
        return Material.MAGMA_CREAM;
    }

    private String formatKey(String key) {
        return key.toUpperCase().replace("_", " ");
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getSlot();

        if (slot == SLOT_APPLY) {
            if (item.getItemMeta() instanceof ArmorMeta am) {
                am.setTrim(new ArmorTrim(selectedMaterial, selectedPattern));
                item.setItemMeta(am);
            }
            callback.accept(item);
            return;
        }

        if (slot == SLOT_CANCEL) {
            callback.accept(null);

            callback.accept(null);
            return;
        }

        if (slot >= 9 && slot <= 17) {
            int idx = slot - 9;
            if (idx < 9 && idx < materials.size()) {
                selectedMaterial = materials.get(idx);
                updateInventory();
            }
        }

        if (slot == 5 && materials.size() > 9) {
            selectedMaterial = materials.get(9);
            updateInventory();
        }

        if (slot >= 18 && slot <= 44) {
            int idx = slot - 18;
            if (idx < patterns.size()) {
                selectedPattern = patterns.get(idx);
                updateInventory();
            }
        }
    }
}
