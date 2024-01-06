package me.dankofuk.discord.commands.botRequiredCommands;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import me.dankofuk.KushStaffUtils;
import me.dankofuk.discord.DiscordBot;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.dankofuk.utils.ColorUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.event.Listener;

public class SuggestionCommand extends ListenerAdapter implements CommandExecutor, Listener {
    private final DiscordBot discordBot;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    public FileConfiguration config;


    public SuggestionCommand(DiscordBot discordBot, FileConfiguration config) {
        this.discordBot = discordBot;
        this.config = config;
    }

    public void accessConfigs() {
        String channelId = KushStaffUtils.getInstance().getConfig().getString("suggestion.channelId");
        String suggestionUsageMessage = KushStaffUtils.getInstance().getConfig().getString("suggestion.usageMessage");
        String responseMessage = KushStaffUtils.getInstance().getConfig().getString("suggestion.sentMessage");
        long cooldown = KushStaffUtils.getInstance().getConfig().getLong("suggestion.cooldown");
        String title = KushStaffUtils.getInstance().getConfig().getString("suggestion.title");
        String description = KushStaffUtils.getInstance().getConfig().getString("suggestion.description");
        Color color = Color.decode(KushStaffUtils.getInstance().getConfig().getString("suggestion.color"));
        String footer = KushStaffUtils.getInstance().getConfig().getString("suggestion.footer");
        String thumbnailUrl = KushStaffUtils.getInstance().getConfig().getString("suggestion.thumbnailUrl");
        String noPermission = KushStaffUtils.getInstance().getConfig().getString("suggestion.noPermissionMessage");

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        if (!config.getBoolean("bot.enabled")) {
            sender.sendMessage("The bot is not enabled. Suggestion command will not function.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("commandlogger.suggest.use")) {
            player.sendMessage(ColorUtils.translateColorCodes(KushStaffUtils.getInstance().getConfig().getString("suggestion.noPermissionMessage")));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ColorUtils.translateColorCodes(KushStaffUtils.getInstance().getConfig().getString("suggestion.usageMessage")));
            return true;
        }

        String suggest = String.join(" ", args);
        long currentTime = System.currentTimeMillis();
        long lastSuggestTime = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        long timeRemaining = (lastSuggestTime + (KushStaffUtils.getInstance().getConfig().getLong("suggestion.cooldown") * 1000L) - currentTime) / 1000L;

        if (timeRemaining > 0) {

            String cooldownMessage = "&cPlease wait " + timeRemaining + " seconds before submitting another suggestion.";
            player.sendMessage(ColorUtils.translateColorCodes(cooldownMessage));
            return true;
        }

        sendSuggestion(player, suggest);
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        player.sendMessage(ColorUtils.translateColorCodes(KushStaffUtils.getInstance().getConfig().getString("suggestion.sentMessage").replace("%suggestion%", suggest)));

        return true;
    }

    private void sendSuggestion(Player player, String suggestion) {
        CompletableFuture.runAsync(() -> {
            try {
                TextChannel channel = discordBot.getJda().getTextChannelById(KushStaffUtils.getInstance().getConfig().getString("suggestion.channelId"));
                if (channel == null) {
                    Bukkit.getLogger().warning("[SuggestionCommandListener] Invalid channel ID specified: " + KushStaffUtils.getInstance().getConfig().getString("suggestion.channelId"));
                    return;
                }

                String playerName = player.getName();
                String playerUUID = player.getUniqueId().toString();

                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(KushStaffUtils.getInstance().getConfig().getString("suggestion.title").replace("%suggestion%", suggestion).replace("%player%", playerName).replace("%player_uuid%", playerUUID));
                embed.setDescription(KushStaffUtils.getInstance().getConfig().getString("suggestion.description").replace("%suggestion%", suggestion).replace("%player%", playerName).replace("%player_uuid%", playerUUID));
                embed.setColor(Color.decode(KushStaffUtils.getInstance().getConfig().getString("suggestion.color")));
                embed.setFooter(KushStaffUtils.getInstance().getConfig().getString("suggestion.footer").replace("%suggestion%", suggestion).replace("%player%", playerName).replace("%player_uuid%", playerUUID), null);
                embed.setThumbnail(KushStaffUtils.getInstance().getConfig().getString("suggestion.thumbnailUrl"));

                channel.sendMessageEmbeds(embed.build()).queue(sentMessage -> {
                    sentMessage.addReaction(Emoji.fromUnicode("U+1F44D")).queue(); // thumbs up reaction
                    sentMessage.addReaction(Emoji.fromUnicode("U+1F44E")).queue(); // thumbs down reaction
                });
            } catch (Exception e) {
                Bukkit.getLogger().warning("[SuggestionCommandListener] Error sending suggestion: " + e.getMessage());
            }
        });
    }
}
