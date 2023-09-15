package me.dankofuk.commands;

import me.dankofuk.KushStaffUtils;
import me.dankofuk.utils.ColorUtils;
import me.dankofuk.utils.WebhookUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.*;

public class ReportCommand implements Listener, CommandExecutor {
    private KushStaffUtils main;
    private WebhookUtils webhookUtils;
    public final Map<UUID, Long> cooldowns = new HashMap<>();
    public FileConfiguration config;

    public ReportCommand(FileConfiguration config) {
        this.config = config;
    }

    public void accessConfigs() {
        String ReportWebhookUrl = KushStaffUtils.getInstance().getConfig().getString("report.webhookUrl");
        String username = KushStaffUtils.getInstance().getConfig().getString("report.username");
        String avatarUrl = KushStaffUtils.getInstance().getConfig().getString("report.avatarUrl");
        boolean isReportEnabled = KushStaffUtils.getInstance().getConfig().getBoolean("report.enabled");
        String reportMessage = KushStaffUtils.getInstance().getConfig().getString("report.message");
        int reportCooldown = KushStaffUtils.getInstance().getConfig().getInt("report.cooldown");
        String reportSentMessage = KushStaffUtils.getInstance().getConfig().getString("report.sentMessage");
        String noPermissionMessage = KushStaffUtils.getInstance().getConfig().getString("report.noPermissionMessage");
        String usageMessage = KushStaffUtils.getInstance().getConfig().getString("report.usageMessage");
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player)sender;
        if (!player.hasPermission("commandlogger.report.use")) {
            player.sendMessage(ColorUtils.translateColorCodes(Objects.requireNonNull(KushStaffUtils.getInstance().getConfig().getString("report.noPermissionMessage"))));
              return true;
        }
        if (args.length < 2) {
            player.sendMessage(ColorUtils.translateColorCodes(Objects.requireNonNull(KushStaffUtils.getInstance().getConfig().getString("report.usageMessage"))));
            return true;
        }
        String reportedPlayerName = args[0];
        String reportReason = String.join(" ", Arrays.<CharSequence>copyOfRange(args, 1, args.length));
        long currentTime = System.currentTimeMillis();
        long lastReportTime = this.cooldowns.getOrDefault(player.getUniqueId(), 0L);
        long timeRemaining = (lastReportTime + KushStaffUtils.getInstance().getConfig().getInt("report.cooldown") * 1000L - currentTime) / 1000L;
        if (timeRemaining > 0L) {
            String cooldownMessage = "&cPlease wait " + timeRemaining + " seconds before submitting another report.";
            player.sendMessage(ColorUtils.translateColorCodes(cooldownMessage));
            return true;
        }
        sendWebhook(player, reportedPlayerName, reportReason);
        this.cooldowns.put(player.getUniqueId(), currentTime);
        player.sendMessage(ColorUtils.translateColorCodes(Objects.requireNonNull(KushStaffUtils.getInstance().getConfig().getString("report.sentMessage"))));
        return true;
    }

    private void sendWebhook(Player player, String reportedPlayerName, String reportReason) {
        try {
            this.webhookUtils = new WebhookUtils(Objects.requireNonNull(KushStaffUtils.getInstance().getConfig().getString("report.webhookUrl")));
            long time = System.currentTimeMillis() / 1000L;
            WebhookUtils.EmbedObject embedObject = new WebhookUtils.EmbedObject()
                    .setDescription(Objects.requireNonNull(KushStaffUtils.getInstance().getConfig().getString("report.message"))
                            .replace("%player%", player.getName())
                            .replace("%reported_player%", reportedPlayerName)
                            .replace("%reason%", reportReason)
                            .replace("\n", "\\n")
                            .replace("%time%", "<t:" + time + ":R>"))
                    .setColor(Color.RED);

            webhookUtils.setContent(null); // Set content to null if you don't want any text content
            webhookUtils.setUsername(KushStaffUtils.getInstance().getConfig().getString("report.username"));
            webhookUtils.setAvatarUrl(KushStaffUtils.getInstance().getConfig().getString("report.avatarUrl"));
            webhookUtils.addEmbed(embedObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            webhookUtils.execute();
        } catch (IOException e) {
            Bukkit.getLogger().warning("[ReportWebhook] Error sending message to Discord webhook.");
            e.printStackTrace();
        }
    }

    private int getColorCode(String color) {
        color = color.replace("#", "");
        return Integer.parseInt(color, 16);
    }
}
