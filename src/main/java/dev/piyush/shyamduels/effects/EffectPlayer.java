package dev.piyush.shyamduels.effects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class EffectPlayer {
    
    public static void playEffect(KillEffect effect, Location loc) {
        if (loc == null || loc.getWorld() == null) return;
        
        World world = loc.getWorld();
        Location center = loc.clone().add(0, 1, 0);
        
        switch (effect) {
            case NONE:
                break;
                
            case BLOOD:
                createSphere(world, center, Particle.REDSTONE, 100, 1.0, 
                    new Particle.DustOptions(Color.fromRGB(139, 0, 0), 2.0f));
                world.spawnParticle(Particle.REDSTONE, center, 200, 0.8, 0.8, 0.8,
                    new Particle.DustOptions(Color.RED, 1.5f));
                world.playSound(loc, Sound.BLOCK_HONEY_BLOCK_BREAK, 1.5f, 0.5f);
                world.playSound(loc, Sound.ENTITY_PLAYER_HURT, 1f, 0.8f);
                break;
                
            case LIGHTNING:
                world.strikeLightningEffect(loc);
                createHelix(world, center, Particle.ELECTRIC_SPARK, 80, 2.5, 2);
                world.spawnParticle(Particle.FLASH, center, 3);
                world.spawnParticle(Particle.FIREWORKS_SPARK, center, 100, 0.5, 1.5, 0.5, 0.2);
                world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1.2f);
                world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.5f);
                break;
                
            case EXPLOSION:
                world.spawnParticle(Particle.EXPLOSION_LARGE, center, 5, 0.3, 0.3, 0.3, 0);
                world.spawnParticle(Particle.EXPLOSION_NORMAL, center, 80, 1.0, 1.0, 1.0, 0.1);
                world.spawnParticle(Particle.SMOKE_LARGE, center, 50, 0.8, 0.8, 0.8, 0.1);
                world.spawnParticle(Particle.LAVA, center, 30, 0.8, 0.5, 0.8, 0);
                world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.8f);
                world.playSound(loc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1f, 1f);
                break;
                
            case FLAME:
                createSpiral(world, center, Particle.FLAME, 100, 2.5, 3);
                world.spawnParticle(Particle.LAVA, center, 40, 0.5, 1.0, 0.5, 0);
                world.spawnParticle(Particle.SMOKE_LARGE, center, 30, 0.5, 1.0, 0.5, 0.05);
                world.spawnParticle(Particle.DRIP_LAVA, center.clone().add(0, 1, 0), 50, 0.8, 0.5, 0.8, 0);
                world.playSound(loc, Sound.ITEM_FIRECHARGE_USE, 1.5f, 0.8f);
                world.playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 2f, 1f);
                break;
                
            case HEART:
                createRing(world, center.clone().add(0, 0.5, 0), Particle.HEART, 40, 1.5);
                world.spawnParticle(Particle.HEART, center, 60, 1.0, 1.5, 1.0, 0);
                world.spawnParticle(Particle.VILLAGER_HAPPY, center, 40, 0.8, 0.8, 0.8, 0);
                world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.8f);
                world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 2f);
                break;
                
            case ENDER:
                createVortex(world, center, Particle.PORTAL, 150, 2.5, 4);
                world.spawnParticle(Particle.REVERSE_PORTAL, center, 100, 0.5, 1.5, 0.5, 0.5);
                world.spawnParticle(Particle.DRAGON_BREATH, center, 50, 0.8, 0.8, 0.8, 0.05);
                world.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 0.5f);
                world.playSound(loc, Sound.BLOCK_PORTAL_TRAVEL, 1f, 1.5f);
                break;
                
            case SNOW:
                createSnowStorm(world, center, 100);
                world.spawnParticle(Particle.SNOWFLAKE, center, 150, 1.2, 1.5, 1.2, 0);
                world.spawnParticle(Particle.CLOUD, center, 50, 1.0, 0.5, 1.0, 0.05);
                world.playSound(loc, Sound.BLOCK_SNOW_BREAK, 2f, 0.8f);
                world.playSound(loc, Sound.BLOCK_POWDER_SNOW_BREAK, 1.5f, 1f);
                break;
                
            case SLIME:
                createBounce(world, center, Particle.SLIME, 80);
                world.spawnParticle(Particle.SLIME, center, 60, 0.8, 0.8, 0.8, 0.1);
                world.spawnParticle(Particle.VILLAGER_HAPPY, center, 30, 0.5, 0.5, 0.5, 0);
                world.playSound(loc, Sound.ENTITY_SLIME_SQUISH, 1.5f, 0.8f);
                world.playSound(loc, Sound.ENTITY_SLIME_JUMP, 1f, 1.2f);
                break;
                
            case SMOKE:
                createExplosion(world, center, Particle.SMOKE_LARGE, 100, 1.5);
                world.spawnParticle(Particle.SMOKE_NORMAL, center, 150, 1.0, 1.0, 1.0, 0.1);
                world.spawnParticle(Particle.CLOUD, center, 50, 0.8, 0.8, 0.8, 0.05);
                world.playSound(loc, Sound.ENTITY_GHAST_SHOOT, 1f, 0.5f);
                break;
                
            case WATER:
                createWave(world, center, Particle.WATER_SPLASH, 120);
                world.spawnParticle(Particle.WATER_BUBBLE, center, 100, 1.0, 1.0, 1.0, 0.2);
                world.spawnParticle(Particle.WATER_DROP, center.clone().add(0, 1.5, 0), 80, 1.0, 0.5, 1.0, 0);
                world.spawnParticle(Particle.DRIP_WATER, center.clone().add(0, 2, 0), 60, 1.0, 0, 1.0, 0);
                world.playSound(loc, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 2f, 1f);
                world.playSound(loc, Sound.AMBIENT_UNDERWATER_ENTER, 1.5f, 1.2f);
                break;
                
            case LAVA:
                createFountain(world, center, Particle.LAVA, 80);
                world.spawnParticle(Particle.DRIP_LAVA, center.clone().add(0, 2, 0), 100, 1.0, 0.5, 1.0, 0);
                world.spawnParticle(Particle.FLAME, center, 80, 0.8, 1.0, 0.8, 0.1);
                world.spawnParticle(Particle.SMOKE_LARGE, center, 40, 0.8, 1.0, 0.8, 0.05);
                world.playSound(loc, Sound.BLOCK_LAVA_POP, 2f, 0.8f);
                world.playSound(loc, Sound.ITEM_BUCKET_EMPTY_LAVA, 1f, 1f);
                break;
                
            case ENCHANT:
                createEnchantmentCircle(world, center, 100);
                world.spawnParticle(Particle.ENCHANTMENT_TABLE, center, 150, 1.0, 2.0, 1.0, 2);
                world.spawnParticle(Particle.CRIT_MAGIC, center, 60, 0.8, 0.8, 0.8, 0.2);
                world.playSound(loc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.5f, 1.2f);
                world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                break;
                
            case CRIT:
                createStarburst(world, center, Particle.CRIT, 100);
                world.spawnParticle(Particle.CRIT_MAGIC, center, 80, 1.0, 1.0, 1.0, 0.3);
                world.spawnParticle(Particle.SWEEP_ATTACK, center, 5, 0.5, 0.5, 0.5, 0);
                world.spawnParticle(Particle.DAMAGE_INDICATOR, center, 30, 0.8, 0.8, 0.8, 0);
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.5f, 1f);
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1f, 1.2f);
                break;
                
            case MAGIC:
                createMagicCircle(world, center, 120);
                world.spawnParticle(Particle.SPELL_WITCH, center, 100, 1.0, 1.5, 1.0, 0.2);
                world.spawnParticle(Particle.SPELL_MOB, center, 80, 0.8, 0.8, 0.8, 0);
                world.spawnParticle(Particle.ENCHANTMENT_TABLE, center, 60, 0.5, 1.0, 0.5, 1);
                world.playSound(loc, Sound.ENTITY_EVOKER_CAST_SPELL, 1.5f, 1f);
                world.playSound(loc, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1f, 1.2f);
                break;
                
            case WITCH:
                createSpiralUp(world, center, Particle.SPELL_WITCH, 100, 3.0);
                world.spawnParticle(Particle.SPELL_MOB, center, 80, 0.8, 1.5, 0.8, 0);
                world.spawnParticle(Particle.SMOKE_LARGE, center, 40, 0.5, 1.0, 0.5, 0.05);
                world.playSound(loc, Sound.ENTITY_WITCH_CELEBRATE, 1.5f, 0.8f);
                world.playSound(loc, Sound.ENTITY_WITCH_AMBIENT, 1f, 1f);
                break;
                
            case DRAGON:
                createDragonBreath(world, center, 120);
                world.spawnParticle(Particle.DRAGON_BREATH, center, 150, 1.0, 1.5, 1.0, 0.1);
                world.spawnParticle(Particle.SPELL_MOB, center, 60, 0.8, 0.8, 0.8, 0);
                world.spawnParticle(Particle.SMOKE_LARGE, center, 50, 0.8, 1.0, 0.8, 0.05);
                world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1.2f);
                world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.5f, 1f);
                break;
                
            case TOTEM:
                createTotemEffect(world, center, 100);
                world.spawnParticle(Particle.TOTEM, center, 150, 1.0, 2.0, 1.0, 0.3);
                world.spawnParticle(Particle.FIREWORKS_SPARK, center, 80, 0.8, 1.5, 0.8, 0.1);
                world.playSound(loc, Sound.ITEM_TOTEM_USE, 1.5f, 1f);
                world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
                break;
                
            case FIREWORK:
                createFireworkShow(world, center, 150);
                world.spawnParticle(Particle.FIREWORKS_SPARK, center, 200, 1.2, 1.5, 1.2, 0.2);
                world.spawnParticle(Particle.FLAME, center, 60, 1.0, 1.0, 1.0, 0.1);
                world.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.5f, 1f);
                world.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1f, 1.2f);
                world.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1f, 1.5f);
                break;
                
            case NOTE:
                createNoteSpiral(world, center, 80);
                for (int i = 0; i < 20; i++) {
                    world.spawnParticle(Particle.NOTE, center.clone().add(
                        Math.random() - 0.5, Math.random() * 2, Math.random() - 0.5), 1);
                }
                world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1.5f);
                world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.8f);
                break;
                
            case VILLAGER:
                createCelebration(world, center, Particle.VILLAGER_HAPPY, 100);
                world.spawnParticle(Particle.HEART, center, 40, 0.8, 1.0, 0.8, 0);
                world.spawnParticle(Particle.VILLAGER_HAPPY, center, 80, 1.0, 1.5, 1.0, 0.2);
                world.playSound(loc, Sound.ENTITY_VILLAGER_YES, 1.5f, 1f);
                world.playSound(loc, Sound.ENTITY_VILLAGER_CELEBRATE, 1f, 1.2f);
                break;
                
            case ANGRY:
                createAngerStorm(world, center, 100);
                world.spawnParticle(Particle.VILLAGER_ANGRY, center, 80, 1.0, 1.5, 1.0, 0);
                world.spawnParticle(Particle.REDSTONE, center, 100, 0.8, 0.8, 0.8,
                    new Particle.DustOptions(Color.fromRGB(200, 0, 0), 1.5f));
                world.spawnParticle(Particle.SMOKE_LARGE, center, 50, 0.8, 1.0, 0.8, 0.05);
                world.playSound(loc, Sound.ENTITY_VILLAGER_NO, 1.5f, 0.5f);
                world.playSound(loc, Sound.ENTITY_RAVAGER_ROAR, 1f, 1f);
                break;
                
            case SOUL:
                createSoulVortex(world, center, 120);
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, center, 100, 0.8, 1.5, 0.8, 0.1);
                world.spawnParticle(Particle.SOUL, center, 80, 1.0, 2.0, 1.0, 0.1);
                world.spawnParticle(Particle.SMOKE_LARGE, center, 40, 0.5, 1.0, 0.5, 0.05);
                world.playSound(loc, Sound.PARTICLE_SOUL_ESCAPE, 1.5f, 0.8f);
                world.playSound(loc, Sound.BLOCK_SOUL_SAND_BREAK, 1f, 0.5f);
                break;
                
            case SCULK:
                createSculkBloom(world, center, 100);
                world.spawnParticle(Particle.SCULK_CHARGE, center, 3, 0.5, 0.5, 0.5, 0);
                world.spawnParticle(Particle.SCULK_SOUL, center, 80, 1.0, 1.5, 1.0, 0.2);
                world.spawnParticle(Particle.SONIC_BOOM, center, 1);
                world.playSound(loc, Sound.BLOCK_SCULK_CATALYST_BLOOM, 1.5f, 0.8f);
                world.playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.8f, 1.2f);
                world.playSound(loc, Sound.BLOCK_SCULK_SPREAD, 1f, 1f);
                break;
                
            case HONEY:
                createHoneyShower(world, center, 100);
                world.spawnParticle(Particle.DRIPPING_HONEY, center.clone().add(0, 2, 0), 100, 1.0, 0.5, 1.0, 0);
                world.spawnParticle(Particle.FALLING_HONEY, center, 80, 0.8, 1.0, 0.8, 0);
                world.spawnParticle(Particle.VILLAGER_HAPPY, center, 40, 0.8, 0.8, 0.8, 0);
                world.playSound(loc, Sound.BLOCK_HONEY_BLOCK_SLIDE, 1.5f, 1f);
                world.playSound(loc, Sound.ITEM_HONEY_BOTTLE_DRINK, 1f, 1.2f);
                break;
                
            case SPORE:
                createSporeCloud(world, center, 120);
                world.spawnParticle(Particle.SPORE_BLOSSOM_AIR, center, 150, 1.2, 2.0, 1.2, 0);
                world.spawnParticle(Particle.FALLING_SPORE_BLOSSOM, center.clone().add(0, 2, 0), 80, 1.0, 0.5, 1.0, 0);
                world.spawnParticle(Particle.VILLAGER_HAPPY, center, 40, 0.8, 0.8, 0.8, 0);
                world.playSound(loc, Sound.BLOCK_SPORE_BLOSSOM_PLACE, 1.5f, 1f);
                world.playSound(loc, Sound.BLOCK_GRASS_BREAK, 1f, 1.5f);
                break;
                
            case CHERRY:
                createCherryTornado(world, center, 150);
                world.spawnParticle(Particle.CHERRY_LEAVES, center, 200, 1.2, 2.5, 1.2, 0.1);
                world.spawnParticle(Particle.FALLING_DUST, center.clone().add(0, 2, 0), 100, 1.0, 0.5, 1.0, 0);
                world.spawnParticle(Particle.HEART, center, 30, 0.8, 1.0, 0.8, 0);
                world.playSound(loc, Sound.BLOCK_CHERRY_LEAVES_BREAK, 1.5f, 1f);
                world.playSound(loc, Sound.BLOCK_PINK_PETALS_PLACE, 1f, 1.2f);
                break;
                
            case GLOW:
                createGlowWave(world, center, 120);
                world.spawnParticle(Particle.GLOW, center, 150, 1.0, 1.5, 1.0, 0.2);
                world.spawnParticle(Particle.GLOW_SQUID_INK, center, 80, 0.8, 0.8, 0.8, 0.1);
                world.spawnParticle(Particle.END_ROD, center, 60, 0.8, 1.0, 0.8, 0.05);
                world.playSound(loc, Sound.ENTITY_GLOW_SQUID_SQUIRT, 1.5f, 1f);
                world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.5f);
                break;
                
            case WAX:
                createWaxCircle(world, center, 100);
                world.spawnParticle(Particle.DRIPPING_DRIPSTONE_WATER, center.clone().add(0, 2, 0), 80, 1.0, 0.5, 1.0, 0);
                world.spawnParticle(Particle.WAX_ON, center, 60, 0.8, 0.8, 0.8, 0);
                world.spawnParticle(Particle.WAX_OFF, center, 40, 0.8, 0.8, 0.8, 0);
                world.playSound(loc, Sound.ITEM_HONEYCOMB_WAX_ON, 1.5f, 1f);
                world.playSound(loc, Sound.BLOCK_CANDLE_EXTINGUISH, 1f, 0.8f);
                break;
                
            case ELECTRIC:
                createElectricShock(world, center, 120);
                world.spawnParticle(Particle.ELECTRIC_SPARK, center, 150, 1.0, 1.5, 1.0, 0.3);
                world.spawnParticle(Particle.END_ROD, center, 80, 0.8, 1.5, 0.8, 0.1);
                world.spawnParticle(Particle.FIREWORKS_SPARK, center, 100, 1.0, 1.0, 1.0, 0.2);
                world.spawnParticle(Particle.FLASH, center, 2);
                world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1f, 1.5f);
                world.playSound(loc, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.5f, 1.2f);
                break;
                
            case RAINBOW:
                createRainbowSpiral(world, center, 200);
                Color[] colors = {
                    Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN,
                    Color.AQUA, Color.BLUE, Color.fromRGB(148, 0, 211)
                };
                for (Color color : colors) {
                    world.spawnParticle(Particle.REDSTONE, center, 30, 1.0, 1.5, 1.0,
                        new Particle.DustOptions(color, 2.0f));
                }
                world.spawnParticle(Particle.FIREWORKS_SPARK, center, 100, 1.0, 1.5, 1.0, 0.2);
                world.spawnParticle(Particle.END_ROD, center, 60, 0.8, 1.0, 0.8, 0.1);
                world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.5f, 2f);
                world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 2f);
                world.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1f, 1.5f);
                break;
        }
    }

    
    private static void createSphere(World world, Location center, Particle particle, int count, double radius, Object data) {
        for (int i = 0; i < count; i++) {
            double phi = Math.random() * Math.PI * 2;
            double theta = Math.random() * Math.PI;
            double x = radius * Math.sin(theta) * Math.cos(phi);
            double y = radius * Math.sin(theta) * Math.sin(phi);
            double z = radius * Math.cos(theta);
            if (data != null) {
                world.spawnParticle(particle, center.clone().add(x, y, z), 1, 0, 0, 0, 0, data);
            } else {
                world.spawnParticle(particle, center.clone().add(x, y, z), 1, 0, 0, 0, 0);
            }
        }
    }
    
    private static void createHelix(World world, Location center, Particle particle, int count, double height, int rotations) {
        for (int i = 0; i < count; i++) {
            double progress = (double) i / count;
            double angle = progress * Math.PI * 2 * rotations;
            double radius = 0.8;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            double y = progress * height;
            world.spawnParticle(particle, center.clone().add(x, y, z), 1, 0, 0, 0, 0);
        }
    }
    
    private static void createSpiral(World world, Location center, Particle particle, int count, double height, int rotations) {
        for (int i = 0; i < count; i++) {
            double progress = (double) i / count;
            double angle = progress * Math.PI * 2 * rotations;
            double radius = 1.2 * (1 - progress);
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            double y = progress * height;
            world.spawnParticle(particle, center.clone().add(x, y, z), 1, 0, 0, 0, 0);
        }
    }
    
    private static void createRing(World world, Location center, Particle particle, int count, double radius) {
        for (int i = 0; i < count; i++) {
            double angle = (double) i / count * Math.PI * 2;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            world.spawnParticle(particle, center.clone().add(x, 0, z), 1, 0, 0, 0, 0);
        }
    }
    
    private static void createVortex(World world, Location center, Particle particle, int count, double height, int rotations) {
        for (int i = 0; i < count; i++) {
            double progress = (double) i / count;
            double angle = progress * Math.PI * 2 * rotations;
            double radius = 1.5 * Math.sin(progress * Math.PI);
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            double y = progress * height - height / 2;
            world.spawnParticle(particle, center.clone().add(x, y, z), 1, 0, 0, 0, 0);
        }
    }
    
    private static void createSnowStorm(World world, Location center, int count) {
        for (int i = 0; i < count; i++) {
            double x = (Math.random() - 0.5) * 2.5;
            double y = Math.random() * 3;
            double z = (Math.random() - 0.5) * 2.5;
            world.spawnParticle(Particle.SNOWFLAKE, center.clone().add(x, y, z), 1, 0, -0.1, 0, 0);
        }
    }
    
    private static void createBounce(World world, Location center, Particle particle, int count) {
        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 1.5;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            double y = Math.random() * 2;
            world.spawnParticle(particle, center.clone().add(x, y, z), 1, 0, 0, 0, 0);
        }
    }
    
    private static void createExplosion(World world, Location center, Particle particle, int count, double radius) {
        for (int i = 0; i < count; i++) {
            Vector direction = new Vector(
                Math.random() - 0.5,
                Math.random() - 0.5,
                Math.random() - 0.5
            ).normalize().multiply(radius);
            world.spawnParticle(particle, center.clone().add(direction), 1, 0, 0, 0, 0);
        }
    }
    
    private static void createWave(World world, Location center, Particle particle, int count) {
        for (int i = 0; i < count; i++) {
            double angle = (double) i / count * Math.PI * 2;
            double radius = 1.5;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            double y = Math.sin(angle * 3) * 0.5;
            world.spawnParticle(particle, center.clone().add(x, y, z), 1, 0, 0, 0, 0);
        }
    }
    
    private static void createFountain(World world, Location center, Particle particle, int count) {
        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 0.5;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            double y = Math.random() * 2.5;
            world.spawnParticle(particle, center.clone().add(x, y, z), 1, 0, 0, 0, 0);
        }
    }
    
    private static void createEnchantmentCircle(World world, Location center, int count) {
        for (int i = 0; i < 3; i++) {
            double radius = 0.8 + i * 0.4;
            createRing(world, center.clone().add(0, i * 0.3, 0), Particle.ENCHANTMENT_TABLE, count / 3, radius);
        }
    }
    
    private static void createStarburst(World world, Location center, Particle particle, int count) {
        int rays = 8;
        for (int i = 0; i < rays; i++) {
            double angle = (double) i / rays * Math.PI * 2;
            for (int j = 0; j < count / rays; j++) {
                double distance = (double) j / (count / rays) * 2;
                double x = distance * Math.cos(angle);
                double z = distance * Math.sin(angle);
                world.spawnParticle(particle, center.clone().add(x, 0, z), 1, 0, 0, 0, 0);
            }
        }
    }
    
    private static void createMagicCircle(World world, Location center, int count) {
        createRing(world, center, Particle.SPELL_WITCH, count / 2, 1.5);
        createRing(world, center.clone().add(0, 0.5, 0), Particle.SPELL_WITCH, count / 2, 1.0);
    }
    
    private static void createSpiralUp(World world, Location center, Particle particle, int count, double height) {
        for (int i = 0; i < count; i++) {
            double progress = (double) i / count;
            double angle = progress * Math.PI * 2 * 3;
            double radius = 1.0;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            double y = progress * height;
            world.spawnParticle(particle, center.clone().add(x, y, z), 1, 0, 0, 0, 0);
        }
    }
    
    private static void createDragonBreath(World world, Location center, int count) {
        createVortex(world, center, Particle.DRAGON_BREATH, count, 2.5, 3);
    }
    
    private static void createTotemEffect(World world, Location center, int count) {
        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 1.2;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            double y = Math.random() * 3;
            world.spawnParticle(Particle.TOTEM, center.clone().add(x, y, z), 1, 0, 0, 0, 0);
        }
    }
    
    private static void createFireworkShow(World world, Location center, int count) {
        for (int i = 0; i < count; i++) {
            Vector direction = new Vector(
                Math.random() - 0.5,
                Math.random() * 0.5 + 0.5,
                Math.random() - 0.5
            ).normalize().multiply(1.5);
            world.spawnParticle(Particle.FIREWORKS_SPARK, center.clone().add(direction), 1, 0, 0, 0, 0);
        }
    }
    
    private static void createNoteSpiral(World world, Location center, int count) {
        createSpiral(world, center, Particle.NOTE, count, 2.5, 3);
    }
    
    private static void createCelebration(World world, Location center, Particle particle, int count) {
        for (int i = 0; i < count; i++) {
            double x = (Math.random() - 0.5) * 2;
            double y = Math.random() * 2.5;
            double z = (Math.random() - 0.5) * 2;
            world.spawnParticle(particle, center.clone().add(x, y, z), 1, 0, 0, 0, 0);
        }
    }
    
    private static void createAngerStorm(World world, Location center, int count) {
        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 1.5;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            double y = Math.random() * 2;
            world.spawnParticle(Particle.VILLAGER_ANGRY, center.clone().add(x, y, z), 1, 0, 0, 0, 0);
        }
    }
    
    private static void createSoulVortex(World world, Location center, int count) {
        createVortex(world, center, Particle.SOUL, count, 3.0, 4);
    }
    
    private static void createSculkBloom(World world, Location center, int count) {
        for (int i = 0; i < 5; i++) {
            double radius = 0.5 + i * 0.3;
            createRing(world, center.clone().add(0, i * 0.2, 0), Particle.SCULK_SOUL, count / 5, radius);
        }
    }
    
    private static void createHoneyShower(World world, Location center, int count) {
        for (int i = 0; i < count; i++) {
            double x = (Math.random() - 0.5) * 2;
            double z = (Math.random() - 0.5) * 2;
            world.spawnParticle(Particle.DRIPPING_HONEY, center.clone().add(x, 2.5, z), 1, 0, 0, 0, 0);
        }
    }
    
    private static void createSporeCloud(World world, Location center, int count) {
        for (int i = 0; i < count; i++) {
            double x = (Math.random() - 0.5) * 2.5;
            double y = Math.random() * 3;
            double z = (Math.random() - 0.5) * 2.5;
            world.spawnParticle(Particle.SPORE_BLOSSOM_AIR, center.clone().add(x, y, z), 1, 0, 0, 0, 0);
        }
    }
    
    private static void createCherryTornado(World world, Location center, int count) {
        createSpiral(world, center, Particle.CHERRY_LEAVES, count, 3.5, 4);
    }
    
    private static void createGlowWave(World world, Location center, int count) {
        for (int i = 0; i < 3; i++) {
            double radius = 0.8 + i * 0.5;
            createRing(world, center.clone().add(0, i * 0.4, 0), Particle.GLOW, count / 3, radius);
        }
    }
    
    private static void createWaxCircle(World world, Location center, int count) {
        createRing(world, center, Particle.WAX_ON, count, 1.5);
    }
    
    private static void createElectricShock(World world, Location center, int count) {
        for (int i = 0; i < count; i++) {
            Vector direction = new Vector(
                Math.random() - 0.5,
                Math.random() - 0.5,
                Math.random() - 0.5
            ).normalize().multiply(1.5);
            world.spawnParticle(Particle.ELECTRIC_SPARK, center.clone().add(direction), 1, 0, 0, 0, 0);
        }
    }
    
    private static void createRainbowSpiral(World world, Location center, int count) {
        createSpiral(world, center, Particle.END_ROD, count, 3.0, 5);
    }
}
