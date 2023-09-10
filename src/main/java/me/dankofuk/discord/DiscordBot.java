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
    public JDA jda;
    public Plugin botTask;
    public Main main;


    public DiscordBot(Plugin botTask) {
        this.botTask = botTask;
    }

    public JDA getJda() {
        return this.jda;
    }

    public void start() throws InterruptedException {
        if (!Main.getInstance().getConfig().getBoolean("bot.enabled"))
            return;
        this.jda = JDABuilder.createDefault(Main.getInstance().getConfig().getString("bot.discord_token")).enableIntents(GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                GatewayIntent.MESSAGE_CONTENT).addEventListeners(this).setActivity(Activity.playing(Main.getInstance().getConfig().getString("bot.discord_activity"))).build().awaitReady();

        // Register Events/Listeners
        this.jda.addEventListener(new OnlinePlayersCommand(this));
        this.jda.addEventListener(new StartStopLogger(this));
        this.jda.addEventListener(new ConsoleCommand(this));
        this.jda.addEventListener(new HelpCommand(this));
        this.jda.addEventListener(new LogsCommand(this));
        this.jda.addEventListener(new CommandLogger(this));
        this.jda.addEventListener(new DiscordChat2Game());
        this.jda.addEventListener(new ReloadCommand(this));
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
        String discordToken = Main.getInstance().getConfig().getString("bot.discord_token");
        boolean discordBotEnabled = Main.getInstance().getConfig().getBoolean("bot.enabled");
        String discordActivity = Main.getInstance().getConfig().getString("bot.discord_activity");
        String adminRoleId = Main.getInstance().getConfig().getString("bot.adminRoleID");
        // Discord Chat 2 Game Chat
        boolean enabled = Main.getInstance().getConfig().getBoolean("discord2game.enabled");
        String channelId = Main.getInstance().getConfig().getString("discord2game.channelId");
        String roleId = Main.getInstance().getConfig().getString("discord2game.roleId");
        boolean roleIdRequired = Main.getInstance().getConfig().getBoolean("discord2game.roleIdRequired");
        String format = Main.getInstance().getConfig().getString("discord2game.message");
        // Start/Stop Logger
        String ServerStatusChannelID = Main.getInstance().getConfig().getString("serverstatus.channelId");
        // Command Logger
        String serverName = Main.getInstance().getConfig().getString("commandlogger.server_name");
        List<String> messageFormats = Main.getInstance().getConfig().getStringList("commandlogger.message_formats");
        List<String> embedTitleFormats = Main.getInstance().getConfig().getStringList("commandlogger.embed_title_formats");
        List<String> ignoredCommands = Main.getInstance().getConfig().getStringList("commandlogger.ignored_commands");
        List<String> whitelistedCommands = Main.getInstance().getConfig().getStringList("commandlogger.whitelisted_commands");
        boolean logAsEmbed = Main.getInstance().getConfig().getBoolean("commandlogger.logAsEmbed");
        boolean whitelistEnabled = Main.getInstance().getConfig().getBoolean("commandlogger.whitelist_enabled");
        String logChannelId = Main.getInstance().getConfig().getString("commandlogger.channel_id");
        // Online Players
        String titleFormat = Main.getInstance().getConfig().getString("online_players.title");
        String footerFormat = Main.getInstance().getConfig().getString("online_players.footer");
        String listThumbnailUrl = Main.getInstance().getConfig().getString("online_players.thumbnailUrl");
        String noPlayersTitle = Main.getInstance().getConfig().getString("online_players.noPlayersTitle");
        boolean requireAdminRole = Main.getInstance().getConfig().getBoolean("online_players.requireAdminRole");

    }

    public void reloadBot() {
        stop();
        accessConfigs();
        if (Main.getInstance().getConfig().getBoolean("bot.enabled")) {
            if ("false".equals(Main.getInstance().getConfig().getString("bot.discord_token")) || Main.getInstance().getConfig().getString("bot.discord_token").isEmpty()) {
                Bukkit.getLogger().warning("[Discord Bot] No bot token found. Bot initialization skipped.");
                return;
            }
            try {
                start();
                Bukkit.getLogger().warning("[Discord Bot] Starting Discord Bot...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Bukkit.getLogger().warning("[Discord Bot] Bot is disabled. Skipping initialization...");
        }
    }

    public Server getMinecraftServer() {
        return Bukkit.getServer();
    }

    public String getAdminRoleID() {
        String adminRoleId = Main.getInstance().getConfig().getString("bot.adminRoleID");
        return adminRoleId;
    }
}