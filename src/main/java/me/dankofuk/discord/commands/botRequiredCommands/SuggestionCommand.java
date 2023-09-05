package me.dankofuk.discord.commands.botRequiredCommands;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import me.dankofuk.Main;
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
    private DiscordBot discordBot;
    private Map<UUID, Long> cooldowns = new HashMap<>();
    public FileConfiguration config;


    public SuggestionCommand(DiscordBot discordBot, FileConfiguration config) {
        this.discordBot = discordBot;
        this.config = config;

        if (!config.getBoolean("bot.enabled")) {
            Bukkit.getLogger().warning("[SuggestionCommandListener] Bot is not enabled. Suggestion command will not function.");
        }
    }

    public void accessConfigs() {
        String channelId = Main.getInstance().getConfig().getString("suggestion.channelId");
        String suggestionUsageMessage = Main.getInstance().getConfig().getString("suggestion.usageMessage");
        String responseMessage = Main.getInstance().getConfig().getString("suggestion.sentMessage");
        long cooldown = Main.getInstance().getConfig().getLong("suggestion.cooldown");
        String title = Main.getInstance().getConfig().getString("suggestion.title");
        String description = Main.getInstance().getConfig().getString("suggestion.description");
        Color color = Color.decode(Main.getInstance().getConfig().getString("suggestion.color"));
        String footer = Main.getInstance().getConfig().getString("suggestion.footer");
        String thumbnailUrl = Main.getInstance().getConfig().getString("suggestion.thumbnailUrl");
        String noPermission = Main.getInstance().getConfig().getString("suggestion.noPermissionMessage");

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
            player.sendMessage(ColorUtils.translateColorCodes(Main.getInstance().getConfig().getString("suggestion.noPermissionMessage")));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ColorUtils.translateColorCodes(Main.getInstance().getConfig().getString("suggestion.usageMessage")));
            return true;
        }

        String suggest = String.join(" ", args);
        long currentTime = System.currentTimeMillis();
        long lastSuggestTime = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        long timeRemaining = (lastSuggestTime + (Main.getInstance().getConfig().getLong("suggestion.cooldown") * 1000L) - currentTime) / 1000L;

        if (timeRemaining > 0) {

            String cooldownMessage = "&cPlease wait " + timeRemaining + " seconds before submitting another suggestion.";
            player.sendMessage(ColorUtils.translateColorCodes(cooldownMessage));
            return true;
        }

        sendSuggestion(player, suggest);
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        // Send a separate message to the player
        player.sendMessage(ColorUtils.translateColorCodes(Main.getInstance().getConfig().getString("suggestion.sentMessage").replace("%suggestion%", suggest)));

        return true;
    }

    private void sendSuggestion(Player player, String suggestion) {
        CompletableFuture.runAsync(() -> {
            try {
                TextChannel channel = discordBot.getJda().getTextChannelById(Main.getInstance().getConfig().getString("suggestion.channelId"));
                if (channel == null) {
                    Bukkit.getLogger().warning("[SuggestionCommandListener] Invalid channel ID specified: " + Main.getInstance().getConfig().getString("suggestion.channelId"));
                    return;
                }

                String playerName = player.getName();
                String playerUUID = player.getUniqueId().toString();

                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(Main.getInstance().getConfig().getString("suggestion.title").replace("%suggestion%", suggestion).replace("%player%", playerName).replace("%player_uuid%", playerUUID));
                embed.setDescription(Main.getInstance().getConfig().getString("suggestion.description").replace("%suggestion%", suggestion).replace("%player%", playerName).replace("%player_uuid%", playerUUID));
                embed.setColor(Color.decode(Main.getInstance().getConfig().getString("suggestion.color")));
                embed.setFooter(Main.getInstance().getConfig().getString("suggestion.footer").replace("%suggestion%", suggestion).replace("%player%", playerName).replace("%player_uuid%", playerUUID), null);
                embed.setThumbnail(Main.getInstance().getConfig().getString("suggestion.thumbnailUrl"));

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
