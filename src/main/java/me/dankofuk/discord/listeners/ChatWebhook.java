package me.dankofuk.discord.listeners;

import com.google.gson.JsonObject;
import me.clip.placeholderapi.PlaceholderAPI;
import me.dankofuk.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.json.simple.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class ChatWebhook implements Listener {
    private Main main;
    public FileConfiguration config;

    public ChatWebhook(FileConfiguration config) {
        this.config = config;
    }

    public void accessConfigs() {
        String chatWebhookUrl = Main.getInstance().getConfig().getString("chatwebhook.webhookUrl");
        String chatUsername = Main.getInstance().getConfig().getString("chatwebhook.username");
        String chatAvatarUrl = Main.getInstance().getConfig().getString("chatwebhook.avatarUrl");
        String chatMessageFormat = Main.getInstance().getConfig().getString("chatwebhook.message");
        boolean enabled = Main.getInstance().getConfig().getBoolean("chatwebhook.enabled");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!Main.getInstance().getConfig().getBoolean("chatwebhook.enabled")) {
            return;
        }

        String playerName = event.getPlayer().getName();
        String message = event.getMessage();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Player player = event.getPlayer();
            playerName = PlaceholderAPI.setPlaceholders(player, "%player%");
            message = PlaceholderAPI.setPlaceholders(player, message);
        }

        String webhookMessage = PlaceholderAPI.setPlaceholders(event.getPlayer(), Objects.requireNonNull(Main.getInstance().getConfig().getString("chatwebhook.message")));
        webhookMessage = webhookMessage
                .replace("%player%", playerName)
                .replace("%message%", message);

        sendWebhook(webhookMessage);
    }


    private void sendWebhook(String webhookMessage) {
        try {
            // Remove color codes from webhook message
            webhookMessage = ChatColor.stripColor(webhookMessage);

            // Create URL object with your Discord webhook URL
            URL url = new URL(Objects.requireNonNull(Main.getInstance().getConfig().getString("chatwebhook.webhookUrl")));

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "ChatLoggerWebhook");
            JSONObject payload = new JSONObject();
            payload.put("content", webhookMessage);
            payload.put("username", Main.getInstance().getConfig().getString("chatwebhook.username"));
            payload.put("avatar_url", Main.getInstance().getConfig().getString("chatwebhook.avatarUrl"));

            String payloadString = payload.toString();

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(payloadString.getBytes());
                outputStream.flush();
            }

            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
