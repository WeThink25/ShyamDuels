package dev.piyush.shyamduels.database;

import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.kit.Kit;
import dev.piyush.shyamduels.util.SerializerUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class KitDao {

    private final DatabaseManager dbManager;

    public KitDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        createTable();
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS kits (" +
                "name VARCHAR(64) PRIMARY KEY, " +
                "inventory TEXT, " +
                "armor TEXT, " +
                "offhand TEXT, " +
                "effects TEXT, " +
                "icon TEXT, " +
                "build_whitelist TEXT" +
                ");";

        try {
            Connection conn = dbManager.getKitConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.execute();

                try {
                    conn.createStatement().execute("ALTER TABLE kits ADD COLUMN build_whitelist TEXT");
                } catch (SQLException ignored) {
                }

                try {
                    conn.createStatement().execute("ALTER TABLE kits ADD COLUMN offhand TEXT");
                } catch (SQLException ignored) {
                }
            }
        } catch (SQLException e) {
            ShyamDuels.getInstance().getLogger().log(Level.SEVERE, "Could not create kits table", e);
        }
    }

    public void saveKit(Kit kit) {
        if (kit == null || kit.getName() == null) {
            return;
        }

        String sql = "REPLACE INTO kits (name, inventory, armor, offhand, effects, icon, build_whitelist) VALUES(?, ?, ?, ?, ?, ?, ?)";

        String invB64 = SerializerUtils.itemStackArrayToBase64(kit.getInventory());
        String armorB64 = SerializerUtils.itemStackArrayToBase64(kit.getArmor());
        String offhandB64 = kit.getOffhand() != null ? SerializerUtils
                .itemStackArrayToBase64(new org.bukkit.inventory.ItemStack[] { kit.getOffhand() }) : "";
        String effectsJson = SerializerUtils.potionEffectsToString(kit.getEffects());
        String iconB64 = kit.getIcon() != null ? SerializerUtils
                .itemStackArrayToBase64(new org.bukkit.inventory.ItemStack[] { kit.getIcon() }) : "";

        String whitelist = kit.getBuildWhitelist().stream()
                .map(Enum::name)
                .collect(java.util.stream.Collectors.joining(","));

        try (Connection conn = dbManager.getKitConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, kit.getName());
            stmt.setString(2, invB64);
            stmt.setString(3, armorB64);
            stmt.setString(4, offhandB64);
            stmt.setString(5, effectsJson);
            stmt.setString(6, iconB64);
            stmt.setString(7, whitelist);

            stmt.executeUpdate();
        } catch (SQLException e) {
            ShyamDuels.getInstance().getLogger().log(Level.SEVERE, "Could not save kit: " + kit.getName(), e);
        }
    }

    public void deleteKit(String name) {
        String sql = "DELETE FROM kits WHERE name = ?";
        try (Connection conn = dbManager.getKitConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            ShyamDuels.getInstance().getLogger().log(Level.WARNING, "Could not delete kit: " + name, e);
        }
    }

    public java.util.List<Kit> loadKits() {
        java.util.List<Kit> kits = new java.util.ArrayList<>();
        String sql = "SELECT * FROM kits";

        try (PreparedStatement stmt = dbManager.getKitConnection().prepareStatement(sql);
                java.sql.ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("name");
                String invB64 = rs.getString("inventory");
                String armorB64 = rs.getString("armor");
                String offhandB64 = rs.getString("offhand");
                String effectsJson = rs.getString("effects");
                String iconB64 = rs.getString("icon");

                Kit kit = new Kit(name);
                kit.setInventory(SerializerUtils.itemStackArrayFromBase64(invB64));
                kit.setArmor(SerializerUtils.itemStackArrayFromBase64(armorB64));
                kit.setEffects(SerializerUtils.potionEffectsFromString(effectsJson));

                if (offhandB64 != null && !offhandB64.isEmpty()) {
                    org.bukkit.inventory.ItemStack[] offhands = SerializerUtils
                            .itemStackArrayFromBase64(offhandB64);
                    if (offhands.length > 0)
                        kit.setOffhand(offhands[0]);
                }

                if (iconB64 != null && !iconB64.isEmpty()) {
                    org.bukkit.inventory.ItemStack[] icons = SerializerUtils
                            .itemStackArrayFromBase64(iconB64);
                    if (icons.length > 0)
                        kit.setIcon(icons[0]);
                }

                String whitelist = rs.getString("build_whitelist");
                if (whitelist != null && !whitelist.isEmpty()) {
                    for (String mat : whitelist.split(",")) {
                        if (!mat.trim().isEmpty()) {
                            try {
                                kit.getBuildWhitelist().add(org.bukkit.Material.valueOf(mat));
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                }

                kits.add(kit);
            }
        } catch (SQLException e) {
            ShyamDuels.getInstance().getLogger().log(Level.SEVERE, "Could not load kits", e);
        }
        return kits;
    }
}
