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

    public StartStopLogger startStopLogger;
    public CommandLogger commandLogger;
    public JoinLeaveLogger joinLeaveLogger;
    public FileCommandLogger fileCommandLogger;
    public ReportCommand reportCommand;
    public String reportSentMessage;
    public BugCommand bugCommand;
    public SuggestionCommand suggestionCommand;
    public DiscordBot discordBot;
    public StartStopLogger serverStatus;
    public FactionStrike factionStrike;
    public FactionsTopAnnouncer factionsTopAnnouncer;
    public ChatWebhook chatWebhook;

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
        String discordToken = getConfig().getString("bot.discord_token");
        boolean discordBotEnabled = getConfig().getBoolean("bot.enabled");
        Server minecraftServer = getServer();
        String adminRoleID = getConfig().getString("bot.adminRoleID");
        String discordActivity = getConfig().getString("bot.discord_activity");
        String ServerStatusChannelID = getConfig().getString("serverstatus.channelId");
        String noPlayersTitle = config.getString("bot.listplayers_no_players_online_title");
        String titleFormat = config.getString("bot.listplayers_title_format");
        String footerFormat = config.getString("bot.listplayers_footer_format");
        String listThumbnailUrl = config.getString("bot.listplayers_thumbnail_url");
        boolean requireAdminRole = config.getBoolean("bot.listplayers_requireAdminRole");
        boolean logsCommandRequiresAdminRole = config.getBoolean("bot.logsCommand_requireAdminRole");
        String serverName = Main.getInstance().getConfig().getString("commandlogger.server_name");
        List<String> messageFormats = Main.getInstance().getConfig().getStringList("commandlogger.message_formats");
        List<String> embedTitleFormats = Main.getInstance().getConfig().getStringList("commandlogger.embed_title_formats");
        boolean logAsEmbed = Main.getInstance().getConfig().getBoolean("commandlogger.logAsEmbed");
        String logChannelId = Main.getInstance().getConfig().getString("commandlogger.channel_id");
        if (config.getBoolean("bot.enabled")) {
            if ("false".equals(discordToken) || discordToken.isEmpty()) {
                getLogger().warning("[Discord Bot] No bot token found. Bot initialization skipped.");
                return;
            }
            this.discordBot = new DiscordBot(discordToken, discordBotEnabled, minecraftServer, adminRoleID, discordActivity, this, config, logChannelId, logAsEmbed, serverName, titleFormat, footerFormat, listThumbnailUrl, noPlayersTitle, requireAdminRole, logsCommandRequiresAdminRole, this.plugin);
            try {
                this.discordBot.start();
                getLogger().warning("[Discord Bot] Starting Discord Bot...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            getLogger().warning("[Discord Bot] Bot is disabled. Skipping initialization...");
        }
        CommandLogViewer commandLogViewer = new CommandLogViewer(getDataFolder().getPath() + File.separator + "logs", 15);
        getCommand("viewlogs").setExecutor(commandLogViewer);
        Bukkit.getServer().getPluginManager().registerEvents(this.fileCommandLogger, this);

        //
        // New Config - Finished Classes
        //

        // Chat Webhook (Webhook)
        if (!config.getBoolean("chatwebhook.enabled")) {
            getLogger().warning("Chat Logger - [Not Enabled]");
        } else {
            this.chatWebhook = new ChatWebhook(config);
            getServer().getPluginManager().registerEvents(chatWebhook, this);
            getLogger().warning("Chat Logger - [Enabled]");
        }

        // Start/Stop Logger (Discord Bot Feature)
        if (!config.getBoolean("serverstatus.enabled")) {
            getLogger().warning("Start/Stop Logger - [Not Enabled]");
        } else {
            StartStopLogger startStopLogger = new StartStopLogger(discordBot, config);
            startStopLogger.sendStatusUpdateMessage(true);
            getLogger().warning("Start/Stop Logger - [Enabled]");
        }

        // Factions/Skyblock Top Announcer (Webhook)
        if (!config.getBoolean("announcer.enabled")) {
            getLogger().warning("Factions Top Announcer - [Not Enabled]");
        } else {
            this.factionsTopAnnouncer = new FactionsTopAnnouncer(config);
            Bukkit.getPluginManager().registerEvents(factionsTopAnnouncer, this);
            getLogger().warning("Factions Top Announcer - [Enabled]");
        }

        // Player Report Command (Webhook + Command)
        if (!config.getBoolean("report.enabled")) {
            getLogger().warning("Player Reporting Command - [Not Enabled]");
        } else {
            this.reportCommand = new ReportCommand(config);
            getCommand("report").setExecutor(this.reportCommand);
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
        if (!config.getBoolean("player_leave_join_logger.enabled")) {
            getLogger().warning("Player Join Leave Logger - [Not Enabled]");
        } else {
            this.joinLeaveLogger = new JoinLeaveLogger(config);
            Bukkit.getServer().getPluginManager().registerEvents(this.joinLeaveLogger, this);
            getLogger().warning("Player Join Leave Logger - [Enabled]");
        }
        // Suggestion Command (Discord Bot + Command)
        if (!config.getBoolean("bot.enabled")) {
            getLogger().warning("Suggestion Command - [Not Enabled] - (Requires Discord Bot enabled)");
        } else {
            this.suggestionCommand = new SuggestionCommand(this.discordBot, config);
            getCommand("suggestion").setExecutor(this.suggestionCommand);
            getServer().getPluginManager().registerEvents(this.suggestionCommand, this);
            getLogger().warning("Suggestion Command - [Enabled]");
        }

        this.ignoredCommands = getConfig().getStringList("ignored_commands");
        new ThreadPoolExecutor(5, 10, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        this.commandLogger = new CommandLogger(this.discordBot, config);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getConsoleSender().sendMessage("[KushStaffUtils] Plugin has been enabled");
    }

    public void onShutdown() {}

    public void onDisable() {
        FileConfiguration config = getConfig();
        boolean discordBotEnabled = config.getBoolean("bot.enabled");
        if (discordBotEnabled) {
            this.discordBot.stop();
            getLogger().warning("[Discord Bot] Bot has been disabled!");
        } else {
            getLogger().warning("[Discord Bot] Bot is disabled, won't stop.");
        }
        boolean factionsTopAnnouncer = config.getBoolean("announcer.enabled");
        if (factionsTopAnnouncer) {
            this.factionsTopAnnouncer.cancelAnnouncements();
        }
        if (config.getBoolean("serverstatus.enabled")) {
            StartStopLogger startStopLogger = new StartStopLogger(discordBot, config);
            startStopLogger.sendStatusUpdateMessage(false);
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
        boolean logCommands = getConfig().getBoolean("log_commands");
        this.fileCommandLogger.reloadLogCommands(logCommands);
        // Discord Bot Stuff
        discordBot.reloadBot();
        // Instance Reloads
        if (Main.getInstance().getConfig().getBoolean("strike.enabled")) {
            factionStrike.accessConfigs();
        }
        if (Main.getInstance().getConfig().getBoolean("bug_report.enabled")) {
            bugCommand.accessConfigs();
        }
        if (Main.getInstance().getConfig().getBoolean("report.enabled")) {
            reportCommand.accessConfigs();
        }
        if (Main.getInstance().getConfig().getBoolean("player_leave_join_logger.enabled")) {
            joinLeaveLogger.accessConfigs();
        }
        if (Main.getInstance().getConfig().getBoolean("bot.enabled")) {
            suggestionCommand.accessConfigs();
        }
        if (Main.getInstance().getConfig().getBoolean("bot.enabled")) {
            commandLogger.accessConfigs();
        }
        if (Main.getInstance().getConfig().getBoolean("announcer.enabled")) {
            factionsTopAnnouncer.reloadAnnouncer();
        }
        if (Main.getInstance().getConfig().getBoolean("chatwebhook.enabled")) {
            chatWebhook.accessConfigs();
        }
        Bukkit.getConsoleSender().sendMessage("[KushStaffUtils] Config options have been reloaded!");
    }

    public static String getCommandLoggerFolder() {
        return logsFolder;
    }

    public static Main getInstance() {
        return instance;
    }
}
