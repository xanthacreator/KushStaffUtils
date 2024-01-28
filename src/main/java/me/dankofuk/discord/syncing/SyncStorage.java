package me.dankofuk.discord.syncing;

import org.bukkit.Bukkit;

import java.sql.*;
import java.util.UUID;

public class SyncStorage {
    private Connection connection;
    private final String url;
    private final String username;
    private final String password;

    public SyncStorage(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        initDatabase();
    }

    public void initDatabase() {
        try {
            this.connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS sync_data (" +
                            "discord_id BIGINT PRIMARY KEY," +
                            "minecraft_uuid VARCHAR(36) NOT NULL" +
                            ");"
            );
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveSyncData(long discordUserId, UUID minecraftUuid) {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, username, password);
            }
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO sync_data (discord_id, minecraft_uuid) VALUES (?, ?) ON DUPLICATE KEY UPDATE minecraft_uuid = ?;"
            );
            statement.setLong(1, discordUserId);
            statement.setString(2, minecraftUuid.toString());
            statement.setString(3, minecraftUuid.toString());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isUserSynced(long discordId) {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, username, password);
            }
            PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM sync_data WHERE discord_id = ?");
            stmt.setLong(1, discordId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                boolean synced = rs.getInt(1) > 0;
                rs.close();
                stmt.close();
                return synced;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public UUID getMinecraftUuid(long discordId) {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, username, password);
            }
            PreparedStatement stmt = connection.prepareStatement("SELECT minecraft_uuid FROM sync_data WHERE discord_id = ?");
            stmt.setLong(1, discordId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("minecraft_uuid"));
                rs.close();
                stmt.close();
                return uuid;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void removeUserSync(long discordId) {
        PreparedStatement stmt = null;
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, username, password);
            }
            stmt = connection.prepareStatement("DELETE FROM sync_data WHERE discord_id = ?");
            stmt.setLong(1, discordId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                Bukkit.getLogger().info("No user found with Discord ID: " + discordId + " to remove.");
            } else {
                Bukkit.getLogger().info("User with Discord ID " + discordId + " removed successfully.");
            }
        } catch (SQLException e) {
            Bukkit.getLogger().info("Error removing user with Discord ID " + discordId);
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                Bukkit.getLogger().info("Database connection closed successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}