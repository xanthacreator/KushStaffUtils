package me.dankofuk.factionstuff;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dankofuk.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FactionStrike implements Listener, CommandExecutor {
    public String StrikeWebhookUrl;
    public String Strikeusername;
    public String StrikeavatarUrl;
    public boolean isStrikeEnabled;
    public String strikeMessage;
    public final Map<String, Integer> strikes = new HashMap<>();
    public String StrikenoPermissionMessage;
    public String StrikeusageMessage;
    public String StrikeCommand;
    public String StrikeEmbedTitle;
    public String StrikeThumbnail;
    public FileConfiguration config;

    public FactionStrike(String StrikeWebhookUrl, String Strikeusername, String StrikeavatarUrl, boolean isStrikeEnabled, String strikeMessage, String StrikenoPermissionMessage, String StrikeusageMessage, String StrikeCommand, String StrikeEmbedTitle, String StrikeThumbnail, FileConfiguration config) {
        this.StrikeWebhookUrl = StrikeWebhookUrl;
        this.Strikeusername = Strikeusername;
        this.StrikeavatarUrl = StrikeavatarUrl;
        this.isStrikeEnabled = isStrikeEnabled;
        this.strikeMessage = strikeMessage;
        this.StrikenoPermissionMessage = StrikenoPermissionMessage;
        this.StrikeusageMessage = StrikeusageMessage;
        this.StrikeCommand = StrikeCommand;
        this.StrikeEmbedTitle = StrikeEmbedTitle;
        this.StrikeThumbnail = StrikeThumbnail;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("commandlogger.strike.use")) {
            player.sendMessage(ColorUtils.translateColorCodes(StrikenoPermissionMessage));
            return true;
        }

        if (!isStrikeEnabled) {
            player.sendMessage(ColorUtils.translateColorCodes("&cStrikes are currently disabled."));
            return true;
        }

        if (args.length < 3 || !isInteger(args[1])) {
            player.sendMessage(ColorUtils.translateColorCodes(StrikeusageMessage));
            return true;
        }

        String groupName = args[0];
        int strikeAmount = Integer.parseInt(args[1]);
        String strikeReason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        if (strikeAmount <= 0) {
            player.sendMessage(ColorUtils.translateColorCodes(StrikeusageMessage));
            return true;
        }

        if (strikes.containsKey(groupName)) {
            strikes.put(groupName, strikes.get(groupName) + strikeAmount);
        } else {
            strikes.put(groupName, strikeAmount);
        }

        // Execute the strike command with placeholders
        String strikeCommand = StrikeCommand.replace("%group%", groupName)
                .replace("%amount%", Integer.toString(strikeAmount))
                .replace("%reason%", strikeReason);

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), strikeCommand);

        sendWebhook(player, groupName, strikeAmount, strikeReason);

        // Send a separate message to the player
        String strikeResponse = "&aYou have given &c" + groupName + " &a" + strikeAmount + " strikes for: &e" + strikeReason;
        player.sendMessage(ColorUtils.translateColorCodes(strikeResponse));

        return true;
    }

    private void sendWebhook(Player player, String groupName, int strikeAmount, String strikeReason) {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(this.StrikeWebhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "StrikeWebhook");
                connection.setDoOutput(true);

                String description = ColorUtils.translateColorCodes(strikeMessage.replace("%group%", groupName).replace("%amount%", Integer.toString(strikeAmount)).replace("%reason%", strikeReason));
                JsonObject json = new JsonObject();
                json.addProperty("username", this.Strikeusername);
                json.addProperty("avatar_url", this.StrikeavatarUrl);
                JsonObject embed = new JsonObject();
                embed.addProperty("description", description);
                embed.addProperty("color", getColorCode("#FF0000"));
                embed.addProperty("title", this.StrikeEmbedTitle);
                JsonObject thumbnail = new JsonObject();
                thumbnail.addProperty("url", this.StrikeThumbnail);
                embed.add("thumbnail", thumbnail);
                JsonArray embeds = new JsonArray();
                embeds.add(embed);
                json.add("embeds", embeds);

                String message = new Gson().toJson(json);

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(message.getBytes());
                }

                connection.connect();

                int responseCode = connection.getResponseCode();
                String responseMessage = connection.getResponseMessage();
                // Debugger
                // if (responseCode != HttpURLConnection.HTTP_OK) {
                //    Bukkit.getLogger().warning("[StrikeWebhook] Strike sent to the Discord - Response code: " + responseCode + " Response message: " + responseMessage);
                //}
            } catch (MalformedURLException e) {
                Bukkit.getLogger().warning("[StrikeWebhook] Invalid webhook URL specified: " + this.StrikeWebhookUrl);
                e.printStackTrace();
            } catch (ProtocolException e) {
                Bukkit.getLogger().warning("[StrikeWebhook] Invalid protocol specified in webhook URL: " + this.StrikeWebhookUrl);
                e.printStackTrace();
            } catch (IOException e) {
                Bukkit.getLogger().warning("[StrikeWebhook] Error sending message to Discord webhook.");
                e.printStackTrace();
            }
        });
    }



    private int getColorCode(String color) {
        color = color.replace("#", "");
        return Integer.parseInt(color, 16);
    }

    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public void reloadConfigOptions(String StrikeWebhookUrl, String Strikeusername, String StrikeavatarUrl, boolean isStrikeEnabled, String strikeMessage, String StrikenoPermissionMessage, String StrikeusageMessage, String StrikeCommand, String StrikeEmbedTitle, String StrikeThumbnail, FileConfiguration config) {
        this.StrikeWebhookUrl = StrikeWebhookUrl;
        this.Strikeusername = Strikeusername;
        this.StrikeavatarUrl = StrikeavatarUrl;
        this.isStrikeEnabled = isStrikeEnabled;
        this.strikeMessage = strikeMessage;
        this.StrikenoPermissionMessage = StrikenoPermissionMessage;
        this.StrikeusageMessage = StrikeusageMessage;
        this.StrikeCommand = StrikeCommand;
        this.StrikeEmbedTitle = StrikeEmbedTitle;
        this.StrikeThumbnail = StrikeThumbnail;
        this.config = config;
    }

}
