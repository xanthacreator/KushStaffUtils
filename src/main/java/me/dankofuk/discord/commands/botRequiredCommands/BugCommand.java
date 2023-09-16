package me.dankofuk.discord.commands.botRequiredCommands;

import me.dankofuk.KushStaffUtils;
import me.dankofuk.discord.DiscordBot;
import me.dankofuk.utils.ColorUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BugCommand extends ListenerAdapter implements CommandExecutor, Listener {
    private KushStaffUtils instance;
    public FileConfiguration config;
    public final Map<UUID, Long> cooldowns = new HashMap<>();
    public DiscordBot discordBot;

    public BugCommand(KushStaffUtils instance, DiscordBot discordBot, FileConfiguration config) {
        this.instance = instance;
        this.discordBot = discordBot;
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("commandlogger.bug.use")) {
            player.sendMessage(ColorUtils.translateColorCodes(Objects.requireNonNull(KushStaffUtils.getInstance().getConfig().getString("bug_report.noPermissionMessage"))));
            return true;
        }

        if (!KushStaffUtils.getInstance().getConfig().getBoolean("bug_report.enabled")) {
            player.sendMessage(ColorUtils.translateColorCodes("&cReporting bugs is currently disabled."));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ColorUtils.translateColorCodes(Objects.requireNonNull(KushStaffUtils.getInstance().getConfig().getString("bug_report.usageMessage"))));
            return true;
        }

        String bugReport = String.join(" ", args);
        long currentTime = System.currentTimeMillis();
        long lastReportTime = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        long timeRemaining = (lastReportTime + (KushStaffUtils.getInstance().getConfig().getLong("bug_report.cooldown") * 1000L) - currentTime) / 1000L;

        if (timeRemaining > 0) {
            String cooldownMessage = "&cPlease wait " + timeRemaining + " seconds before submitting another report.";
            player.sendMessage(ColorUtils.translateColorCodes(cooldownMessage));
            return true;
        }
        sendBug(player, bugReport);
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        player.sendMessage(ColorUtils.translateColorCodes(Objects.requireNonNull(KushStaffUtils.getInstance().getConfig().getString("bug_report.responseMessage")).replace("%reason%", bugReport)));

        return true;
    }

    private void sendBug(Player player, String bugReport) {
        CompletableFuture.runAsync(() -> {
            try {
                TextChannel channel = discordBot.getJda().getTextChannelById(KushStaffUtils.getInstance().getConfig().getString("bug_report.channelId"));
                if (channel == null) {
                    Bukkit.getLogger().warning("[Bug Command] Invalid channel ID specified: " + KushStaffUtils.getInstance().getConfig().getString("bug_report.channelId"));
                    return;
                }

                String playerName = player.getName();
                String playerUUID = player.getUniqueId().toString();

                EmbedBuilder embed = new EmbedBuilder();
                long time = System.currentTimeMillis() / 1000L;
                embed.setTitle(KushStaffUtils.getInstance().getConfig().getString("bug_report.embed_title").replace("%bug%", bugReport).replace("%player%", playerName).replace("%time%", "<t:" + time + ":R>"));
                embed.setDescription(KushStaffUtils.getInstance().getConfig().getString("bug_report.message").replace("%bug%", bugReport).replace("%player%", playerName).replace("%time%", "<t:" + time + ":R>"));

                channel.sendMessageEmbeds(embed.build()).queue();
            } catch (Exception e) {
                Bukkit.getLogger().warning("[Bug Command] Error sending bug report: " + e.getMessage());
            }
        });
    }

    private int getColorCode(String color) {
        color = color.replace("#", "");
        return Integer.parseInt(color, 16);
    }
}
