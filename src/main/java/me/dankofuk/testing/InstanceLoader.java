package me.dankofuk.testing;

import me.dankofuk.KushStaffUtils;
import me.dankofuk.commands.CommandLogViewer;
import me.dankofuk.discord.DiscordBot;
import me.dankofuk.discord.commands.botRequiredCommands.BugCommand;
import me.dankofuk.discord.commands.botRequiredCommands.ReportCommand;
import me.dankofuk.discord.commands.botRequiredCommands.SuggestionCommand;
import me.dankofuk.discord.listeners.ChatWebhook;
import me.dankofuk.discord.listeners.CommandLogger;
import me.dankofuk.discord.listeners.StartStopLogger;
import me.dankofuk.factions.FactionStrike;
import me.dankofuk.factions.FactionsTopAnnouncer;
import me.dankofuk.loggers.creative.CreativeDropLogger;
import me.dankofuk.loggers.creative.CreativeMiddleClickLogger;
import me.dankofuk.loggers.players.FileCommandLogger;
import me.dankofuk.loggers.players.JoinLeaveLogger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Objects;

public class InstanceLoader {

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
    public CreativeMiddleClickLogger creativeLogger;
    public CreativeDropLogger creativeDropLogger;

    public void onEnableClassLoader(KushStaffUtils instance, FileConfiguration config) {


        // Features
        //if (config.getBoolean("bot.enabled")) {
        //    if ("false".equals(KushStaffUtils.getInstance().getConfig().getString("bot.discord_token")) || Objects.requireNonNull(KushStaffUtils.getInstance().getConfig().getString("bot.discord_token")).isEmpty()) {
        //        instance.getLogger().warning("[Discord Bot] No bot token found. Bot initialization skipped.");
        //        return;
        //    }
        //    this.discordBot = new DiscordBot(instance, config);
        //    try {
        //        this.discordBot.start();
        //        instance.getLogger().warning("[Discord Bot] Starting Discord Bot...");
        //    } catch (InterruptedException e) {
        //        e.printStackTrace();
        //    }
        //} else {
        //    instance.getLogger().warning("[Discord Bot] Bot is disabled. Skipping initialization...");
        //}
        //CommandLogViewer commandLogViewer = new CommandLogViewer(instance.getDataFolder().getPath() + File.separator + "logs", 15);
        //Objects.requireNonNull(instance.getCommand("viewlogs")).setExecutor(commandLogViewer);


        // FileCommandLogger (Logging Folder)
        if (!config.getBoolean("per-user-logging.enabled")) {
            instance.getLogger().warning("Per User Logging - [Not Enabled]");
        } else {
            String logsFolder = (new File(instance.getDataFolder(), "logs")).getPath();
            this.fileCommandLogger = new FileCommandLogger(logsFolder);
            instance.getServer().getPluginManager().registerEvents(fileCommandLogger, instance);
            instance.getLogger().warning("Per User Logging - [Enabled]");
        }
        // Chat Webhook (Webhook)
        if (!config.getBoolean("chatwebhook.enabled")) {
            instance.getLogger().warning("Chat Logger - [Not Enabled]");
        } else {
            this.chatWebhook = new ChatWebhook(config);
            instance.getServer().getPluginManager().registerEvents(chatWebhook, instance);
            instance.getLogger().warning("Chat Logger - [Enabled]");
        }
        // Start/Stop Logger (Discord Bot Feature)
        if (!config.getBoolean("bot.enabled")) {
            instance.getLogger().warning("Start/Stop Logger - [Not Enabled] - (Requires Discord Bot enabled)");
        } else if (!config.getBoolean("serverstatus.enabled")) {
            instance.getLogger().warning("Start/Stop Logger - [Not Enabled] - (Requires Discord Bot enabled)");
        } else {
            StartStopLogger startStopLogger = new StartStopLogger(discordBot);
            startStopLogger.sendStatusUpdateMessage(true);
            instance.getLogger().warning("Start/Stop Logger - [Enabled]");
        }
        // Factions/Skyblock Top Announcer (Webhook)
        if (!config.getBoolean("announcer.enabled")) {
            instance.getLogger().warning("Factions Top Announcer - [Not Enabled]");
        } else {
            this.factionsTopAnnouncer = new FactionsTopAnnouncer(config, instance);
            Bukkit.getPluginManager().registerEvents(factionsTopAnnouncer, instance);
            instance.getLogger().warning("Factions Top Announcer - [Enabled]");
        }
        // Player Report Command (Webhook + Command)
        if (!config.getBoolean("bot.enabled")) {
            instance.getLogger().warning("Player Reporting Command - [Not Enabled] - (Requires Discord Bot enabled)");
        } else if (!config.getBoolean("report.enabled")) {
            instance.getLogger().warning("Player Reporting Command - [Not Enabled] - (Requires Discord Bot enabled)");
        } else {
            this.reportCommand = new ReportCommand(instance, discordBot);
            Objects.requireNonNull(instance.getCommand("report")).setExecutor(this.reportCommand);
            Bukkit.getPluginManager().registerEvents(reportCommand, instance);
            instance.getLogger().warning("Player Reporting Command - [Enabled]");
        }
        // Strike Command (Webhook + Command)
        if (!config.getBoolean("strike.enabled")) {
            instance.getLogger().warning("Strike Command - [Not Enabled]");
        } else {
            this.factionStrike = new FactionStrike(config, instance);
            Objects.requireNonNull(instance.getCommand("strike")).setExecutor(this.factionStrike);
            instance.getLogger().warning("Strike Command - [Enabled]");
        }
        // Bug Report Command (Webhook + Command)
        if (!config.getBoolean("bot.enabled")) {
            instance.getLogger().warning("Bug Report Command - [Not Enabled] - (Requires Discord Bot enabled)");
        } else if (!config.getBoolean("bug_report.enabled")) {
            instance.getLogger().warning("Bug Report Command - [Not Enabled]");
        } else {
            this.bugCommand = new BugCommand(instance, discordBot, config);
            instance.getServer().getPluginManager().registerEvents(this.bugCommand, instance);
            Objects.requireNonNull(instance.getCommand("bug")).setExecutor(this.bugCommand);
            instance.getLogger().warning("Bug Command - [Enabled]");
        }
        // Join Leave Logger (Webhooks)
        if (!config.getBoolean("player_leave_join_logger.enabled")) {
            instance.getLogger().warning("Player Join Leave Logger - [Not Enabled]");
        } else {
            this.joinLeaveLogger = new JoinLeaveLogger(config);
            Bukkit.getServer().getPluginManager().registerEvents(this.joinLeaveLogger, instance);
            instance.getLogger().warning("Player Join Leave Logger - [Enabled]");
        }
        // Suggestion Command (Discord Bot + Command)
        if (!config.getBoolean("bot.enabled")) {
            instance.getLogger().warning("Suggestion Command - [Not Enabled] - (Requires Discord Bot enabled)");
        } else if (!config.getBoolean("suggestion.enabled")) {
            instance.getLogger().warning("Suggestion Command - [Not Enabled]");
        } else {
            this.suggestionCommand = new SuggestionCommand(this.discordBot, config);
            Objects.requireNonNull(instance.getCommand("suggestion")).setExecutor(this.suggestionCommand);
            instance.getServer().getPluginManager().registerEvents(this.suggestionCommand, instance);
            instance.getLogger().warning("Suggestion Command - [Enabled]");
        }
        // Command Logger (Discord Feature)
        if (!config.getBoolean("bot.enabled")) {
            instance.getLogger().warning("Command Logger - [Not Enabled] - (Requires Discord Bot enabled)");
        } else {
            this.commandLogger = new CommandLogger(this.discordBot);
            instance.getServer().getPluginManager().registerEvents(this.commandLogger, instance);
            instance.getLogger().warning("Command Logger - [Enabled]");
        }

        // Creative Logging (Webhooks)
        if (!config.getBoolean("creative-logging.enabled")) {
            instance.getLogger().warning("Creative Logging - [Not Enabled]");
        } else {
            this.creativeLogger = new CreativeMiddleClickLogger(instance);
            this.creativeDropLogger = new CreativeDropLogger(instance);
            instance.getServer().getPluginManager().registerEvents(this.creativeLogger, instance);
            instance.getServer().getPluginManager().registerEvents(this.creativeDropLogger, instance);
            instance.getLogger().warning("Creative Logging - [Enabled]");
        }

    }

    public void onReloadClassLoader(KushStaffUtils instance, FileConfiguration config) {


        // Features
        if (config.getBoolean("bot.enabled")) {
            if ("false".equals(KushStaffUtils.getInstance().getConfig().getString("bot.discord_token")) || Objects.requireNonNull(KushStaffUtils.getInstance().getConfig().getString("bot.discord_token")).isEmpty()) {
                instance.getLogger().warning("[Discord Bot] No bot token found. Bot initialization skipped.");
                return;
            }
            this.discordBot = new DiscordBot(instance, config);
            try {
                this.discordBot.start();
                instance.getLogger().warning("[Discord Bot] Starting Discord Bot...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            instance.getLogger().warning("[Discord Bot] Bot is disabled. Skipping initialization...");
        }
        CommandLogViewer commandLogViewer = new CommandLogViewer(instance.getDataFolder().getPath() + File.separator + "logs", 15);
        Objects.requireNonNull(instance.getCommand("viewlogs")).setExecutor(commandLogViewer);


        // FileCommandLogger (Logging Folder)
        if (!config.getBoolean("per-user-logging.enabled")) {
            instance.getLogger().warning("Per User Logging - [Not Enabled]");
        } else {
            String logsFolder = (new File(instance.getDataFolder(), "logs")).getPath();
            this.fileCommandLogger = new FileCommandLogger(logsFolder);
            instance.getServer().getPluginManager().registerEvents(fileCommandLogger, instance);
            instance.getLogger().warning("Per User Logging - [Enabled]");
        }
        // Chat Webhook (Webhook)
        if (!config.getBoolean("chatwebhook.enabled")) {
            instance.getLogger().warning("Chat Logger - [Not Enabled]");
        } else {
            this.chatWebhook = new ChatWebhook(config);
            instance.getServer().getPluginManager().registerEvents(chatWebhook, instance);
            instance.getLogger().warning("Chat Logger - [Enabled]");
        }
        // Start/Stop Logger (Discord Bot Feature)
        if (!config.getBoolean("bot.enabled")) {
            instance.getLogger().warning("Start/Stop Logger - [Not Enabled] - (Requires Discord Bot enabled)");
        } else if (!config.getBoolean("serverstatus.enabled")) {
            instance.getLogger().warning("Start/Stop Logger - [Not Enabled] - (Requires Discord Bot enabled)");
        } else {
            StartStopLogger startStopLogger = new StartStopLogger(discordBot);
            startStopLogger.sendStatusUpdateMessage(true);
            instance.getLogger().warning("Start/Stop Logger - [Enabled]");
        }
        // Factions/Skyblock Top Announcer (Webhook)
        if (!config.getBoolean("announcer.enabled")) {
            instance.getLogger().warning("Factions Top Announcer - [Not Enabled]");
        } else {
            this.factionsTopAnnouncer = new FactionsTopAnnouncer(config, instance);
            Bukkit.getPluginManager().registerEvents(factionsTopAnnouncer, instance);
            instance.getLogger().warning("Factions Top Announcer - [Enabled]");
        }
        // Player Report Command (Webhook + Command)
        if (!config.getBoolean("bot.enabled")) {
            instance.getLogger().warning("Player Reporting Command - [Not Enabled] - (Requires Discord Bot enabled)");
        } else if (!config.getBoolean("report.enabled")) {
            instance.getLogger().warning("Player Reporting Command - [Not Enabled] - (Requires Discord Bot enabled)");
        } else {
            this.reportCommand = new ReportCommand(instance, discordBot);
            Objects.requireNonNull(instance.getCommand("report")).setExecutor(this.reportCommand);
            Bukkit.getPluginManager().registerEvents(reportCommand, instance);
            instance.getLogger().warning("Player Reporting Command - [Enabled]");
        }
        // Strike Command (Webhook + Command)
        if (!config.getBoolean("strike.enabled")) {
            instance.getLogger().warning("Strike Command - [Not Enabled]");
        } else {
            this.factionStrike = new FactionStrike(config, instance);
            Objects.requireNonNull(instance.getCommand("strike")).setExecutor(this.factionStrike);
            instance.getLogger().warning("Strike Command - [Enabled]");
        }
        // Bug Report Command (Webhook + Command)
        if (!config.getBoolean("bot.enabled")) {
            instance.getLogger().warning("Bug Report Command - [Not Enabled] - (Requires Discord Bot enabled)");
        } else if (!config.getBoolean("bug_report.enabled")) {
            instance.getLogger().warning("Bug Report Command - [Not Enabled]");
        } else {
            this.bugCommand = new BugCommand(instance, discordBot, config);
            instance.getServer().getPluginManager().registerEvents(this.bugCommand, instance);
            Objects.requireNonNull(instance.getCommand("bug")).setExecutor(this.bugCommand);
            instance.getLogger().warning("Bug Command - [Enabled]");
        }
        // Join Leave Logger (Webhooks)
        if (!config.getBoolean("player_leave_join_logger.enabled")) {
            instance.getLogger().warning("Player Join Leave Logger - [Not Enabled]");
        } else {
            this.joinLeaveLogger = new JoinLeaveLogger(config);
            Bukkit.getServer().getPluginManager().registerEvents(this.joinLeaveLogger, instance);
            instance.getLogger().warning("Player Join Leave Logger - [Enabled]");
        }
        // Suggestion Command (Discord Bot + Command)
        if (!config.getBoolean("bot.enabled")) {
            instance.getLogger().warning("Suggestion Command - [Not Enabled] - (Requires Discord Bot enabled)");
        } else if (!config.getBoolean("suggestion.enabled")) {
            instance.getLogger().warning("Suggestion Command - [Not Enabled]");
        } else {
            this.suggestionCommand = new SuggestionCommand(this.discordBot, config);
            Objects.requireNonNull(instance.getCommand("suggestion")).setExecutor(this.suggestionCommand);
            instance.getServer().getPluginManager().registerEvents(this.suggestionCommand, instance);
            instance.getLogger().warning("Suggestion Command - [Enabled]");
        }
        // Command Logger (Discord Feature)
        if (!config.getBoolean("bot.enabled")) {
            instance.getLogger().warning("Command Logger - [Not Enabled] - (Requires Discord Bot enabled)");
        } else {
            this.commandLogger = new CommandLogger(this.discordBot);
            instance.getServer().getPluginManager().registerEvents(this.commandLogger, instance);
            instance.getLogger().warning("Command Logger - [Enabled]");
        }

        // Creative Logging (Webhooks)
        if (!config.getBoolean("creative-logging.enabled")) {
            instance.getLogger().warning("Creative Logging - [Not Enabled]");
        } else {
            this.creativeLogger = new CreativeMiddleClickLogger(instance);
            this.creativeDropLogger = new CreativeDropLogger(instance);
            instance.getServer().getPluginManager().registerEvents(this.creativeLogger, instance);
            instance.getServer().getPluginManager().registerEvents(this.creativeDropLogger, instance);
            instance.getLogger().warning("Creative Logging - [Enabled]");
        }

    }
}