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
import me.dankofuk.listeners.MiddleClick;
import me.dankofuk.utils.ColorUtils;
import net.dv8tion.jda.api.JDA;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main extends JavaPlugin implements Listener {
    private static String logsFolder;
    private static Main instance;
    private JDA jda;
    private Plugin plugin;

    public FileConfiguration config;
    public FileConfiguration messagesConfig;

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
    public MiddleClick creativeLogger;

    public void onEnable() {
        // Loading configuration
        FileConfiguration config = getConfig();
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        messagesConfig = loadMessagesConfig();
        setDefaultMessages();

        // Instance
        instance = this;

        // PAPI/Vault Checker
        PluginManager pluginManager = Bukkit.getPluginManager();
        Plugin placeholderAPI = pluginManager.getPlugin("PlaceholderAPI");
        if (placeholderAPI == null)
            getLogger().warning("PlaceholderAPI is not installed or enabled. Some placeholders may not work.");
        Plugin vault = pluginManager.getPlugin("Vault");
        if (vault == null)
            getLogger().warning("Vault is not installed or enabled. Some functionality may be limited.");

        // bStats
        int pluginId = 18185;
        Metrics metrics = new Metrics(this, pluginId);

        // Features
        if (config.getBoolean("bot.enabled")) {
            if ("false".equals(Main.getInstance().getConfig().getString("bot.discord_token")) || Main.getInstance().getConfig().getString("bot.discord_token").isEmpty()) {
                getLogger().warning("[Discord Bot] No bot token found. Bot initialization skipped.");
                return;
            }
            this.discordBot = new DiscordBot(this);
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

        //
        // New Config - Finished Classes
        //

        // FileCommandLogger (Logging Folder)
        if (!config.getBoolean("per-user-logging.enabled")) {
            getLogger().warning("Per User Logging - [Not Enabled]");
        } else {
            String logsFolder = (new File(getDataFolder(), "logs")).getPath();
            this.fileCommandLogger = new FileCommandLogger(logsFolder);
            getServer().getPluginManager().registerEvents(fileCommandLogger, this);
            getLogger().warning("Per User Logging - [Enabled]");
        }
        // Chat Webhook (Webhook)
        if (!config.getBoolean("chatwebhook.enabled")) {
            getLogger().warning("Chat Logger - [Not Enabled]");
        } else {
            this.chatWebhook = new ChatWebhook(config);
            getServer().getPluginManager().registerEvents(chatWebhook, this);
            getLogger().warning("Chat Logger - [Enabled]");
        }
        // Start/Stop Logger (Discord Bot Feature)
        if (!config.getBoolean("bot.enabled")) {
            } else if (!config.getBoolean("serverstatus.enabled")) {
                getLogger().warning("Start/Stop Logger - [Not Enabled]");
            } else {
            StartStopLogger startStopLogger = new StartStopLogger(discordBot);
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
        // Command Logger (Discord Feature)
        if (!config.getBoolean("bot.enabled")) {
            getLogger().warning("Command Logger - [Not Enabled] - (Requires Discord Bot enabled)");
        } else {
            this.commandLogger = new CommandLogger(this.discordBot);
            getServer().getPluginManager().registerEvents(this.commandLogger, this);
            getLogger().warning("Command Logger - [Enabled]");
        }

        // Creative Logging (Webhooks)
        if (!config.getBoolean("creative-logging.enabled")) {
            getLogger().warning("Creative Logging - [Not Enabled]");
        } else {
            this.creativeLogger = new MiddleClick();
            getServer().getPluginManager().registerEvents(this.creativeLogger, this);
            getLogger().warning("Creative Logging - [Enabled]");
        }

        new ThreadPoolExecutor(5, 10, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        Bukkit.getConsoleSender().sendMessage("[KushStaffUtils] Plugin has been enabled");
    }

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
        if (discordBotEnabled) {
            if (config.getBoolean("serverstatus.enabled")) {
                StartStopLogger startStopLogger = new StartStopLogger(discordBot);
                startStopLogger.sendStatusUpdateMessage(false);
            }
        }
        Bukkit.getConsoleSender().sendMessage("[KushStaffUtils] Plugin has been disabled!");
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("stafflogger")) {
            if (!sender.hasPermission("commandlogger.reload")) {
                sender.sendMessage(ColorUtils.translateColorCodes(messagesConfig.getString("noPermissionMessage")));
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                reloadConfigOptions();
                sender.sendMessage(ColorUtils.translateColorCodes(messagesConfig.getString("reloadMessage")));
                return true;
            }
            return false;
        }
        return false;
    }

    public void reloadConfigOptions() {
        reloadConfig();
        FileConfiguration config = getConfig();
        loadMessagesConfig();
        // Discord Bot Stuff
        if (Main.getInstance().getConfig().getBoolean("bot.enabled")) {
            discordBot.reloadBot();
        }
        // Instance Reloads
        instance = this;

        // FileCommandLogger (Logging Folder)
        if (!config.getBoolean("per-user-logging.enabled")) {
            getLogger().warning("Per User Logging - [Not Enabled]");
        } else {
            getLogger().warning("Per User Logging - [Enabled]");
        }
        // Chat Webhook (Webhook)
        if (!config.getBoolean("chatwebhook.enabled")) {
            getLogger().warning("Chat Logger - [Not Enabled]");
        } else {
            getLogger().warning("Chat Logger - [Enabled]");
        }
        // Start/Stop Logger (Discord Bot Feature)
        if (!config.getBoolean("serverstatus.enabled")) {
            getLogger().warning("Start/Stop Logger - [Not Enabled]");
        } else {
            getLogger().warning("Start/Stop Logger - [Enabled]");
        }
        // Factions/Skyblock Top Announcer (Webhook)
        if (!config.getBoolean("announcer.enabled")) {
            getLogger().warning("Factions Top Announcer - [Not Enabled]");
        } else {
            getLogger().warning("Factions Top Announcer - [Enabled]");
        }
        // Player Report Command (Webhook + Command)
        if (!config.getBoolean("report.enabled")) {
            getLogger().warning("Player Reporting Command - [Not Enabled]");
        } else {
            getLogger().warning("Player Reporting Command - [Enabled]");
        }
        // Strike Command (Webhook + Command)
        if (!config.getBoolean("strike.enabled")) {
            getLogger().warning("Strike Command - [Not Enabled]");
        } else {
            getLogger().warning("Strike Command - [Enabled]");
        }
        // Bug Report Command (Webhook + Command)
        if (!config.getBoolean("bug_report.enabled")) {
            getLogger().warning("Bug Command - [Not Enabled]");
        } else {
            getLogger().warning("Bug Command - [Enabled]");
        }
        // Join Leave Logger (Webhooks)
        if (!config.getBoolean("player_leave_join_logger.enabled")) {
            getLogger().warning("Player Join Leave Logger - [Not Enabled]");
        } else {
            getLogger().warning("Player Join Leave Logger - [Enabled]");
        }
        // Suggestion Command (Discord Bot + Command)
        if (!config.getBoolean("bot.enabled")) {
            getLogger().warning("Suggestion Command - [Not Enabled] - (Requires Discord Bot enabled)");
        } else {
            getLogger().warning("Suggestion Command - [Enabled]");
        }
        // Command Logger (Discord Feature)
        if (!config.getBoolean("bot.enabled")) {
            getLogger().warning("Command Logger - [Not Enabled] - (Requires Discord Bot enabled)");
        } else {
            getLogger().warning("Command Logger - [Enabled]");
        }
        // Creative Logging (Webhooks)
        if (!config.getBoolean("creative-logging.enabled")) {
            getLogger().warning("Creative Logging - [Not Enabled]");
        } else {
            getLogger().warning("Creative Logging - [Enabled]");
        }
        Bukkit.getConsoleSender().sendMessage("[KushStaffUtils] Config options have been reloaded!");
    }

    private FileConfiguration loadMessagesConfig() {
        saveResource("messages.yml", false);

        return YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
    }

    private void setDefaultMessages() {
        messagesConfig.addDefault("reloadMessage", "&c[&c&ᴋᴜѕʜѕᴛᴀꜰꜰᴜᴛɪʟѕ&c] &8» &dThe config files have been reloaded!");
        messagesConfig.addDefault("noPermissionMessage", "&c[&c&ᴋᴜѕʜѕᴛᴀꜰꜰᴜᴛɪʟѕ&c] &8» &cYou do not have permission to &d/stafflogger&f!");

        messagesConfig.options().copyDefaults(true);
        saveMessagesConfig();
    }

    private void saveMessagesConfig() {
        try {
            messagesConfig.save(new File(getDataFolder(), "messages.yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCommandLoggerFolder() {
        return logsFolder;
    }

    public static Main getInstance() {
        return instance;
    }
}
