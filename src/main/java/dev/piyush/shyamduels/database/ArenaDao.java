package dev.piyush.shyamduels.database;

import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.arena.Arena;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ArenaDao {

    private final DatabaseManager dbManager;

    public ArenaDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        createTable();
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS arenas (" +
                "name VARCHAR(64) PRIMARY KEY, " +
                "world VARCHAR(64), " +
                "corner1_x INT, corner1_y INT, corner1_z INT, " +
                "corner2_x INT, corner2_y INT, corner2_z INT, " +
                "spawn1 VARCHAR(128), " +
                "spawn2 VARCHAR(128), " +
                "center VARCHAR(128), " +
                "allowed_kits TEXT, " +
                "build_enabled BOOLEAN DEFAULT FALSE, " +
                "arena_type VARCHAR(16) DEFAULT 'DUEL'" +
                ");";

        try (Connection conn = dbManager.getArenaConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();

            try {
                conn.createStatement().execute("ALTER TABLE arenas ADD COLUMN center VARCHAR(128)");
            } catch (SQLException ignored) {
            }
            try {
                conn.createStatement().execute("ALTER TABLE arenas ADD COLUMN build_enabled BOOLEAN DEFAULT FALSE");
            } catch (SQLException ignored) {
            }
            try {
                conn.createStatement().execute("ALTER TABLE arenas ADD COLUMN arena_type VARCHAR(16) DEFAULT 'DUEL'");
            } catch (SQLException ignored) {
            }

        } catch (SQLException e) {
            ShyamDuels.getInstance().getLogger().log(Level.SEVERE, "Could not create arenas table", e);
        }
    }

    public void saveArena(Arena arena) {
        if (arena == null || arena.getName() == null) {
            return;
        }

        String sql = "REPLACE INTO arenas (name, world, corner1_x, corner1_y, corner1_z, corner2_x, corner2_y, corner2_z, spawn1, spawn2, center, allowed_kits, build_enabled, arena_type) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getArenaConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, arena.getName());
            stmt.setString(2, arena.getWorldName());
            stmt.setInt(3, arena.getCorner1().getBlockX());
            stmt.setInt(4, arena.getCorner1().getBlockY());
            stmt.setInt(5, arena.getCorner1().getBlockZ());
            stmt.setInt(6, arena.getCorner2().getBlockX());
            stmt.setInt(7, arena.getCorner2().getBlockY());
            stmt.setInt(8, arena.getCorner2().getBlockZ());

            String s1 = arena.getSpawn1() != null ? serializeLoc(arena.getSpawn1()) : "";
            String s2 = arena.getSpawn2() != null ? serializeLoc(arena.getSpawn2()) : "";
            String cen = arena.getCenter() != null ? serializeLoc(arena.getCenter()) : "";

            stmt.setString(9, s1);
            stmt.setString(10, s2);
            stmt.setString(11, cen);
            stmt.setString(12, String.join(",", arena.getAllowedKits()));
            stmt.setBoolean(13, arena.isBuildEnabled());
            stmt.setString(14, arena.getType().name());

            stmt.executeUpdate();
        } catch (SQLException e) {
            ShyamDuels.getInstance().getLogger().log(Level.SEVERE, "Could not save arena: " + arena.getName(), e);
        }
    }

    public void deleteArena(String name) {
        String sql = "DELETE FROM arenas WHERE name = ?";
        try (Connection conn = dbManager.getArenaConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            ShyamDuels.getInstance().getLogger().log(Level.WARNING, "Could not delete arena: " + name, e);
        }
    }

    public List<Arena> loadArenas() {
        List<Arena> arenas = new ArrayList<>();
        String sql = "SELECT * FROM arenas";

        try (Connection conn = dbManager.getArenaConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("name");
                String world = rs.getString("world");
                int c1x = rs.getInt("corner1_x");
                int c1y = rs.getInt("corner1_y");
                int c1z = rs.getInt("corner1_z");
                int c2x = rs.getInt("corner2_x");
                int c2y = rs.getInt("corner2_y");
                int c2z = rs.getInt("corner2_z");
                String spawn1Str = rs.getString("spawn1");
                String spawn2Str = rs.getString("spawn2");
                String centerStr = rs.getString("center");
                String allowedKits = rs.getString("allowed_kits");
                boolean buildEnabled = rs.getBoolean("build_enabled");
                String arenaTypeStr = rs.getString("arena_type");

                org.bukkit.World w = org.bukkit.Bukkit.getWorld(world);
                if (w == null)
                    continue;

                org.bukkit.Location c1 = new org.bukkit.Location(w, c1x, c1y, c1z);
                org.bukkit.Location c2 = new org.bukkit.Location(w, c2x, c2y, c2z);

                Arena arena = new Arena(name, world, c1, c2);
                arena.setBuildEnabled(buildEnabled);

                if (arenaTypeStr != null && !arenaTypeStr.isEmpty()) {
                    try {
                        arena.setType(Arena.ArenaType.valueOf(arenaTypeStr));
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                if (spawn1Str != null && !spawn1Str.isEmpty())
                    arena.setSpawn1(parseLoc(spawn1Str));
                if (spawn2Str != null && !spawn2Str.isEmpty())
                    arena.setSpawn2(parseLoc(spawn2Str));
                if (centerStr != null && !centerStr.isEmpty())
                    arena.setCenter(parseLoc(centerStr));

                if (allowedKits != null && !allowedKits.isEmpty()) {
                    for (String k : allowedKits.split(",")) {
                        if (!k.trim().isEmpty())
                            arena.addKit(k.trim());
                    }
                }

                arenas.add(arena);
            }
        } catch (SQLException e) {
            ShyamDuels.getInstance().getLogger().log(Level.SEVERE, "Could not load arenas", e);
        }
        return arenas;
    }

    private String serializeLoc(org.bukkit.Location loc) {
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw()
                + "," + loc.getPitch();
    }

    private org.bukkit.Location parseLoc(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        String[] parts = s.split(",");
        if (parts.length < 6) {
            return null;
        }
        try {
            org.bukkit.World w = org.bukkit.Bukkit.getWorld(parts[0]);
            if (w == null) {
                return null;
            }
            return new org.bukkit.Location(w, 
                Double.parseDouble(parts[1]), 
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]), 
                Float.parseFloat(parts[4]), 
                Float.parseFloat(parts[5]));
        } catch (NumberFormatException e) {
            ShyamDuels.getInstance().getLogger().log(Level.WARNING, "Failed to parse location: " + s, e);
            return null;
        }
    }
}
