package dev.piyush.shyamduels.database;

import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.kit.PlayerKit;
import dev.piyush.shyamduels.util.SerializerUtils;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerKitDao {

    private final DatabaseManager dbManager;

    public PlayerKitDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        createTable();
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS player_kits (" +
                "player_uuid VARCHAR(36) NOT NULL, " +
                "kit_name VARCHAR(64) NOT NULL, " +
                "inventory TEXT, " +
                "armor TEXT, " +
                "offhand TEXT, " +
                "last_edited BIGINT, " +
                "PRIMARY KEY (player_uuid, kit_name)" +
                ");";

        try (PreparedStatement stmt = dbManager.getKitConnection().prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            ShyamDuels.getInstance().getLogger().log(Level.SEVERE, "Could not create player_kits table", e);
        }
    }

    public void savePlayerKit(PlayerKit playerKit) {
        String sql = "REPLACE INTO player_kits (player_uuid, kit_name, inventory, armor, offhand, last_edited) VALUES(?, ?, ?, ?, ?, ?)";

        String invB64 = SerializerUtils.itemStackArrayToBase64(playerKit.getInventory());
        String armorB64 = SerializerUtils.itemStackArrayToBase64(playerKit.getArmor());
        String offhandB64 = playerKit.getOffhand() != null
                ? SerializerUtils.itemStackArrayToBase64(new ItemStack[] { playerKit.getOffhand() })
                : "";

        try (PreparedStatement stmt = dbManager.getKitConnection().prepareStatement(sql)) {

            stmt.setString(1, playerKit.getPlayerUuid().toString());
            stmt.setString(2, playerKit.getKitName());
            stmt.setString(3, invB64);
            stmt.setString(4, armorB64);
            stmt.setString(5, offhandB64);
            stmt.setLong(6, System.currentTimeMillis());

            stmt.executeUpdate();
        } catch (SQLException e) {
            ShyamDuels.getInstance().getLogger().log(Level.SEVERE,
                    "Could not save player kit: " + playerKit.getKitName(), e);
        }
    }

    public PlayerKit getPlayerKit(UUID playerUuid, String kitName) {
        String sql = "SELECT * FROM player_kits WHERE player_uuid = ? AND kit_name = ?";

        try (PreparedStatement stmt = dbManager.getKitConnection().prepareStatement(sql)) {

            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, kitName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PlayerKit pk = new PlayerKit(playerUuid, kitName);

                    String invB64 = rs.getString("inventory");
                    String armorB64 = rs.getString("armor");
                    String offhandB64 = rs.getString("offhand");
                    long lastEdited = rs.getLong("last_edited");

                    pk.setInventory(SerializerUtils.itemStackArrayFromBase64(invB64));
                    pk.setArmor(SerializerUtils.itemStackArrayFromBase64(armorB64));

                    if (offhandB64 != null && !offhandB64.isEmpty()) {
                        ItemStack[] items = SerializerUtils.itemStackArrayFromBase64(offhandB64);
                        if (items.length > 0)
                            pk.setOffhand(items[0]);
                    }

                    pk.setLastEdited(lastEdited);
                    return pk;
                }
            }
        } catch (SQLException e) {
            ShyamDuels.getInstance().getLogger().log(Level.SEVERE, "Could not load player kit", e);
        }
        return null;
    }

    public void deletePlayerKit(UUID playerUuid, String kitName) {
        String sql = "DELETE FROM player_kits WHERE player_uuid = ? AND kit_name = ?";
        try (PreparedStatement stmt = dbManager.getKitConnection().prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, kitName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            ShyamDuels.getInstance().getLogger().log(Level.SEVERE, "Could not delete player kit", e);
        }
    }

    public void deleteAllPlayerKitsForKit(String kitName) {
        String sql = "DELETE FROM player_kits WHERE kit_name = ?";
        try (PreparedStatement stmt = dbManager.getKitConnection().prepareStatement(sql)) {
            stmt.setString(1, kitName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            ShyamDuels.getInstance().getLogger().log(Level.SEVERE, "Could not delete all player kits for kit: " + kitName, e);
        }
    }
}
