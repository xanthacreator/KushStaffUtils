package me.dankofuk.factionstuff;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dankofuk.KushStaffUtils;
import me.dankofuk.utils.ColorUtils;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FactionStrike implements Listener, CommandExecutor {

    private final Map<String, Integer> strikes = new HashMap<>();
    private final FileConfiguration config;
    private KushStaffUtils main;

    public FactionStrike(FileConfiguration config) {
        this.config = config;
    }

    public void accessConfigs() {
        String strikeWebhookUrl = KushStaffUtils.getInstance().getConfig().getString("strike.webhookUrl");
        String strikeUsername = KushStaffUtils.getInstance().getConfig().getString("strike.username");
        String strikeAvatarUrl = KushStaffUtils.getInstance().getConfig().getString("strike.avatarUrl");
        boolean isStrikeEnabled = KushStaffUtils.getInstance().getConfig().getBoolean("strike.enabled");
        String strikeMessage = KushStaffUtils.getInstance().getConfig().getString("strike.message");
        String strikeNoPermissionMessage = KushStaffUtils.getInstance().getConfig().getString("strike.noPermissionMessage");
        String strikeUserMessage = KushStaffUtils.getInstance().getConfig().getString("strike.usageMessage");
        List<String> strikeCommand = KushStaffUtils.getInstance().getConfig().getStringList("strike.sendCommand");
        String strikeEmbedTitle = KushStaffUtils.getInstance().getConfig().getString("strike.embedTitle");
        String strikeThumbnail = KushStaffUtils.getInstance().getConfig().getString("strike.thumbnail");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("commandlogger.strike.use")) {
            player.sendMessage(ColorUtils.translateColorCodes(KushStaffUtils.getInstance().getConfig().getString("strike.noPermissionMessage")));
            return true;
        }

        if (!KushStaffUtils.getInstance().getConfig().getBoolean("strike.enabled")) {
            player.sendMessage(ColorUtils.translateColorCodes("&cStrikes are currently disabled."));
            return true;
        }
        if (args.length < 3 || !isInteger(args[1])) {
            player.sendMessage(ColorUtils.translateColorCodes(KushStaffUtils.getInstance().getConfig().getString("strike.usageMessage")));
            return true;
        }
        String groupName = args[0];
        int strikeAmount = Integer.parseInt(args[1]);
        String strikeReason = String.join(" ", Arrays.<CharSequence>copyOfRange(args, 2, args.length));
        if (strikeAmount <= 0) {
            player.sendMessage(ColorUtils.translateColorCodes(KushStaffUtils.getInstance().getConfig().getString("strike.usageMessage")));
            return true;

        }
        if (this.strikes.containsKey(groupName)) {
            this.strikes.put(groupName, this.strikes.get(groupName) + strikeAmount);
        } else {
            this.strikes.put(groupName, strikeAmount);
        }

        for (String cmd : KushStaffUtils.getInstance().getConfig().getStringList("strike.sendCommand")) {
            cmd = cmd.replace("%group%", groupName)
                    .replace("%amount%", Integer.toString(strikeAmount))
                    .replace("%reason%", strikeReason)
                    .replace("%staff%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        sendWebhook(player, groupName, strikeAmount, strikeReason);

        String strikeResponse = config.getString("strike.staffMessage")
                .replace("%group%", groupName)
                .replace("%amount%", Integer.toString(strikeAmount))
                .replace("%reason%", strikeReason);
        player.sendMessage(ColorUtils.translateColorCodes(strikeResponse));
        return true;
    }

    private void sendWebhook(Player player, String groupName, int strikeAmount, String strikeReason) {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(KushStaffUtils.getInstance().getConfig().getString("strike.webhookUrl"));
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "StrikeWebhook");
                connection.setDoOutput(true);
                String description = ColorUtils.translateColorCodes(KushStaffUtils.getInstance().getConfig().getString("strike.message")
                        .replace("%group%", groupName)
                        .replace("%amount%", Integer.toString(strikeAmount))
                        .replace("%reason%", strikeReason)
                        .replace("%staff%", player.getName()));
                JsonObject json = new JsonObject();
                json.addProperty("username", KushStaffUtils.getInstance().getConfig().getString("strike.username"));
                json.addProperty("avatar_url", KushStaffUtils.getInstance().getConfig().getString("strike.avatarUrl"));
                JsonObject embed = new JsonObject();
                embed.addProperty("description", description);
                embed.addProperty("color", getColorCode("#FF0000"));
                embed.addProperty("title", KushStaffUtils.getInstance().getConfig().getString("strike.embedTitle"));
                JsonObject thumbnail = new JsonObject();
                thumbnail.addProperty("url", KushStaffUtils.getInstance().getConfig().getString("strike.thumbnail"));
                embed.add("thumbnail", thumbnail);
                JsonArray embeds = new JsonArray();
                embeds.add(embed);
                json.add("embeds", embeds);
                String message = (new Gson()).toJson(json);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(message.getBytes());
                }
                connection.connect();
                int responseCode = connection.getResponseCode();
                String str1 = connection.getResponseMessage();
            } catch (MalformedURLException e) {
                Bukkit.getLogger().warning("[StrikeWebhook] Invalid webhook URL specified: " + KushStaffUtils.getInstance().getConfig().getString("strike.webhookUrl"));
                e.printStackTrace();
            } catch (ProtocolException e) {
                Bukkit.getLogger().warning("[StrikeWebhook] Invalid protocol specified in webhook URL: " + KushStaffUtils.getInstance().getConfig().getString("strike.webhookUrl"));
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
}
