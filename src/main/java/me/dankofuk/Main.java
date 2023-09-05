package me.dankofuk;

import me.dankofuk.commands.BugCommand;
import me.dankofuk.commands.CommandLogViewer;
import me.dankofuk.commands.ReportCommand;
import me.dankofuk.discord.DiscordBot;
import me.dankofuk.discord.commands.botRequiredCommands.SuggestionCommand;
import me.dankofuk.discord.listeners.ChatWebhook;
import me.dankofuk.discord.listeners.CommandLogger;
import me.dankofuk.discord.listeners.StartStopLogger;
import me.dankofuk.factionstuff.FactionStrike;
import me.dankofuk.factionstuff.FactionsTopAnnouncer;
import me.dankofuk.listeners.FileCommandLogger;
import me.dankofuk.listeners.JoinLeaveLogger;
import me.dankofuk.utils.ColorUtils;
import net.dv8tion.jda.api.JDA;
import org.bstats.bukkit.Metrics;
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

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main extends JavaPlugin implements Listener {
    private static String logsFolder;
    private static Main instance;
    private JDA jda;
    private Plugin plugin;

    private List<String> ignoredCommands;
    public FileConfiguration config;

    private CommandLogger commandLogger;
    public JoinLeaveLogger joinLeaveLogger;
    public FileCommandLogger fileCommandLogger;
    public ReportCommand reportCommand;
    public String reportSentMessage;
    private BugCommand bugCommand;
    private SuggestionCommand suggestionCommand;
    private DiscordBot discordBot;
    private StartStopLogger serverStatus;
    private FactionStrike factionStrike;
    private FactionsTopAnnouncer factionsTopAnnouncer;

    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        instance = this;
        PluginManager pluginManager = Bukkit.getPluginManager();
        Plugin placeholderAPI = pluginManager.getPlugin("PlaceholderAPI");
        if (placeholderAPI == null)
            getLogger().warning("PlaceholderAPI is not installed or enabled. Some placeholders may not work.");
        Plugin vault = pluginManager.getPlugin("Vault");
        if (vault == null)
            getLogger().warning("Vault is not installed or enabled. Some functionality may be limited.");
        int pluginId = 18185;
        Metrics metrics = new Metrics(this, pluginId);
        String noPermissionMessage = config.getString("no-permission-message");
        String logsFolder = (new File(getDataFolder(), "logs")).getPath();
        this.fileCommandLogger = new FileCommandLogger(logsFolder);
        boolean logCommands = getConfig().getBoolean("per-user-logging.enabled", true);
        this.fileCommandLogger.reloadLogCommands(logCommands);
        String serverName = getConfig().getString("server_name");
        String discordToken = getConfig().getString("bot.discord_token");
        boolean discordBotEnabled = getConfig().getBoolean("bot.enabled");
        Server minecraftServer = getServer();
        String adminRoleID = getConfig().getString("bot.adminRoleID");
        String discordActivity = getConfig().getString("bot.discord_activity");
        String ServerStatusChannelID = getConfig().getString("serverstatus.channel_id");
        String logChannelId = getConfig().getString("bot.command_log_channel_id");
        String noPlayersTitle = config.getString("bot.listplayers_no_players_online_title");
        String titleFormat = config.getString("bot.listplayers_title_format");
        String footerFormat = config.getString("bot.listplayers_footer_format");
        String listThumbnailUrl = config.getString("bot.listplayers_thumbnail_url");
        boolean logAsEmbed = getConfig().getBoolean("bot.command_log_logAsEmbed");
        boolean requireAdminRole = config.getBoolean("bot.listplayers_requireAdminRole");
        boolean logsCommandRequiresAdminRole = config.getBoolean("bot.logsCommand_requireAdminRole");
        List<String> messageFormats = config.getStringList("bot.command_log_message_formats");
        List<String> embedTitleFormats = config.getStringList("bot.command_log_embed_title_formats");
        if (config.getBoolean("bot.enabled")) {
            if ("false".equals(discordToken) || discordToken.isEmpty()) {
                System.out.println("[KushStaffUtils - Discord Bot] No bot token found. Bot initialization skipped.");
                return;
            }
            this.discordBot = new DiscordBot(discordToken, discordBotEnabled, minecraftServer, adminRoleID, discordActivity, this, config, ServerStatusChannelID, logChannelId, logAsEmbed, serverName, titleFormat, footerFormat, listThumbnailUrl, noPlayersTitle, requireAdminRole, logsCommandRequiresAdminRole, this.plugin);
            try {
                this.discordBot.start();
                System.out.println("[KushStaffUtils - Discord Bot] Starting Discord Bot...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("[KushStaffUtils - Discord Bot] Bot is disabled. Skipping initialization...");
        }
        CommandLogViewer commandLogViewer = new CommandLogViewer(getDataFolder().getPath() + File.separator + "logs", 15);
        getCommand("viewlogs").setExecutor(commandLogViewer);
        if (logCommands)
            Bukkit.getServer().getPluginManager().registerEvents(this.fileCommandLogger, this);
        ChatWebhook chatWebhook = new ChatWebhook(config.getString("chatwebhook.url"), config.getString("chatwebhook.username"), config.getString("chatwebhook.avatarUrl"), config.getString("chatwebhook.message"), config.getBoolean("chatwebhook.enabled"), config);
        getServer().getPluginManager().registerEvents(chatWebhook, this);

        //
        // New Config - Finished Classes
        //

        // Factions/Skyblock Top Announcer (Webhook)
        if (!config.getBoolean("announcer.enabled")) {
            getLogger().warning("Factions Top Announcer - [Not Enabled]");
        } else {
            this.factionsTopAnnouncer = new FactionsTopAnnouncer(config);
            getLogger().warning("Factions Top Announcer - [Enabled]");
        }

        // Player Report Command (Webhook + Command)
        if (!config.getBoolean("report.enabled")) {
            getLogger().warning("Player Reporting Command - [Not Enabled]");
        } else {
            this.reportCommand = new ReportCommand(config);
            getCommand("report").setExecutor(this.factionStrike);
            Bukkit.getPluginManager().registerEvents(reportCommand, this);
            getLogger().warning("Player Reporting Command - [Enabled]");
        }
        // Strike Command (Webhook + Command)
        if (!config.getBoolean("strike.enabled")) {
            getLogger().warning("Strike Command - [Not Enabled]");
                } else {
                    this.factionStrike = new FactionStrike(config);
                    getCommand("strike").setExecutor(this.factionStrike);
                    getLogger().warning("Strike Command - [Enabled]");
        }
        // Bug Report Command (Webhook + Command)
        if (!config.getBoolean("bug_report.enabled")) {
            getLogger().warning("Bug Command - [Not Enabled]");
                } else {
                    this.bugCommand = new BugCommand(config);
                    getServer().getPluginManager().registerEvents(this.bugCommand, this);
                    getCommand("bug").setExecutor(this.bugCommand);
                    getLogger().warning("Bug Command - [Enabled]");
            }
        // Join Leave Logger (Webhooks)
        if (!config.getBoolean("player_join_leave_logger.enabled")) {
            getLogger().warning("Player Join Leave Logger - [Not Enabled]");
        } else {
            this.joinLeaveLogger = new JoinLeaveLogger(config);
            getLogger().warning("Player Join Leave Logger - [Enabled]");
        }
        // Suggestion Command (Discord Bot + Command)
        if (!config.getBoolean("bot.enabled")) {
            getLogger().warning("Suggestion Command - [Not Enabled] - (Requires Discord Bot enabled)");
        } else {
            this.suggestionCommand = new SuggestionCommand(this.discordBot, config);
            getCommand("suggest").setExecutor(this.suggestionCommand);
            getCommand("suggestion").setExecutor(this.suggestionCommand);
            getServer().getPluginManager().registerEvents(this.suggestionCommand, this);
            getLogger().warning("Suggestion Command - [Enabled]");
        }

        this.ignoredCommands = getConfig().getStringList("ignored_commands");
        new ThreadPoolExecutor(5, 10, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        this.commandLogger = new CommandLogger(this.discordBot, messageFormats, embedTitleFormats, serverName, logAsEmbed, logChannelId);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getServer().getPluginManager().registerEvents(this.joinLeaveLogger, this);
        Bukkit.getConsoleSender().sendMessage("[KushStaffUtils] Plugin has been enabled");
    }

    public void onDisable() {
        FileConfiguration config = getConfig();
        boolean discordBotEnabled = config.getBoolean("bot.enabled");
        if (discordBotEnabled) {
            this.discordBot.stop();
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
        this.commandLogger.logCommand(event.getMessage(), playerName);
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
        ChatWebhook chatWebhook = new ChatWebhook(config.getString("chatwebhook.url"), config.getString("chatwebhook.username"), config.getString("chatwebhook.avatarUrl"), config.getString("chatwebhook.message"), config.getBoolean("chatwebhook.enabled"), config);
        boolean logCommands = getConfig().getBoolean("log_commands");
        this.fileCommandLogger.reloadLogCommands(logCommands);
        String serverName = getConfig().getString("server_name");
        this.ignoredCommands = getConfig().getStringList("ignored_commands");
        List<String> messageFormats = getConfig().getStringList("message_formats");
        List<String> embedTitleFormats = getConfig().getStringList("embed_title_formats");
        boolean logAsEmbed = getConfig().getBoolean("logAsEmbed");
        String logChannelId = getConfig().getString("bot.command_log_channel_id");
        this.commandLogger.reloadLogAsEmbed(logAsEmbed);
        this.commandLogger.reloadMessageFormats(messageFormats);
        this.commandLogger.reloadEmbedTitleFormats(embedTitleFormats);
        this.commandLogger.setServerName(serverName);
        this.commandLogger.reloadLogChannelID(logChannelId);
        // Discord Bot Stuff
        this.discordBot.accessConfigs();
        // Instance Reloads
        this.factionStrike.accessConfigs();
        this.bugCommand.accessConfigs();
        this.reportCommand.accessConfigs();
        this.joinLeaveLogger.accessConfigs();
        this.suggestionCommand.accessConfigs();
        Bukkit.getConsoleSender().sendMessage("[KushStaffUtils] Config options have been reloaded!");
    }

    public static String getCommandLoggerFolder() {
        return logsFolder;
    }

    public static Main getInstance() {
        return instance;
    }
}
