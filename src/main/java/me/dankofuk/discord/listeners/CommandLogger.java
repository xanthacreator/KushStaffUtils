package me.dankofuk.discord.listeners;

import me.dankofuk.Main;
import me.dankofuk.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandLogger extends ListenerAdapter {
    private List<String> messageFormats;
    private String serverName;
    private List<String> embedTitleFormats;
    public boolean logAsEmbed;
    public String logChannelId;
    public DiscordBot discordBot;
    private static CommandLogger instance;

    public CommandLogger(DiscordBot discordBot, List<String> messageFormat, List<String> embedTitleFormat, String serverName, boolean logAsEmbed, String logChannelId) {
        this.discordBot = discordBot;
        this.messageFormats = messageFormat;
        this.serverName = serverName;
        this.embedTitleFormats = embedTitleFormat;
        this.logAsEmbed = logAsEmbed;
        this.logChannelId = logChannelId;
        instance = this;
    }

    public static CommandLogger getInstance() {
        return instance;
    }

    public void logCommand(String command, String playerName) {
        CompletableFuture.runAsync(() -> {
            List<String> messages = new ArrayList<>();
            List<String> embedTitles = new ArrayList<>();
            long time = System.currentTimeMillis() / 1000L;
            for (String messageFormat : this.messageFormats) {
                String message = messageFormat.replace("%player%", playerName).replace("%time%", "<t:" + time + ":R>").replace("%server%", this.serverName).replace("%command%", command);
                messages.add(message);
            }
            for (String embedTitleFormat : this.embedTitleFormats) {
                String embedTitle = embedTitleFormat.replace("%player%", playerName).replace("%time%", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date())).replace("%server%", this.serverName).replace("%command%", command);
                embedTitles.add(embedTitle);
            }
            String playerHeadUrl = getPlayerHeadUrl(playerName);
            sendToDiscord(messages, embedTitles, playerHeadUrl, this.discordBot, this.logChannelId);
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
            if (playerUuid.length() == 32)
                playerHeadUrl = "https://crafatar.com/avatars/" + playerUuid + "?overlay=head";
        } catch (IOException|org.json.simple.parser.ParseException iOException) {}
        return playerHeadUrl;
    }

    private void sendToDiscord(List<String> messages, List<String> embedTitles, String playerHeadUrl, DiscordBot jda, String logChannelId) {
        CompletableFuture.runAsync(() -> {
            if (logChannelId == null || logChannelId.isEmpty()) {
                Bukkit.getLogger().warning("[DiscordLogger] No log channel specified.");
                return;
            }
            try {
                TextChannel channel = this.discordBot.getJda().getTextChannelById(logChannelId);
                if (channel == null) {
                    Bukkit.getLogger().warning("[DiscordLogger] Invalid log channel ID specified: " + logChannelId);
                    return;
                }
                for (int i = 0; i < messages.size(); i++) {
                    String message = messages.get(i);
                    String embedTitle = embedTitles.get(i);
                    if (!isJavaPlayer(playerHeadUrl)) {
                        if (this.logAsEmbed) {
                            EmbedBuilder embedBuilder = new EmbedBuilder();
                            embedBuilder.setTitle(embedTitle);
                            embedBuilder.setDescription(message);
                            channel.sendMessageEmbeds(embedBuilder.build(), new net.dv8tion.jda.api.entities.MessageEmbed[0]).queue();
                        } else {
                            channel.sendMessage(message).queue();
                        }
                    } else if (this.logAsEmbed) {
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setTitle(embedTitle);
                        embedBuilder.setDescription(message);
                        embedBuilder.setThumbnail(playerHeadUrl);
                        channel.sendMessageEmbeds(embedBuilder.build(), new net.dv8tion.jda.api.entities.MessageEmbed[0]).queue();
                    } else {
                        channel.sendMessage(message).queue();
                    }
                }
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning("[DiscordLogger] Invalid log channel ID specified: " + logChannelId);
                e.printStackTrace();
            } catch (Exception e) {
                Bukkit.getLogger().warning("[DiscordLogger] Error sending message to Discord.");
                e.printStackTrace();
            }
        });
    }

    private boolean isJavaPlayer(String playerHeadUrl) {
        return playerHeadUrl.contains("crafatar.com/avatars/");
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

    public void reloadLogChannelID(String logChannelId) {
        this.logChannelId = logChannelId;
    }

    public void reloadLogAsEmbed(boolean logAsEmbed) {
        this.logAsEmbed = logAsEmbed;
    }

    public void DLoggerInstance() {
        instance = this;
    }
}

