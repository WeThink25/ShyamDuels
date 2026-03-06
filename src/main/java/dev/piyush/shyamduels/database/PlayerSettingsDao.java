package dev.piyush.shyamduels.database;

import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.settings.PlayerSettings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerSettingsDao {
    
    private final DatabaseManager dbManager;
    
    public PlayerSettingsDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS player_settings (" +
                "player_uuid VARCHAR(36) PRIMARY KEY, " +
                "auto_gg BOOLEAN DEFAULT 1, " +
                "death_messages BOOLEAN DEFAULT 1, " +
                "killstreak_messages BOOLEAN DEFAULT 1, " +
                "match_start_sounds BOOLEAN DEFAULT 1" +
                ");";
        
        try (PreparedStatement stmt = dbManager.getStatsConnection().prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            ShyamDuels.getInstance().getLogger().log(Level.SEVERE, "Could not create player_settings table", e);
        }
    }
    
    public PlayerSettings loadSettings(UUID playerUuid) {
        String sql = "SELECT * FROM player_settings WHERE player_uuid = ?";
        
        try (PreparedStatement stmt = dbManager.getStatsConnection().prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PlayerSettings settings = new PlayerSettings(playerUuid);
                    settings.setAutoGG(rs.getBoolean("auto_gg"));
                    settings.setDeathMessages(rs.getBoolean("death_messages"));
                    settings.setKillstreakMessages(rs.getBoolean("killstreak_messages"));
                    settings.setMatchStartSounds(rs.getBoolean("match_start_sounds"));
                    return settings;
                }
            }
        } catch (SQLException e) {
            ShyamDuels.getInstance().getLogger().log(Level.SEVERE, "Could not load player settings", e);
        }
        
        return null;
    }
    
    public void saveSettings(PlayerSettings settings) {
        String sql = "REPLACE INTO player_settings (player_uuid, auto_gg, death_messages, killstreak_messages, match_start_sounds) " +
                "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = dbManager.getStatsConnection().prepareStatement(sql)) {
            stmt.setString(1, settings.getPlayerUuid().toString());
            stmt.setBoolean(2, settings.isAutoGG());
            stmt.setBoolean(3, settings.isDeathMessages());
            stmt.setBoolean(4, settings.isKillstreakMessages());
            stmt.setBoolean(5, settings.isMatchStartSounds());
            stmt.executeUpdate();
        } catch (SQLException e) {
            ShyamDuels.getInstance().getLogger().log(Level.SEVERE, "Could not save player settings", e);
        }
    }
}
