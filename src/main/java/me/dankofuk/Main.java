package me.dankofuk;

import me.clip.placeholderapi.metrics.bukkit.Metrics;
import me.dankofuk.chat.ChatWebhook;
import me.dankofuk.commands.*;
import me.dankofuk.discord.DiscordBot;
import me.dankofuk.discord.listeners.DiscordLogger;
import me.dankofuk.discord.listeners.ServerStatus;
import me.dankofuk.factionstuff.EnderPearlCooldown;
import me.dankofuk.factionstuff.FactionStrike;
import me.dankofuk.listeners.FileCommandLogger;
import me.dankofuk.listeners.FlyBoostListener;
import me.dankofuk.listeners.JoinLeaveLogger;
import me.dankofuk.utils.ColorUtils;
import net.dv8tion.jda.api.JDA;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main extends JavaPlugin implements Listener {
    private DiscordLogger DLogger;
    private List<String> ignoredCommands;
    public String joinWebhookUrl;
    private ReportCommand reportCommand;
    public String leaveWebhookUrl;
    public List<String> joinMessage;
    public List<String> leaveMessage;
    public boolean useEmbed;
    public boolean isEnabled;
    public boolean isReportEnabled;
    public me.dankofuk.listeners.JoinLeaveLogger JoinLeaveLogger;
    public me.dankofuk.listeners.FileCommandLogger FileCommandLogger;
    public ReportCommand ReportCommand;
    public String LogsHeader;
    public String NextPage;
    public String PrevPage;
    public String reportMessage;
    public String ReportWebhookUrl;
    public String username;
    public String avatarUrl;
    public int cooldownSeconds;
    public String UsageMessage;
    public FileConfiguration FileConfiguration;
    public String reportSentMessage;
    public String usageMessage;
    private FactionStrike factionStrike;
    private static Main instance;
    private EnderPearlCooldown enderPearlCooldown;
    private FactionStrike FactionStrike;
    private BugCommand BugCommand;
    private SuggestionCommand suggestionCommand;
    private DiscordBot discordBot;
    private ServerStatus serverStatus;
    private FlyBoostListener flyBoostListener;
    private JDA jda;
    private Plugin plugin;


    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        // BStats
        int pluginId = 18185; // <-- Replace with the id of your plugin!
        Metrics metrics = new Metrics(this, pluginId);

        // Check if PlaceholderAPI is installed
        PluginManager pluginManager = Bukkit.getPluginManager();
        Plugin placeholderAPI = pluginManager.getPlugin("PlaceholderAPI");
        if (placeholderAPI == null || !placeholderAPI.isEnabled()) {
            getLogger().warning("PlaceholderAPI is not installed or enabled. Some placeholders may not work.");
        }

        // Check if Vault is installed
        Plugin vault = pluginManager.getPlugin("Vault");
        if (vault == null || !vault.isEnabled()) {
            getLogger().warning("Vault is not installed or enabled. Some functionality may be limited.");
        }

        // No Permission String
        String noPermissionMessage = config.getString("no-permission-message");
        // Files
        String logsFolder = (new File(getDataFolder(), "logs")).getPath();
        this.FileCommandLogger = new FileCommandLogger(logsFolder);
        boolean logCommands = getConfig().getBoolean("log_commands", true);
        this.FileCommandLogger.reloadLogCommands(logCommands);

          // Main Discord Bot
        String serverName = getConfig().getString("server_name");
        String discordToken = getConfig().getString("bot.discord_token");
        boolean discordBotEnabled = getConfig().getBoolean("bot.enabled");
        Server minecraftServer = getServer();
        String commandPrefix = getConfig().getString("bot.command_prefix");
        String adminRoleID = getConfig().getString("bot.adminRoleID");
        String discordActivity = getConfig().getString("bot.discord_activity");;
        String ServerStatusChannelID = getConfig().getString("serverstatus.channel_id");
        String logChannelId = getConfig().getString("bot.command_log_channel_id");
        String titleFormat = config.getString("bot.listplayers_title_format");
        String footerFormat = config.getString("bot.listplayers_footer_format");
        String listThumbnailUrl = config.getString("bot.listplayers_thumbnail_url");
        boolean logAsEmbed = getConfig().getBoolean("bot.command_log_logAsEmbed");
        if (config.getBoolean("bot.enabled")) {
            if ("false".equals(discordToken) || discordToken.isEmpty()) {
                System.out.println("[KushStaffUtils - Discord Bot] No bot token found. Bot initialization skipped.");
                return;
            }

            discordBot = new DiscordBot(discordToken, discordBotEnabled, minecraftServer, commandPrefix, adminRoleID, discordActivity, this, config, ServerStatusChannelID, logChannelId, logAsEmbed, serverName, titleFormat, footerFormat, listThumbnailUrl, plugin);
            try {
                discordBot.start();
                System.out.println("[KushStaffUtils - Discord Bot] Starting Discord Bot...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("[KushStaffUtils - Discord Bot] Bot is disabled. Skipping initialization...");
        }


        // Fly Boost Limiter
        FlyBoostListener flyBoostListener = new FlyBoostListener(this, config);
        Bukkit.getServer().getPluginManager().registerEvents(flyBoostListener, this);

        // Command Log Viewer Command
        CommandLogViewer commandLogViewer = new CommandLogViewer(getDataFolder().getPath() + File.separator + "logs", 15);
        getCommand("viewlogs").setExecutor(commandLogViewer);
        if (logCommands)
            Bukkit.getServer().getPluginManager().registerEvents(this.FileCommandLogger, this);
        // Chat Webhook
        String ChatwebhookUrl = config.getString("chatwebhook.url");
        String ChatserverName = config.getString("chatwebhook.serverName");
        String Chatusername = config.getString("chatwebhook.username");
        String ChatavatarUrl = config.getString("chatwebhook.avatarUrl");
        String ChatmessageFormat = config.getString("chatwebhook.message");
        boolean enabled = config.getBoolean("chatwebhook.enabled", true);
        ChatWebhook chatWebhook = new ChatWebhook(ChatwebhookUrl, ChatserverName, Chatusername, ChatavatarUrl, ChatmessageFormat, enabled, config);
        getServer().getPluginManager().registerEvents(chatWebhook, this);
        // Strike Command
        this.factionStrike = new FactionStrike(config.getString("strike.webhookUrl"), config.getString("strike.username"), config.getString("strike.avatarUrl"), config.getBoolean("strike.enabled"), config.getString("strike.message"), config.getString("strike.noPermissionMessage"), config.getString("strike.usageMessage"), config.getString("strike.sendCommand"), config.getString("strike.embedTitle"), config.getString("strike.thumbnail"), config);
        getCommand("strike").setExecutor(this.FactionStrike);
        // Bug Command
        this.BugCommand = new BugCommand(config.getString("bug_webhook_url"), config.getString("bug_username"), config.getString("bug_avatar_url"), config.getBoolean("is_bug_enabled"), config.getString("bug_message"), config.getString("no_bug_permission_message"), config.getString("bug_usage_message"), config.getString("bug_thumbnail"), config.getLong("bug_cooldown"), config.getString("bug_sent_message"), config);
        getServer().getPluginManager().registerEvents(this.BugCommand, this);
        getCommand("bug").setExecutor(this.BugCommand);
        // Initialize the SuggestionCommand
        String suggestionWebhookUrl = config.getString("suggestion.webhook_url");
        String suggestionThumbnail = config.getString("suggestion.thumbnail");
        String channelId = config.getString("suggestion.channel_id");
        String threadId = config.getString("suggestion.thread_id");
        String suggestionMessage = config.getString("suggestion.message");
        String suggestionUsageMessage = config.getString("suggestion.usage_message");
        String responseMessage = config.getString("suggestion.sent_message");
        long cooldown = config.getLong("suggestion.cooldown");
        String title = config.getString("suggestion.title");
        String description = config.getString("suggestion.description");
        Color color = Color.decode(config.getString("suggestion.color"));
        String footer = config.getString("suggestion.footer");
        String thumbnailUrl = config.getString("suggestion.thumbnail_url");

        suggestionCommand = new SuggestionCommand(discordBot, channelId, threadId, suggestionMessage,
                noPermissionMessage, suggestionUsageMessage, responseMessage, cooldown, title, description, footer, color, listThumbnailUrl, config);

        // Register the SuggestionCommand as a command executor
        getCommand("suggest").setExecutor(suggestionCommand);
        getCommand("suggestion").setExecutor(suggestionCommand);

        // Register the SuggestionCommand as a listener to handle the suggestion sent event
        getServer().getPluginManager().registerEvents(this.suggestionCommand, this);
        // Enderpearl and Chorus Fruit Cooldown
        this.enderPearlCooldown = new EnderPearlCooldown(this);
        this.enderPearlCooldown.setEnderpearlCooldownMessage(getConfig().getString("enderpearl.cooldown-message"));
        this.enderPearlCooldown.setEnderpearlCooldown(getConfig().getInt("enderpearl.cooldown-time"));
        this.enderPearlCooldown.setEnderpearlEnabled(getConfig().getBoolean("enderpearl.enabled"));
        this.enderPearlCooldown.setChorusCooldownMessage(getConfig().getString("chorus.cooldown-message"));
        this.enderPearlCooldown.setChorusCooldownTime(getConfig().getInt("chorus.cooldown-time"));
        this.enderPearlCooldown.setChorusEnabled(getConfig().getBoolean("chorus.enabled"));
        this.enderPearlCooldown.start();
        // Report Webhook
        String ReportWebhookUrl = config.getString("webhook-url");
        String username = config.getString("username");
        String avatarUrl = config.getString("avatar-url");
        boolean isReportEnabled = config.getBoolean("enabled");
        String reportMessage = config.getString("report-message");
        int cooldownSeconds = config.getInt("cooldown-seconds");
        String reportSentMessage = config.getString("report-sent-message");
        String usageMessage = config.getString("usage-message");
        ReportCommand reportCommand = new ReportCommand(ReportWebhookUrl, username, avatarUrl, isReportEnabled, reportMessage, cooldownSeconds, reportSentMessage, noPermissionMessage, usageMessage, FileConfiguration);
        Bukkit.getPluginManager().registerEvents(reportCommand, this);
        getCommand("report").setExecutor(reportCommand);
        // JoinLeave - Command Logger Webhook
        this.ignoredCommands = getConfig().getStringList("ignored_commands");
        List<String> messageFormats = config.getStringList("bot.command_log_message_formats");
        List<String> embedTitleFormats = config.getStringList("bot.command_log_embed_title_formats");
        this.joinWebhookUrl = getConfig().getString("joinWebhookUrl");
        this.leaveWebhookUrl = getConfig().getString("leaveWebhookUrl");
        this.joinMessage = getConfig().getStringList("joinMessage");
        this.leaveMessage = getConfig().getStringList("leaveMessage");
        this.useEmbed = getConfig().getBoolean("useEmbed", false);
        this.JoinLeaveLogger = new JoinLeaveLogger(this.joinWebhookUrl, this.leaveWebhookUrl, this.joinMessage, this.leaveMessage, this.useEmbed, this.isEnabled);
        new ThreadPoolExecutor(5, 10, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        DLogger = new DiscordLogger(discordBot, messageFormats, embedTitleFormats, serverName, logAsEmbed, logChannelId);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getServer().getPluginManager().registerEvents(this.JoinLeaveLogger, this);
        Bukkit.getConsoleSender().sendMessage("[KushStaffUtils] Plugin has been enabled");
    }

    public void onDisable() {
        FileConfiguration config = getConfig();
        this.enderPearlCooldown = null;
        boolean discordBotEnabled = config.getBoolean("bot.enabled");

        // Stop Discord bot if it is enabled
        if (discordBotEnabled) {
            discordBot.stop();
            System.out.println("[Discord Bot] Bot has been disabled!");
        } else {
            System.out.println("Discord Bot is disabled, won't stop.");
        }
        Bukkit.getConsoleSender().sendMessage("[KushStaffUtils] Plugin has been disabled!");
    }


    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!event.getPlayer().hasPermission("commandlogger.log") || event.getPlayer().hasPermission("commandlogger.bypass"))
            return;
        String[] args = event.getMessage().split(" ");
        String command = args[0];
        if (isIgnoredCommand(command))
            return;
        if (getConfig().getBoolean("whitelist_enabled")) {
            List<String> whitelistedCommands = getConfig().getStringList("whitelisted_commands");
            if (!isWhitelistedCommand(command, whitelistedCommands))
                return;
        }
        String playerName = event.getPlayer().getName();
        this.DLogger.logCommand(event.getMessage(), playerName);
    }

    private boolean isIgnoredCommand(String command) {
        for (String ignored : this.ignoredCommands) {
            if (ignored.equalsIgnoreCase(command))
                return true;
        }
        return false;
    }

    private boolean isWhitelistedCommand(String command, List<String> whitelistedCommands) {
        for (String whitelisted : whitelistedCommands) {
            if (whitelisted.trim().equalsIgnoreCase(command))
                return true;
        }
        return false;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("stafflogger")) {
            if (!sender.hasPermission("commandlogger.reload")) {
                sender.sendMessage(ColorUtils.translateColorCodes(Objects.requireNonNull(getConfig().getString("no_permission"))));
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                reloadConfigOptions();
                sender.sendMessage(ColorUtils.translateColorCodes(getConfig().getString("reload_message")));
                return true;
            }
            return false;
        }
        return false;
    }

    public void reloadConfigOptions() {
        reloadConfig();
        FileConfiguration config = getConfig();
        String noPermissionMessage = config.getString("no-permission-message");
        // stop Discord bot
        String ChatwebhookUrl = config.getString("chatwebhook.url");
        String ChatserverName = config.getString("chatwebhook.serverName");
        String Chatusername = config.getString("chatwebhook.username");
        String ChatavatarUrl = config.getString("chatwebhook.avatarUrl");
        String ChatmessageFormat = config.getString("chatwebhook.message");
        boolean enabled = config.getBoolean("chatwebhook.enabled", true);
        ChatWebhook chatWebhook = new ChatWebhook(ChatwebhookUrl, ChatserverName, Chatusername, ChatavatarUrl, ChatmessageFormat, enabled, config);
        String strikeWebhookUrl = config.getString("strike.webhookUrl");
        String strikeusername = config.getString("strike.username");
        String strikeavatarUrl = config.getString("strike.avatarUrl");
        boolean isStrikeEnabled = config.getBoolean("strike.enabled");
        String strikeMessage = config.getString("strike.message");
        String strikenoPermissionMessage = config.getString("strike.noPermissionMessage");
        String strikeusageMessage = config.getString("strike.usageMessage");
        String strikeCommand = config.getString("strike.sendCommand");
        String strikeembedTitle = config.getString("strike.embedTitle");
        String strikeThumbnail = config.getString("strike.thumbnail");
        this.factionStrike.reloadConfigOptions(strikeWebhookUrl, strikeusername, strikeavatarUrl, isStrikeEnabled, strikeMessage, strikenoPermissionMessage, strikeusageMessage, strikeCommand, strikeembedTitle, strikeThumbnail, config);
        boolean logCommands = getConfig().getBoolean("log_commands");
        this.FileCommandLogger.reloadLogCommands(logCommands);
        String serverName = getConfig().getString("server_name");
        this.ignoredCommands = getConfig().getStringList("ignored_commands");
        List<String> messageFormats = getConfig().getStringList("message_formats");
        List<String> embedTitleFormats = getConfig().getStringList("embed_title_formats");
        boolean logAsEmbed = getConfig().getBoolean("logAsEmbed");
        String logChannelId = getConfig().getString("bot.command_log_channel_id");
        this.DLogger.reloadLogAsEmbed(logAsEmbed);
        this.DLogger.reloadMessageFormats(messageFormats);
        this.DLogger.reloadEmbedTitleFormats(embedTitleFormats);
        this.DLogger.setServerName(serverName);
        this.DLogger.reloadLogChannelID(logChannelId);
        this.useEmbed = getConfig().getBoolean("useEmbed", false);
        this.isEnabled = getConfig().getBoolean("isEnabled", false);
        this.JoinLeaveLogger.reloadJoinWebhook(getConfig().getString("joinWebhookUrl"));
        this.JoinLeaveLogger.reloadLeaveWebhook(getConfig().getString("leaveWebhookUrl"));
        this.JoinLeaveLogger.setEnabled(this.isEnabled);
        this.JoinLeaveLogger.reloadJoinMessage((ArrayList<String>)this.joinMessage);
        this.JoinLeaveLogger.reloadLeaveMessage((ArrayList<String>)this.leaveMessage);
        this.JoinLeaveLogger.reloadEmbedOption(this.useEmbed);
        String suggestionWebhookUrl = config.getString("suggestion.webhook_url");
        String suggestionThumbnail = config.getString("suggestion.thumbnail");
        String channelId = config.getString("suggestion.channel_id");
        String threadId = config.getString("suggestion.thread_id");
        String suggestionMessage = config.getString("suggestion.message");
        String suggestionUsageMessage = config.getString("suggestion.usage_message");
        String responseMessage = config.getString("suggestion.sent_message");
        long cooldown = config.getLong("suggestion.cooldown");
        String title = config.getString("suggestion.title");
        String description = config.getString("suggestion.description");
        Color color = Color.decode(config.getString("suggestion.color"));
        String footer = config.getString("suggestion.footer");
        String thumbnailUrl = config.getString("suggestion.thumbnail_url");
        this.suggestionCommand.reloadConfigOptions(discordBot, channelId, threadId, suggestionMessage,
                noPermissionMessage, suggestionUsageMessage, responseMessage, cooldown, title, description, footer, color, thumbnailUrl);
        String bugWebhookUrl = config.getString("bug_webhook_url");
        String bugThumbnail = config.getString("bug_thumbnail");
        long bugCooldown = config.getLong("bug_cooldown");
        String bugUsername = config.getString("bug_username");
        String bugAvatarUrl = config.getString("bug_avatar_url");
        boolean isBugEnabled = config.getBoolean("is_bug_enabled");
        String bugMessage = config.getString("bug_message");
        String bugResponse = config.getString("bug_sent_message");
        String noBugPermissionMessage = config.getString("no_bug_permission_message");
        String bugUsageMessage = config.getString("bug_usage_message");
        this.BugCommand.reloadConfigOptions(bugWebhookUrl, bugUsername, bugAvatarUrl, isBugEnabled, bugMessage, noBugPermissionMessage, bugUsageMessage, bugThumbnail, bugCooldown, bugResponse, config);
        this.enderPearlCooldown.setEnderpearlCooldownMessage(getConfig().getString("enderpearl.cooldown-message"));
        this.enderPearlCooldown.setEnderpearlCooldown(getConfig().getInt("enderpearl.cooldown-time"));
        this.enderPearlCooldown.setEnderpearlEnabled(getConfig().getBoolean("enderpearl.enabled"));
        this.enderPearlCooldown.setChorusCooldownMessage(getConfig().getString("chorus.cooldown-message"));
        this.enderPearlCooldown.setChorusCooldownTime(getConfig().getInt("chorus.cooldown-time"));
        this.enderPearlCooldown.setChorusEnabled(getConfig().getBoolean("chorus.enabled"));
        // load new configuration
        String discordToken = getConfig().getString("bot.discord_token");
        boolean discordBotEnabled = getConfig().getBoolean("bot.enabled");
        String commandPrefix = getConfig().getString("bot.command_prefix");
        String adminRoleID = getConfig().getString("bot.adminRoleID");
        String discordActivity = getConfig().getString("bot.discord_activity");
        String ServerStatusChannelID = getConfig().getString("serverstatus.channel_id");
        String ReportWebhookUrl = config.getString("webhook-url");
        String username = config.getString("username");
        String avatarUrl = config.getString("avatar-url");
        boolean isReportEnabled = config.getBoolean("enabled");
        String reportMessage = config.getString("report-message");
        int cooldownSeconds = config.getInt("cooldown-seconds");
        String reportSentMessage = config.getString("report-sent-message");
        String usageMessage = config.getString("usage-message");
        reportCommand = new ReportCommand(ReportWebhookUrl, username, avatarUrl, isReportEnabled, reportMessage, cooldownSeconds, reportSentMessage, noPermissionMessage, usageMessage, config);
        // start new Discord bot session if enabled
        Bukkit.getConsoleSender().sendMessage("[KushStaffUtils] Config options have been reloaded!");

    }
}
