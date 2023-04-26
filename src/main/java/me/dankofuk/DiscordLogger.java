package me.dankofuk;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class DiscordLogger {
    private List<String> messageFormats;

    private String webhookUrl;

    private String serverName;

    private List<String> embedTitleFormats;

    public DiscordLogger(String webhookUrl, List<String> messageFormat, List<String> embedTitleFormat, String serverName) {
        this.webhookUrl = webhookUrl;
        this.messageFormats = messageFormat;
        this.serverName = serverName;
        this.embedTitleFormats = embedTitleFormat;
    }

    public void reloadMessageFormats(List<String> messageFormats) {
        this.messageFormats = messageFormats;
    }

    public void reloadEmbedTitleFormats(List<String> embedTitleFormats) {
        this.embedTitleFormats = embedTitleFormats;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void webhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void logCommand(String command, String playerName) {
        CompletableFuture.runAsync(() -> {
            List<String> messages = new ArrayList<>();
            List<String> embedTitles = new ArrayList<>();
            long time = System.currentTimeMillis() / 1000L;
            for (String messageFormat : this.messageFormats) {
                String message = messageFormat.replace("%player%", playerName).replace("%time%", "<t:" + time + ":R>").replace("%server%", serverName).replace("%command%", command);
                messages.add(message);
            }
            for (String embedTitleFormat : this.embedTitleFormats) {
                String embedTitle = embedTitleFormat.replace("%player%", playerName).replace("%time%", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date())).replace("%server%", serverName).replace("%command%", command);
                embedTitles.add(embedTitle);
            }
            String playerHeadUrl = getPlayerHeadUrl(playerName);
            sendToDiscord(messages, embedTitles, playerHeadUrl);
        });
    }

    public String getPlayerHeadUrl(String playerName) {
        String playerHeadUrl = "";
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "KushStaffLogger");
            connection.setDoOutput(true);
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject)parser.parse(new InputStreamReader(connection.getInputStream()));
            String playerUuid = json.get("id").toString();
            playerHeadUrl = "https://crafatar.com/avatars/" + playerUuid + "?overlay=head";
        } catch (IOException|org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }
        return playerHeadUrl;
    }

    private void sendToDiscord(List<String> messages, List<String> embedTitles, String playerHeadUrl) {
        CompletableFuture.runAsync(() -> {
            if (this.webhookUrl == null || this.webhookUrl.isEmpty()) {
                Bukkit.getLogger().warning("[DiscordLogger] No webhook URL specified.");
                return;
            }
            try {
                URL url = new URL(this.webhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "KushStaffLogger");
                connection.setDoOutput(true);
                StringBuilder jsonString = new StringBuilder();
                for (int i = 0; i < messages.size(); i++)
                    jsonString.append("{\"title\":\"").append(embedTitles.get(i).replace("\n", "\\n")).append("\",\"description\":\"").append(messages.get(i).replace("\n", "\\n")).append("\",\"thumbnail\":{\"url\":\"").append(playerHeadUrl).append("\"}},");
                jsonString = new StringBuilder("{\"embeds\":[" + jsonString.substring(0, jsonString.length() - 1) + "]}");
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonString.toString().getBytes());
                }
                connection.getResponseCode();
                connection.getResponseMessage();
            } catch (MalformedURLException e) {
                Bukkit.getLogger().warning("[DiscordLogger] Invalid webhook URL specified: " + this.webhookUrl);
                e.printStackTrace();
            } catch (ProtocolException e) {
                Bukkit.getLogger().warning("[DiscordLogger] Invalid protocol specified in webhook URL: " + this.webhookUrl);
                e.printStackTrace();
            } catch (IOException e) {
                Bukkit.getLogger().warning("[DiscordLogger] Error sending message to Discord webhook.");
                e.printStackTrace();
            }
        });
    }

}
