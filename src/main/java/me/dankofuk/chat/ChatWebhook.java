package me.dankofuk.chat;

import me.clip.placeholderapi.PlaceholderAPI;
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

public class ChatWebhook implements Listener {

    public String ChatwebhookUrl;
    public String ChatserverName;
    public String Chatusername;
    public String ChatavatarUrl;
    public String ChatmessageFormat;
    public boolean enabled;

    public ChatWebhook(String ChatwebhookUrl, String ChatserverName, String Chatusername, String ChatavatarUrl, String ChatmessageFormat, boolean enabled, FileConfiguration config) {
        this.ChatwebhookUrl = ChatwebhookUrl;
        this.ChatserverName = ChatserverName;
        this.Chatusername = Chatusername;
        this.ChatavatarUrl = ChatavatarUrl;
        this.ChatmessageFormat = ChatmessageFormat;
        this.enabled = enabled;
    }


    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String playerName = event.getPlayer().getName();
        String message = event.getMessage();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Player player = event.getPlayer();
            playerName = PlaceholderAPI.setPlaceholders(player, "%player%");
            message = PlaceholderAPI.setPlaceholders(player, message);
        }

        String webhookMessage = PlaceholderAPI.setPlaceholders(event.getPlayer(), ChatmessageFormat);
        webhookMessage = webhookMessage
                .replaceAll("%player%", playerName)
                .replaceAll("%message%", message);
        if (enabled) {
            sendWebhook(webhookMessage);
        }
    }


    private void sendWebhook(String webhookMessage) {
        try {
            // Remove color codes from webhook message
            webhookMessage = ChatColor.stripColor(webhookMessage);

            // Escape special characters in the webhook message
            webhookMessage = escapeSpecialCharacters(webhookMessage);

            // Create URL object with your Discord webhook URL
            URL url = new URL(ChatwebhookUrl);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true); // add this line
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "ChatLoggerWebhook");
            // Create JSON payload
            JSONObject payload = new JSONObject();
            payload.put("content", webhookMessage);

            // Convert JSON payload to string
            String payloadString = payload.toString();

            // Write payload string to connection output stream
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(payloadString.getBytes());
            outputStream.flush();
            outputStream.close();

            // Get response code and message from connection
            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();

            // Disconnect the connection
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String escapeSpecialCharacters(String input) {
        input = input.replaceAll("\\$", "\\\\\\$");
        input = input.replaceAll("\\\\", "\\\\\\\\");
        input = input.replaceAll("\\.", "\\\\.");
        input = input.replaceAll("\\(", "\\\\(");
        input = input.replaceAll("\\)", "\\\\)");
        input = input.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]");
        input = input.replaceAll("\\?", "\\\\?");
        input = input.replaceAll("\\*", "\\\\*");
        input = input.replaceAll("\\+", "\\\\+");
        input = input.replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}");
        input = input.replaceAll("\\|", "\\\\|");
        input = input.replaceAll("\\^", "\\\\^");
        input = input.replaceAll("\\$", "\\\\\\$");
        return input;
    }
}
