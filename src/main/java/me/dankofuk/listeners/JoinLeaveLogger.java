package me.dankofuk.listeners;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeaveLogger implements Listener {
    public String joinWebhookUrl;
    public String leaveWebhookUrl;
    public List<String> joinMessage;
    public List<String> leaveMessage;
    public boolean useEmbed;
    public boolean isEnabled;

    public JoinLeaveLogger(String joinWebhookUrl, String leaveWebhookUrl, List<String> joinMessage, List<String> leaveMessage, boolean useEmbed, boolean isEnabled) {
        this.joinWebhookUrl = joinWebhookUrl;
        this.leaveWebhookUrl = leaveWebhookUrl;
        this.joinMessage = joinMessage;
        this.leaveMessage = leaveMessage;
        this.useEmbed = useEmbed;
        this.isEnabled = isEnabled;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isEnabled || joinWebhookUrl == null || joinWebhookUrl.isEmpty())
            return;
        Player player = event.getPlayer();
        String playerName = player.getName();
        String playerUuid = player.getUniqueId().toString();
        String playerHeadUrl = "https://crafatar.com/avatars/" + playerUuid + "?overlay=head";
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(this.joinWebhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "PlayerJoinLeaveWebhook");
                connection.setDoOutput(true);
                String message = this.joinMessage.stream().map(line -> PlaceholderAPI.setPlaceholders(player, line)).collect(Collectors.joining("\\n")).replace("%player%", playerName);

                if (this.useEmbed) {
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
                Bukkit.getLogger().warning("[PlayerJoinLeaveWebhook] Invalid webhook URL specified: " + this.joinWebhookUrl);
                e.printStackTrace();
            } catch (ProtocolException e) {
                Bukkit.getLogger().warning("[PlayerJoinLeaveWebhook] Invalid protocol specified in webhook URL: " + this.joinWebhookUrl);
                e.printStackTrace();
            } catch (IOException e) {
                Bukkit.getLogger().warning("[PlayerJoinLeaveWebhook] Error sending message to Discord webhook.");
                e.printStackTrace();
            }
        });
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (!this.isEnabled)
            return;
        Player player = event.getPlayer();
        String playerName = player.getName();
        String playerUuid = player.getUniqueId().toString();
        String playerHeadUrl = "https://crafatar.com/avatars/" + playerUuid + "?overlay=head";
        if (this.leaveWebhookUrl != null && !this.leaveWebhookUrl.isEmpty() && isEnabled) { // check if logging is enabled before attempting to send a message
            CompletableFuture.runAsync(() -> {
                try {
                    URL url = new URL(this.leaveWebhookUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("User-Agent", "PlayerJoinLeaveWebhook");
                    connection.setDoOutput(true);
                    String message = this.leaveMessage.stream().map(line -> PlaceholderAPI.setPlaceholders(player, line)).collect(Collectors.joining("\\n")).replace("%player%", playerName);

                    if (this.useEmbed) {
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
                    Bukkit.getLogger().warning("[PlayerJoinLeaveWebhook] Invalid webhook URL specified: " + this.leaveWebhookUrl);
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    Bukkit.getLogger().warning("[PlayerJoinLeaveWebhook] Invalid protocol specified in webhook URL: " + this.leaveWebhookUrl);
                    e.printStackTrace();
                } catch (IOException e) {
                    Bukkit.getLogger().warning("[PlayerJoinLeaveWebhook] Error sending message to Discord webhook.");
                    e.printStackTrace();
                }
            });
        }
    }

    public void reloadJoinLeaveLogger(String joinWebhookUrl, String leaveWebhookUrl, ArrayList<String> joinMessage, ArrayList<String> leaveMessage, boolean useEmbed, boolean isEnabled) {
        this.joinWebhookUrl = joinWebhookUrl;
        this.leaveWebhookUrl = leaveWebhookUrl;
        this.joinMessage = joinMessage;
        this.leaveMessage = leaveMessage;
        this.useEmbed = useEmbed;
        this.isEnabled = isEnabled;
    }
}