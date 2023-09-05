package me.dankofuk.discord;

import me.dankofuk.Main;
import me.dankofuk.discord.commands.*;
import me.dankofuk.discord.listeners.CommandLogger;
import me.dankofuk.discord.listeners.DiscordChat2Game;
import me.dankofuk.discord.listeners.StartStopLogger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DiscordBot extends ListenerAdapter {
    public String discordToken;
    public boolean discordBotEnabled;
    public Server minecraftServer;
    public String discordActivity;
    public JDA jda;
    public String adminRoleID;
    public Plugin botTask;
    public FileConfiguration config;
    public String ServerStatusChannelID;
    public String logChannelId;
    public CommandLogger commandLogger;
    public boolean logAsEmbed;
    public String titleFormat;
    public String footerFormat;
    public String listThumbnailUrl;
    public Plugin plugin;
    public StartStopLogger serverStatus;
    public String noPlayersTitle;
    public boolean requireAdminRole;
    public boolean logsCommandRequiresAdminRole;
    public List<String> ignoredCommands;
    private List<String> whitelistCommands;
    private boolean whitelistMode;
    private final String serverName;
    private List<String> messageFormats;
    private List<String> embedTitleFormats;
    public Main main;


    public DiscordBot(String discordToken, boolean discordBotEnabled, Server minecraftServer, String adminRoleID, String discordActivity, Plugin botTask, FileConfiguration config, String ServerStatusChannelID, String logChannelId, boolean logAsEmbed, String serverName, String titleFormat, String footerFormat, String listThumbnailUrl, String noPlayersTitle, boolean requireAdminRole, boolean logsCommandRequiresAdminRole, Plugin plugin) {
        this.discordToken = discordToken;
        this.discordBotEnabled = discordBotEnabled;
        this.minecraftServer = minecraftServer;
        this.adminRoleID = adminRoleID;
        this.discordActivity = discordActivity;
        this.botTask = botTask;
        this.config = config;
        this.ServerStatusChannelID = ServerStatusChannelID;
        this.logChannelId = logChannelId;
        this.logAsEmbed = logAsEmbed;
        this.serverName = serverName;
        this.titleFormat = titleFormat;
        this.footerFormat = footerFormat;
        this.listThumbnailUrl = listThumbnailUrl;
        this.noPlayersTitle = noPlayersTitle;
        this.requireAdminRole = requireAdminRole;
        this.logsCommandRequiresAdminRole = logsCommandRequiresAdminRole;
        this.plugin = plugin;
    }

    public JDA getJda() {
        return this.jda;
    }

    public void start() throws InterruptedException {
        if (!this.discordBotEnabled)
            return;
        this

                .jda = JDABuilder.createDefault(this.discordToken).enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.MESSAGE_CONTENT).addEventListeners(this).setActivity(Activity.playing(this.discordActivity)).build().awaitReady();
        boolean enabled = Main.getInstance().getConfig().getBoolean("bot.discord_to_game_enabled");
        String channelId = Main.getInstance().getConfig().getString("bot.discord_to_game_channel_id");
        String roleId = Main.getInstance().getConfig().getString("bot.discord_to_game_roleId");
        boolean roleIdRequired = Main.getInstance().getConfig().getBoolean("bot.discord_to_game_roleIdRequired");
        String format = Main.getInstance().getConfig().getString("bot.discord_to_game_format");
        String discordToken = Main.getInstance().getConfig().getString("bot.discord_token");
        boolean discordBotEnabled = Main.getInstance().getConfig().getBoolean("bot.enabled");
        String discordActivity = Main.getInstance().getConfig().getString("bot.discord_activity");
        String ServerStatusChannelID = Main.getInstance().getConfig().getString("serverstatus.channel_id");
        String logChannelId = Main.getInstance().getConfig().getString("bot.command_log_channel_id");
        boolean logAsEmbed = Main.getInstance().getConfig().getBoolean("bot.command_log_logAsEmbed");
        String titleFormat = Main.getInstance().getConfig().getString("bot.listplayers_title_format");
        String footerFormat = Main.getInstance().getConfig().getString("bot.listplayers_footer_format");
        String listThumbnailUrl = Main.getInstance().getConfig().getString("bot.listplayers_thumbnail_url");
        String noPlayersTitle = Main.getInstance().getConfig().getString("bot.listplayers_no_players_online_title");
        boolean requireAdminRole = Main.getInstance().getConfig().getBoolean("bot.listplayers_requireAdminRole");
        boolean logsCommandRequiresAdminRole = Main.getInstance().getConfig().getBoolean("bot.logsCommand_requireAdminRole");
        boolean logCommands = Main.getInstance().getConfig().getBoolean("per-user-logging.enabled");
        this.jda.addEventListener(new OnlinePlayersCommand(this, noPlayersTitle, titleFormat, footerFormat, listThumbnailUrl, requireAdminRole));
        this.jda.addEventListener(new StartStopLogger(this, this.ServerStatusChannelID));
        this.jda.addEventListener(new ConsoleCommand(this));
        this.jda.addEventListener(new HelpCommand(this));
        this.jda.addEventListener(new LogsCommand(this, logsCommandRequiresAdminRole, logCommands, this.config));
        this.jda.addEventListener(new CommandLogger(this, messageFormats, embedTitleFormats, this.serverName, this.logAsEmbed, this.logChannelId));
        this.jda.addEventListener(new DiscordChat2Game(enabled, channelId, format, roleIdRequired, roleId));
        this.jda.addEventListener(new ReloadCommand(config));
    }

    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandsData = new ArrayList<>();
        commandsData.add(Commands.slash("help", "Shows the list of all commands in this bot."));
        commandsData.add(Commands.slash("online", "Lists Online Players."));
        commandsData.add(Commands.slash("command", "Sends the command to the server.").addOption(OptionType.STRING, "command", "The command you want to send."));
        commandsData.add(Commands.slash("logs", "Gets the logs for the user you enter.").addOption(OptionType.STRING, "user", "The user you would like the logs for."));
        commandsData.add(Commands.slash("reload", "Reloads the bot configs. (only bot related)"));
        event.getJDA().updateCommands().addCommands(commandsData).queue();
    }

    public void onReady(@NotNull ReadyEvent event) {
        List<CommandData> commandsData = new ArrayList<>();
        commandsData.add(Commands.slash("help", "Shows the list of all commands in this bot."));
        commandsData.add(Commands.slash("online", "Lists Online Players."));
        commandsData.add(Commands.slash("command", "Sends the command to the server.").addOption(OptionType.STRING, "command", "The command you want to send."));
        commandsData.add(Commands.slash("logs", "Gets the logs for the user you enter.").addOption(OptionType.STRING, "user", "The user you would like the logs for."));
        commandsData.add(Commands.slash("reload", "Reloads the bot configs. (only bot related)"));
        event.getJDA().updateCommands().addCommands(commandsData).queue();
    }

    public void stop() {
        if (this.jda != null) {
            this.jda.shutdown();
            Bukkit.getScheduler().getPendingTasks().stream()
                    .filter(task -> (task.getOwner() == this.botTask))
                    .forEach(task -> task.cancel());
        }
    }

    public void accessConfigs() {
        boolean enabled = Main.getInstance().getConfig().getBoolean("bot.discord_to_game_enabled");
        String channelId = Main.getInstance().getConfig().getString("bot.discord_to_game_channel_id");
        String roleId = Main.getInstance().getConfig().getString("bot.discord_to_game_roleId");
        boolean roleIdRequired = Main.getInstance().getConfig().getBoolean("bot.discord_to_game_roleIdRequired");
        String format = Main.getInstance().getConfig().getString("bot.discord_to_game_format");
        String discordToken = Main.getInstance().getConfig().getString("bot.discord_token");
        boolean discordBotEnabled = Main.getInstance().getConfig().getBoolean("bot.enabled");
        String discordActivity = Main.getInstance().getConfig().getString("bot.discord_activity");
        String ServerStatusChannelID = Main.getInstance().getConfig().getString("serverstatus.channel_id");
        String logChannelId = Main.getInstance().getConfig().getString("bot.command_log_channel_id");
        boolean logAsEmbed = Main.getInstance().getConfig().getBoolean("bot.command_log_logAsEmbed");
        String titleFormat = Main.getInstance().getConfig().getString("bot.listplayers_title_format");
        String footerFormat = Main.getInstance().getConfig().getString("bot.listplayers_footer_format");
        String listThumbnailUrl = Main.getInstance().getConfig().getString("bot.listplayers_thumbnail_url");
        String noPlayersTitle = Main.getInstance().getConfig().getString("bot.listplayers_no_players_online_title");
        boolean requireAdminRole = Main.getInstance().getConfig().getBoolean("bot.listplayers_requireAdminRole");
        boolean logsCommandRequiresAdminRole = Main.getInstance().getConfig().getBoolean("bot.logsCommand_requireAdminRole");
    }

    public Server getMinecraftServer() {
        return Bukkit.getServer();
    }

    public String getAdminRoleID() {
        return this.adminRoleID;
    }
}