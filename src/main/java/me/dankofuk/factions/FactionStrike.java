package me.dankofuk.factions;

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
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FactionStrike implements Listener, CommandExecutor {

    private final Map<String, Integer> strikes = new HashMap<>();
    private final FileConfiguration config;
    private KushStaffUtils instance;

    public FactionStrike(FileConfiguration config, KushStaffUtils instance) {
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
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("commandlogger.strike.use")) {
            sender.sendMessage(ColorUtils.translateColorCodes(Objects.requireNonNull(KushStaffUtils.getInstance().getConfig().getString("strike.noPermissionMessage"))));
            return true;
        }

        if (!KushStaffUtils.getInstance().getConfig().getBoolean("strike.enabled")) {
            sender.sendMessage(ColorUtils.translateColorCodes("&cStrikes are currently disabled."));
            return true;
        }

        if (args.length < 3 || !isInteger(args[1])) {
            sender.sendMessage(ColorUtils.translateColorCodes(Objects.requireNonNull(KushStaffUtils.getInstance().getConfig().getString("strike.usageMessage"))));
            return true;
        }

        String groupName = args[0];
        int strikeAmount = Integer.parseInt(args[1]);
        String strikeReason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        if (strikeAmount <= 0) {
            sender.sendMessage(ColorUtils.translateColorCodes(Objects.requireNonNull(KushStaffUtils.getInstance().getConfig().getString("strike.usageMessage"))));
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
                    .replace("%staff%", sender.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        sendWebhook(sender, groupName, strikeAmount, strikeReason);

        String strikeResponse = Objects.requireNonNull(config.getString("strike.staffMessage"))
                .replace("%group%", groupName)
                .replace("%amount%", Integer.toString(strikeAmount))
                .replace("%reason%", strikeReason);

        sender.sendMessage(ColorUtils.translateColorCodes(strikeResponse));
        return true;
    }

    private void sendWebhook(CommandSender sender, String groupName, int strikeAmount, String strikeReason) {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(Objects.requireNonNull(KushStaffUtils.getInstance().getConfig().getString("strike.webhookUrl")));
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "StrikeWebhook");
                connection.setDoOutput(true);
                String description = ColorUtils.translateColorCodes(Objects.requireNonNull(KushStaffUtils.getInstance().getConfig().getString("strike.message"))
                        .replace("%group%", groupName)
                        .replace("%amount%", Integer.toString(strikeAmount))
                        .replace("%reason%", strikeReason)
                        .replace("%staff%", sender.getName()));
                JsonObject json = new JsonObject();
                json.addProperty("username", KushStaffUtils.getInstance().getConfig().getString("strike.username"));
                json.addProperty("avatar_url", KushStaffUtils.getInstance().getConfig().getString("strike.avatarUrl"));
                JsonObject embed = new JsonObject();
                embed.addProperty("description", description);
                String embedColor = KushStaffUtils.getInstance().getConfig().getString("strike.embedColor");
                int embedColorCode = getColorCode(embedColor);
                embed.addProperty("color", embedColorCode);
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
        try {
            return Integer.parseInt(color, 16);
        } catch (NumberFormatException e) {
            Bukkit.getLogger().warning("[FactionsStrike] Invalid color code specified: " + color);
            return 0;
        }
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
