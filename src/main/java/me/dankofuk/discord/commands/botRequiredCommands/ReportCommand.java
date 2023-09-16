package me.dankofuk.discord.commands.botRequiredCommands;

import me.dankofuk.KushStaffUtils;
import me.dankofuk.discord.DiscordBot;
import me.dankofuk.utils.ColorUtils;
import me.dankofuk.utils.WebhookUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
import java.util.concurrent.CompletableFuture;

public class ReportCommand implements Listener, CommandExecutor {
    private final DiscordBot discordBot;
    private final KushStaffUtils instance;
    public final Map<UUID, Long> cooldowns = new HashMap<>();

    public ReportCommand(KushStaffUtils instance, DiscordBot discordBot) {
        this.instance = instance;
        this.discordBot = discordBot;
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
        CompletableFuture.runAsync(() -> {
            try {
                TextChannel channel = discordBot.getJda().getTextChannelById(KushStaffUtils.getInstance().getConfig().getString("report.channelId"));
                if (channel == null) {
                    Bukkit.getLogger().warning("[Player Report Command] Invalid channel ID specified: " + KushStaffUtils.getInstance().getConfig().getString("report.channelId"));
                    return;
                }

                String playerName = player.getName();
                String playerUUID = player.getUniqueId().toString();

                EmbedBuilder embed = new EmbedBuilder();
                long time = System.currentTimeMillis() / 1000L;
                embed.setTitle(KushStaffUtils.getInstance().getConfig().getString("report.embed_title").replace("%reason%", reportReason).replace("%reported_player%", reportedPlayerName).replace("%player%", playerName).replace("%time%", "<t:" + time + ":R>"));
                embed.setDescription(KushStaffUtils.getInstance().getConfig().getString("report.message").replace("%reason%", reportReason).replace("%reported_player%", reportedPlayerName).replace("%player%", playerName).replace("%time%", "<t:" + time + ":R>"));

                channel.sendMessageEmbeds(embed.build()).queue();
            } catch (Exception e) {
                Bukkit.getLogger().warning("[Player Report Command] Error sending player report: " + e.getMessage());
            }
        });
    }

    private int getColorCode(String color) {
        color = color.replace("#", "");
        return Integer.parseInt(color, 16);
    }
}
