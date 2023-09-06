package me.dankofuk.commands;

import me.dankofuk.Main;
import me.dankofuk.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
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

public class ReportCommand implements Listener, CommandExecutor {
    private Main main;
    public final Map<UUID, Long> cooldowns = new HashMap<>();
    public FileConfiguration config;

    public ReportCommand(FileConfiguration config) {
        this.config = config;
    }

    public void accessConfigs() {
        String ReportWebhookUrl = Main.getInstance().getConfig().getString("report.webhookUrl");
        String username = Main.getInstance().getConfig().getString("report.username");
        String avatarUrl = Main.getInstance().getConfig().getString("report.avatarUrl");
        boolean isReportEnabled = Main.getInstance().getConfig().getBoolean("report.enabled");
        String reportMessage = Main.getInstance().getConfig().getString("report.message");
        int reportCooldown = Main.getInstance().getConfig().getInt("report.cooldown");
        String reportSentMessage = Main.getInstance().getConfig().getString("report.sentMessage");
        String noPermissionMessage = Main.getInstance().getConfig().getString("report.noPermissionMessage");
        String usageMessage = Main.getInstance().getConfig().getString("report.usageMessage");
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player)sender;
        if (!player.hasPermission("commandlogger.report.use")) {
            player.sendMessage(ColorUtils.translateColorCodes(Objects.requireNonNull(Main.getInstance().getConfig().getString("report.noPermissionMessage"))));
              return true;
        }
        if (args.length < 2) {
            player.sendMessage(ColorUtils.translateColorCodes(Objects.requireNonNull(Main.getInstance().getConfig().getString("report.usageMessage"))));
            return true;
        }
        String reportedPlayerName = args[0];
        String reportReason = String.join(" ", Arrays.<CharSequence>copyOfRange(args, 1, args.length));
        long currentTime = System.currentTimeMillis();
        long lastReportTime = this.cooldowns.getOrDefault(player.getUniqueId(), 0L);
        long timeRemaining = (lastReportTime + Main.getInstance().getConfig().getInt("report.cooldown") * 1000L - currentTime) / 1000L;
        if (timeRemaining > 0L) {
            String cooldownMessage = "&cPlease wait " + timeRemaining + " seconds before submitting another report.";
            player.sendMessage(ColorUtils.translateColorCodes(cooldownMessage));
            return true;
        }
        sendWebhook(player, reportedPlayerName, reportReason);
        this.cooldowns.put(player.getUniqueId(), currentTime);
        player.sendMessage(ColorUtils.translateColorCodes(Objects.requireNonNull(Main.getInstance().getConfig().getString("report.sentMessage"))));
        return true;
    }

    private void sendWebhook(Player player, String reportedPlayerName, String reportReason) {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(Objects.requireNonNull(Main.getInstance().getConfig().getString("report.webhookUrl")));
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "ReportWebhook");
                connection.setDoOutput(true);
                String message = Objects.requireNonNull(Main.getInstance().getConfig().getString("report.message")).replace("%player%", player.getName()).replace("%reported_player%", reportedPlayerName).replace("%reason%", reportReason).replace("\n", "\\n");
                message = "{\"username\":\"" + Main.getInstance().getConfig().getString("report.username") + "\",\"avatar_url\":\"" + Main.getInstance().getConfig().getString("report.avatarUrl") + "\",\"embeds\":[{\"description\":\"" + message + "\",\"color\":" + getColorCode("#FF0000") + "}]}";
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(message.getBytes());
                }
                connection.getResponseCode();
                connection.getResponseMessage();
            } catch (MalformedURLException e) {
                Bukkit.getLogger().warning("[ReportWebhook] Invalid webhook URL specified: " + Main.getInstance().getConfig().getString("report.webhookUrl"));
                e.printStackTrace();
            } catch (ProtocolException e) {
                Bukkit.getLogger().warning("[ReportWebhook] Invalid protocol specified in webhook URL: " + Main.getInstance().getConfig().getString("report.webhookUrl"));
                e.printStackTrace();
            } catch (IOException e) {
                Bukkit.getLogger().warning("[ReportWebhook] Error sending message to Discord webhook.");
                e.printStackTrace();
            }
        });
    }

    private int getColorCode(String color) {
        color = color.replace("#", "");
        return Integer.parseInt(color, 16);
    }
}
