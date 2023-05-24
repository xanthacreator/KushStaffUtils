package me.dankofuk.discord.commands;

import me.dankofuk.DiscordLogger;
import me.dankofuk.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class ReloadCommand extends ListenerAdapter {

    private final DiscordBot discordBot;
    private String commandPrefix;
    private FileConfiguration config;
    public Plugin botTask;
    public String discordToken;
    private boolean discordBotEnabled;
    private Server minecraftServer;
    private String discordActivity;
    private JDA jda;
    private String adminRoleID;
    private String ServerStatusChannelID;
    private DiscordLogger discordLogger;
    public String logChannelId;
    public boolean logAsEmbed;
    private boolean enabled;
    private String channelId;
    private String format;
    private boolean roleIdRequired;
    private String roleId;

    public DiscordBot getDiscordBot() {
        return discordBot;
    }

    public ReloadCommand(DiscordBot discordBot, String commandPrefix, FileConfiguration config, String logChannelId, boolean logAsEmbed, boolean enabled, String channelId, String format, boolean roleIdRequired, String roleId) {
        this.discordBot = discordBot;
        this.commandPrefix = commandPrefix;
        this.config = config;
        this.botTask = discordBot.botTask;
        this.discordToken = discordBot.discordToken;
        this.discordBotEnabled = discordBot.discordBotEnabled;
        this.adminRoleID = discordBot.adminRoleID;
        this.discordActivity = discordBot.discordActivity;
        this.ServerStatusChannelID = discordBot.ServerStatusChannelID;
        this.logChannelId = logChannelId;
        this.logAsEmbed = logAsEmbed;
        this.enabled = enabled;
        this.channelId = channelId;
        this.format = format;
        this.roleIdRequired = roleIdRequired;
        this.roleId = roleId;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String message = event.getMessage().getContentRaw();

        if (message.equalsIgnoreCase(commandPrefix + "reloadconfig")) {
            String adminRoleID = config.getString("bot.adminRoleID");
            if (!event.getMember().getRoles().stream().map(Role::getId).collect(Collectors.toList()).contains(adminRoleID)) {
                EmbedBuilder noPerms = new EmbedBuilder();
                noPerms.setColor(Color.RED);
                noPerms.setTitle("Error #NotDankEnough");
                noPerms.setDescription(">  `You lack the required permissions for this command!`");
                noPerms.setFooter(OffsetDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
                event.getChannel().sendMessageEmbeds(noPerms.build()).queue();
                return;
            }

            // Reload configuration file
            try {
                config.load(new File("plugins/KushStaffUtils/config.yml"));
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }

            boolean enabled = config.getBoolean("bot.discord_to_game_enabled");
            String channelId = config.getString("bot.discord_to_game_channel_id");
            String roleId = config.getString("bot.discord_to_game_roleId");
            boolean roleIdRequired = config.getBoolean("bot.discord_to_game_roleIdRequired");
            String format = config.getString("bot.discord_to_game_format");
            String discordToken = config.getString("bot.discord_token");
            boolean discordBotEnabled = config.getBoolean("bot.enabled");
            String commandPrefix = config.getString("bot.command_prefix");
            String discordActivity = config.getString("bot.discord_activity");
            String ServerStatusChannelID = config.getString("serverstatus.channel_id");
            String logChannelId = config.getString("bot.command_log_channel_id");
            boolean logAsEmbed = config.getBoolean("bot.command_log_logAsEmbed");
            Server minecraftServer = Bukkit.getServer();
            Bukkit.getScheduler().getPendingTasks().stream()
                    .filter(task -> task.getOwner() == botTask)
                    .forEach(task -> task.cancel());
            discordBot.reloadDiscordConfig(discordToken, discordBotEnabled, minecraftServer, commandPrefix, adminRoleID, discordActivity, botTask, config, ServerStatusChannelID, logChannelId, logAsEmbed);
            discordBot.stop();

            // Reload config strings
            this.discordToken = discordToken;
            this.discordBotEnabled = discordBotEnabled;
            this.commandPrefix = commandPrefix;
            this.adminRoleID = adminRoleID;
            this.discordActivity = discordActivity;
            this.ServerStatusChannelID = ServerStatusChannelID;
            this.logChannelId = logChannelId;
            this.logAsEmbed = logAsEmbed;
            this.enabled = enabled;
            this.channelId = channelId;
            this.format = format;
            this.roleIdRequired = roleIdRequired;
            this.roleId = roleId;

            EmbedBuilder stoppedEmbed = new EmbedBuilder();
            stoppedEmbed.setColor(Color.RED);
            stoppedEmbed.setTitle("Bot stopped!");
            stoppedEmbed.setDescription(">  `Reloading configuration....`");
            stoppedEmbed.setFooter(OffsetDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
            event.getChannel().sendMessageEmbeds(stoppedEmbed.build()).queue();
            try {
                discordBot.start();
                System.out.println("[KushStaffUtils - Discord Bot] Reloading Discord Bot...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            EmbedBuilder startedEmbed = new EmbedBuilder();
            startedEmbed.setColor(Color.GREEN);
            startedEmbed.setTitle("Bot started!");
            startedEmbed.setDescription(">  `Reload Complete!`");
            startedEmbed.setFooter(OffsetDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
            event.getChannel().sendMessageEmbeds(startedEmbed.build()).queue();
           }
        }
    }

