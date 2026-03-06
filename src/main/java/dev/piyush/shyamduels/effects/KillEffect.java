package dev.piyush.shyamduels.effects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public enum KillEffect {
    
    NONE("None", 0, Material.BARRIER, "No effect"),
    
    BLOOD("Blood Explosion", 100, Material.REDSTONE, "Massive blood particle burst"),
    LIGHTNING("Lightning Strike", 200, Material.LIGHTNING_ROD, "Thunder strikes from the sky"),
    EXPLOSION("TNT Blast", 300, Material.TNT, "Explosive particle show"),
    FLAME("Inferno", 400, Material.FIRE_CHARGE, "Blazing fire tornado"),
    HEART("Love Burst", 500, Material.RED_DYE, "Hearts everywhere"),
    
    ENDER("Ender Vortex", 600, Material.ENDER_PEARL, "Portal spiral effect"),
    SNOW("Blizzard", 700, Material.SNOWBALL, "Freezing snowstorm"),
    SLIME("Slime Explosion", 800, Material.SLIME_BALL, "Bouncy slime burst"),
    SMOKE("Smoke Bomb", 900, Material.COAL, "Dense smoke cloud"),
    WATER("Tsunami", 1000, Material.WATER_BUCKET, "Water splash wave"),
    
    LAVA("Volcano", 1100, Material.LAVA_BUCKET, "Erupting lava fountain"),
    ENCHANT("Mystic Runes", 1200, Material.ENCHANTING_TABLE, "Floating enchantment symbols"),
    CRIT("Critical Strike", 1300, Material.DIAMOND_SWORD, "Massive crit particles"),
    MAGIC("Arcane Blast", 1400, Material.BREWING_STAND, "Magical spell explosion"),
    WITCH("Witch Curse", 1500, Material.CAULDRON, "Dark magic swirl"),
    
    DRAGON("Dragon Roar", 1600, Material.DRAGON_BREATH, "Dragon breath tornado"),
    TOTEM("Resurrection", 1700, Material.TOTEM_OF_UNDYING, "Totem revival effect"),
    FIREWORK("Celebration", 1800, Material.FIREWORK_ROCKET, "Colorful firework show"),
    NOTE("Symphony", 1900, Material.NOTE_BLOCK, "Musical note spiral"),
    VILLAGER("Trade Success", 2000, Material.EMERALD, "Happy villager celebration"),
    
    ANGRY("Rage Mode", 2100, Material.REDSTONE_BLOCK, "Angry particle storm"),
    SOUL("Soul Reaper", 2200, Material.SOUL_CAMPFIRE, "Soul fire vortex"),
    SCULK("Warden's Wrath", 2300, Material.SCULK_CATALYST, "Sculk charge explosion"),
    HONEY("Sweet Victory", 2400, Material.HONEY_BOTTLE, "Honey drip shower"),
    SPORE("Nature's Bloom", 2500, Material.SPORE_BLOSSOM, "Floating spore cloud"),
    
    CHERRY("Sakura Storm", 2600, Material.CHERRY_LEAVES, "Cherry blossom tornado"),
    GLOW("Bioluminescence", 2700, Material.GLOW_INK_SAC, "Glowing particle wave"),
    WAX("Candle Ritual", 2800, Material.HONEYCOMB, "Wax drip circle"),
    ELECTRIC("Thunderbolt", 2900, Material.END_ROD, "Electric shock wave"),
    RAINBOW("Prismatic Burst", 3000, Material.NETHER_STAR, "Rainbow spiral explosion");
    
    private final String displayName;
    private final int requiredElo;
    private final Material icon;
    private final String description;
    
    KillEffect(String displayName, int requiredElo, Material icon, String description) {
        this.displayName = displayName;
        this.requiredElo = requiredElo;
        this.icon = icon;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getRequiredElo() {
        return requiredElo;
    }
    
    public Material getIcon() {
        return icon;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean canUse(int playerElo) {
        return playerElo >= requiredElo;
    }

    
    public void play(Location location) {
        EffectPlayer.playEffect(this, location);
    }
    
    public void playForPlayer(Player player, Location location) {
        if (player == null || !player.isOnline()) return;
        play(location);
    }
}
