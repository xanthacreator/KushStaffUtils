package me.dankofuk.loggers.players;

import me.clip.placeholderapi.PlaceholderAPI;
import me.dankofuk.KushStaffUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class JoinLeaveLogger implements Listener {
    private final FileConfiguration config;
    private KushStaffUtils main;

    public JoinLeaveLogger(FileConfiguration config) {
        this.config = config;

    }

    public void accessConfigs() {
        String joinWebhookUrl = KushStaffUtils.getInstance().getConfig().getString("player_leave_join_logger.joinWebhookUrl");
        String leaveWebhookUrl = KushStaffUtils.getInstance().getConfig().getString("player_leave_join_logger.leaveWebhookUrl");
        List<String> joinMessage = KushStaffUtils.getInstance().getConfig().getStringList("player_leave_join_logger.joinMessage");
        List<String> leaveMessage = KushStaffUtils.getInstance().getConfig().getStringList("player_leave_join_logger.leaveMessage");
        boolean useEmbed = KushStaffUtils.getInstance().getConfig().getBoolean("player_leave_join_logger.useEmbed");
        boolean enabled = KushStaffUtils.getInstance().getConfig().getBoolean("player_leave_join_logger.enabled");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!KushStaffUtils.getInstance().getConfig().getBoolean("player_leave_join_logger.enabled") || KushStaffUtils.getInstance().getConfig().getString("player_leave_join_logger.joinWebhookUrl") == null || KushStaffUtils.getInstance().getConfig().getString("player_leave_join_logger.joinWebhookUrl").isEmpty()) {
            Bukkit.getLogger().warning("Error Join Settings");
        } else {
            Player player = event.getPlayer();
            String playerName = player.getName();
            String playerUuid = player.getUniqueId().toString();
            String playerHeadUrl = "https://crafatar.com/avatars/" + playerUuid + "?overlay=head";
            CompletableFuture.runAsync(() -> {
                try {
                    URL url = new URL(KushStaffUtils.getInstance().getConfig().getString("player_leave_join_logger.joinWebhookUrl"));
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("User-Agent", "PlayerJoinLeaveWebhook");
                    connection.setDoOutput(true);
                    String messageKey = "player_leave_join_logger.joinMessage";
                    String message;

                    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                        message = KushStaffUtils.getInstance().getConfig().getStringList(messageKey).stream()
                                .map(line -> PlaceholderAPI.setPlaceholders(player, line))
                                .collect(Collectors.joining("\\n"));
                    } else {
                        message = KushStaffUtils.getInstance().getConfig().getStringList(messageKey).stream()
                                .collect(Collectors.joining("\\n"));
                    }

                    message = message.replace("%player%", playerName);

                    if (KushStaffUtils.getInstance().getConfig().getBoolean("player_leave_join_logger.useEmbed")) {
                        message = "{\"username\":\"" + playerName + "\",\"embeds\":[{\"description\":\"" + message.replace("\n", "\\n") + "\",\"thumbnail\":{\"url\":\"" + playerHeadUrl + "\"}}]}";
                    } else {
                        message = "{\"username\":\"" + playerName + "\",\"content\":\"" + message.replace("\n", "\\n") + "\",\"thumbnail\":{\"url\":\"" + playerHeadUrl + "\"}}";
                    }
                    try (OutputStream os = connection.getOutputStream()) {
                        os.write(message.getBytes());
                    }
                    connection.getResponseCode();
                    connection.getResponseMessage();
                } catch (MalformedURLException e) {
                    Bukkit.getLogger().warning("[PlayerJoinLeaveWebhook] Invalid webhook URL specified: " + KushStaffUtils.getInstance().getConfig().getString("player_leave_join_logger.joinWebhookUrl"));
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    Bukkit.getLogger().warning("[PlayerJoinLeaveWebhook] Invalid protocol specified in webhook URL: " + KushStaffUtils.getInstance().getConfig().getString("player_leave_join_logger.joinWebhookUrl"));
                    e.printStackTrace();
                } catch (IOException e) {
                    Bukkit.getLogger().warning("[PlayerJoinLeaveWebhook] Error sending message to Discord webhook.");
                    e.printStackTrace();
                }
            });
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (!KushStaffUtils.getInstance().getConfig().getBoolean("player_leave_join_logger.enabled") || KushStaffUtils.getInstance().getConfig().getString("player_leave_join_logger.leaveWebhookUrl") == null || KushStaffUtils.getInstance().getConfig().getString("player_leave_join_logger.leaveWebhookUrl").isEmpty()) {
            Bukkit.getLogger().warning("Error Join Settings");
        } else {
            Player player = event.getPlayer();
            String playerName = player.getName();
            String playerUuid = player.getUniqueId().toString();
            String playerHeadUrl = "https://crafatar.com/avatars/" + playerUuid + "?overlay=head";
            CompletableFuture.runAsync(() -> {
                try {
                    URL url = new URL(KushStaffUtils.getInstance().getConfig().getString("player_leave_join_logger.leaveWebhookUrl"));
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("User-Agent", "PlayerJoinLeaveWebhook");
                    connection.setDoOutput(true);
                    String message = KushStaffUtils.getInstance().getConfig().getStringList("player_leave_join_logger.leaveMessage").stream().map(line -> PlaceholderAPI.setPlaceholders(player, line)).collect(Collectors.joining("\\n")).replace("%player%", playerName);

                    if (KushStaffUtils.getInstance().getConfig().getBoolean("player_leave_join_logger.useEmbed")) {
                        message = "{\"username\":\"" + playerName + "\",\"embeds\":[{\"description\":\"" + message.replace("\n", "\\n") + "\",\"thumbnail\":{\"url\":\"" + playerHeadUrl + "\"}}]}";
                    } else {
                        message = "{\"username\":\"" + playerName + "\",\"content\":\"" + message.replace("\n", "\\n") + "\",\"thumbnail\":{\"url\":\"" + playerHeadUrl + "\"}}";
                    }
                    try (OutputStream os = connection.getOutputStream()) {
                        os.write(message.getBytes());
                    }
                    connection.getResponseCode();
                    connection.getResponseMessage();
                } catch (MalformedURLException e) {
                    Bukkit.getLogger().warning("[PlayerJoinLeaveWebhook] Invalid webhook URL specified: " + KushStaffUtils.getInstance().getConfig().getString("player_leave_join_logger.leaveWebhookUrl"));
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    Bukkit.getLogger().warning("[PlayerJoinLeaveWebhook] Invalid protocol specified in webhook URL: " + KushStaffUtils.getInstance().getConfig().getString("player_leave_join_logger.leaveWebhookUrl"));
                    e.printStackTrace();
                } catch (IOException e) {
                    Bukkit.getLogger().warning("[PlayerJoinLeaveWebhook] Error sending message to Discord webhook.");
                    e.printStackTrace();
                }
            });
        }
    }
}