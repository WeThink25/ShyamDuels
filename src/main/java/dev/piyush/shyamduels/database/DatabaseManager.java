package dev.piyush.shyamduels.database;

import dev.piyush.shyamduels.ShyamDuels;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class DatabaseManager {
    private final ShyamDuels plugin;
    private Connection arenaConnection;
    private Connection kitConnection;
    private Connection statsConnection;
    private String type;

    public DatabaseManager(ShyamDuels plugin) {
        this.plugin = plugin;
        this.type = plugin.getConfig().getString("database.type", "sqlite");
    }

    public void init() {
        if (type.equalsIgnoreCase("mysql")) {
            initMySQL();
        } else {
            initSQLite();
        }
    }

    private void initSQLite() {
        File dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create data directory");
            return;
        }

        try {
            File arenaFile = new File(dataFolder, "arenas.db");
            if (!arenaFile.exists() && !arenaFile.createNewFile()) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create arenas.db");
                return;
            }
            Class.forName("org.sqlite.JDBC");
            arenaConnection = DriverManager.getConnection("jdbc:sqlite:" + arenaFile.getAbsolutePath());

            File kitFile = new File(dataFolder, "kits.db");
            if (!kitFile.exists() && !kitFile.createNewFile()) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create kits.db");
                return;
            }
            kitConnection = DriverManager.getConnection("jdbc:sqlite:" + kitFile.getAbsolutePath());

            File statsFile = new File(dataFolder, "stats.db");
            if (!statsFile.exists() && !statsFile.createNewFile()) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create stats.db");
                return;
            }
            statsConnection = DriverManager.getConnection("jdbc:sqlite:" + statsFile.getAbsolutePath());

            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().info("SQLite databases initialized.");
            }
        } catch (ClassNotFoundException | SQLException | IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not initialize SQLite", e);
            closeAllConnections();
        }
    }

    private void initMySQL() {
        String host = plugin.getConfig().getString("database.mysql.host");
        int port = plugin.getConfig().getInt("database.mysql.port");
        String database = plugin.getConfig().getString("database.mysql.database");
        String username = plugin.getConfig().getString("database.mysql.username");
        String password = plugin.getConfig().getString("database.mysql.password");

        if (host == null || host.isEmpty() || database == null || database.isEmpty()) {
            plugin.getLogger().log(Level.SEVERE, "MySQL configuration is incomplete");
            return;
        }

        try {
            arenaConnection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database,
                    username, password != null ? password : "");
            kitConnection = arenaConnection;
            statsConnection = arenaConnection;
            plugin.getLogger().info("MySQL connected.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not connect to MySQL", e);
        }
    }

    public Connection getArenaConnection() throws SQLException {
        if (arenaConnection == null || arenaConnection.isClosed()) {
            init();
        }
        return arenaConnection;
    }

    public Connection getKitConnection() throws SQLException {
        if (kitConnection == null || kitConnection.isClosed()) {
            if (type.equalsIgnoreCase("mysql") && arenaConnection != null) {
                return arenaConnection;
            }
            init();
        }
        return kitConnection;
    }

    public Connection getStatsConnection() throws SQLException {
        if (statsConnection == null || statsConnection.isClosed()) {
            if (type.equalsIgnoreCase("mysql") && arenaConnection != null) {
                return arenaConnection;
            }
            init();
        }
        return statsConnection;
    }

    public void close() {
        closeAllConnections();
    }

    private void closeAllConnections() {
        try {
            if (arenaConnection != null && !arenaConnection.isClosed()) {
                arenaConnection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error closing arena connection", e);
        }

        try {
            if (kitConnection != null && !kitConnection.isClosed() && kitConnection != arenaConnection) {
                kitConnection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error closing kit connection", e);
        }

        try {
            if (statsConnection != null && !statsConnection.isClosed() && statsConnection != arenaConnection) {
                statsConnection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error closing stats connection", e);
        }
    }
}
