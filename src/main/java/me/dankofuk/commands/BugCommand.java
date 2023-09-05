package me.dankofuk.commands;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dankofuk.Main;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BugCommand implements Listener, CommandExecutor {
    private Main main;
    public FileConfiguration config;
    public final Map<UUID, Long> cooldowns = new HashMap<>();

    public BugCommand(FileConfiguration config) {
        this.config = config;
    }

    public void accessConfig() {
        String bugWebhookUrl = Main.getInstance().getConfig().getString("bug_report.webhookUrl");
        String bugThumbnail = Main.getInstance().getConfig().getString("bug_report.thumbnailUrl");
        long bugCooldown = Main.getInstance().getConfig().getLong("bug_report.cooldown");;
        String bugUsername = Main.getInstance().getConfig().getString("bug_report.username");;
        String bugAvatarUrl = Main.getInstance().getConfig().getString("bug_report.avatarUrl");;
        boolean isBugEnabled = Main.getInstance().getConfig().getBoolean("bug_report.enabled");;
        String bugMessage = Main.getInstance().getConfig().getString("bug_report.message");;
        String noBugPermissionMessage = Main.getInstance().getConfig().getString("bug_report.noPermissionMessage");
        String bugUsageMessage = Main.getInstance().getConfig().getString("bug_report.usageMessage");;
        String bugResponse = Main.getInstance().getConfig().getString("bug_report.responseMessage");;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("commandlogger.bug.use")) {
            player.sendMessage(ColorUtils.translateColorCodes(Main.getInstance().getConfig().getString("bug_report.noPermissionMessage")));
            return true;
        }

        if (!Main.getInstance().getConfig().getBoolean("bug_report.enabled")) {
            player.sendMessage(ColorUtils.translateColorCodes("&cReporting bugs is currently disabled."));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ColorUtils.translateColorCodes(Main.getInstance().getConfig().getString("bug_report.usageMessage")));
            return true;
        }

        String bugReport = String.join(" ", args);
        long currentTime = System.currentTimeMillis();
        long lastReportTime = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        long timeRemaining = (lastReportTime + (Main.getInstance().getConfig().getLong("bug_report.cooldown") * 1000L) - currentTime) / 1000L;

        if (timeRemaining > 0) {
            String cooldownMessage = "&cPlease wait " + timeRemaining + " seconds before submitting another report.";
            player.sendMessage(ColorUtils.translateColorCodes(cooldownMessage));
            return true;
        }
        sendWebhook(player, bugReport);
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        // Send a separate message to the player
        player.sendMessage(ColorUtils.translateColorCodes(Main.getInstance().getConfig().getString("bug_report.responseMessage").replace("%reason%", bugReport)));

        return true;
    }


    private void sendWebhook(Player player, String bugReport) {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(Main.getInstance().getConfig().getString("bug_report.webhookUrl"));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "BugWebhook");
                connection.setDoOutput(true);

                String description = ColorUtils.translateColorCodes(Main.getInstance().getConfig().getString("bug_report.message").replace("%player%", player.getName()).replace("%bug%", bugReport));
                JsonObject json = new JsonObject();
                json.addProperty("username", Main.getInstance().getConfig().getString("bug_report.username"));
                json.addProperty("avatar_url", Main.getInstance().getConfig().getString("bug_report.avatarUrl"));
                JsonObject embed = new JsonObject();
                embed.addProperty("description", description);
                embed.addProperty("color", getColorCode("#FF0000"));
                embed.addProperty("title", "New Bug Report");
                JsonObject thumbnail = new JsonObject();
                thumbnail.addProperty("url", Main.getInstance().getConfig().getString("bug_report.thumbnailUrl"));
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
                //    Bukkit.getLogger().warning("[BugWebhook] Bug report sent to the Discord - Response code: " + responseCode + " Response message: " + responseMessage);
                //}
            } catch (MalformedURLException e) {
                Bukkit.getLogger().warning("[BugWebhook] Invalid webhook URL specified: " + Main.getInstance().getConfig().getString("bug_report.webhookUrl"));
                e.printStackTrace();
            } catch (ProtocolException e) {
                Bukkit.getLogger().warning("[BugWebhook] Invalid protocol specified in webhook URL: " + Main.getInstance().getConfig().getString("bug_report.webhookUrl"));
                e.printStackTrace();
            } catch (IOException e) {
                Bukkit.getLogger().warning("[BugWebhook] Error sending message to Discord webhook.");
                e.printStackTrace();
            }
        });
    }

    private int getColorCode(String color) {
        color = color.replace("#", "");
        return Integer.parseInt(color, 16);
    }
}
