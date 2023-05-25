package me.dankofuk.discord;

import me.dankofuk.discord.listeners.DiscordLogger;
import me.dankofuk.discord.commands.ConsoleCommand;
import me.dankofuk.discord.commands.ReloadCommand;
import me.dankofuk.discord.listeners.DiscordChat2Game;
import me.dankofuk.discord.listeners.ListPlayers;
import me.dankofuk.discord.listeners.ServerStatus;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.awt.*;
import java.util.List;

public class DiscordBot extends ListenerAdapter {
    public String discordToken;
    public boolean discordBotEnabled;
    public Server minecraftServer;
    public String commandPrefix;
    public String discordActivity;
    public JDA jda;
    public String adminRoleID;
    public Plugin botTask;
    public FileConfiguration config;
    public String ServerStatusChannelID;
    public String logChannelId;
    private DiscordLogger discordLogger;
    public boolean logAsEmbed;
    private String serverName;
    private String titleFormat;
    private String footerFormat;
    private String listThumbnailUrl;

    public DiscordBot(String discordToken, boolean discordBotEnabled, Server minecraftServer, String commandPrefix, String adminRoleID, String discordActivity, Plugin botTask, FileConfiguration config, String ServerStatusChannelID, String logChannelId, boolean logAsEmbed, String serverName, String titleFormat, String footerFormat, String listThumbnailUrl) {
        this.discordToken = discordToken;
        this.discordBotEnabled = discordBotEnabled;
        this.minecraftServer = minecraftServer;
        this.commandPrefix = commandPrefix;
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
    }

    public JDA getJda() {
        return jda;
    }

    public void start() throws InterruptedException {

        if (!discordBotEnabled) {
            return;
        }

        // Creates the Discord Bot
        jda = JDABuilder.createDefault(discordToken)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(this)
                .setActivity(Activity.playing(discordActivity))
                .build()
                .awaitReady();

        // Register Discord Events
        // List Players
        String titleFormat = config.getString("bot.listplayers_title_format");
        String footerFormat = config.getString("bot.listplayers_footer_format");
        String listThumbnailUrl = config.getString("bot.listplayers_thumbnail_url");
        jda.addEventListener(new ListPlayers(this, commandPrefix, titleFormat, footerFormat, listThumbnailUrl));
        jda.addEventListener(new ServerStatus(this, ServerStatusChannelID));
        jda.addEventListener(new ConsoleCommand(this, commandPrefix, config, minecraftServer));
        // Discord Logger
        List<String> messageFormats = config.getStringList("bot.command_log_message_formats");
        List<String> embedTitleFormats = config.getStringList("bot.command_log_embed_title_formats");
        jda.addEventListener(new DiscordLogger(this, messageFormats, embedTitleFormats, serverName, logAsEmbed, logChannelId));
        // Discord2Chat
        boolean enabled = config.getBoolean("bot.discord_to_game_enabled");
        boolean roleIdRequired = config.getBoolean("bot.discord_to_game_roleIdRequired");
        String channelId = config.getString("bot.discord_to_game_channel_id");
        String format = config.getString("bot.discord_to_game_format");
        String roleId = config.getString("bot.discord_to_game_roleId");
        jda.addEventListener(new DiscordChat2Game(enabled, channelId, format, roleIdRequired, roleId));

        // Reload Command
        jda.addEventListener(new ReloadCommand(this, commandPrefix, config, logChannelId, logAsEmbed, titleFormat, footerFormat, listThumbnailUrl));
    }

    // Method for stopping the Discord Bot
    public void stop() {
        if (jda != null) {
            jda.shutdown();
            // Cancel any tasks registered by the bot
            Bukkit.getScheduler().getPendingTasks().stream()
                    .filter(task -> task.getOwner() == botTask)
                    .forEach(task -> task.cancel());
        }
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String message = event.getMessage().getContentRaw();
        // Help Command
        if (message.equalsIgnoreCase(commandPrefix + "help")) {
            EmbedBuilder helpEmbed = new EmbedBuilder();
            helpEmbed.setColor(Color.RED);
            helpEmbed.setTitle("__`Help Page 1/1`__");
            helpEmbed.setDescription(">  `Command List` \n  \n> `" + commandPrefix + "help` - Shows this menu \n  \n> `" + commandPrefix + "servercommand [command]` - Sends a command to the server! \n  \n> `" + commandPrefix + "online` - Shows the players online \n  \n> `" + commandPrefix + "reloadconfig` - Reloads the configs for the bot related stuff.");
            helpEmbed.setFooter("Help Page 1/1 - Made by DankOfUK/ChatGPT");
            event.getChannel().sendMessageEmbeds(helpEmbed.build()).queue();

            // Server Console Command
        }
    }

    // Reload Discord Elements
    public void reloadDiscordConfig(String discordToken, boolean discordBotEnabled, Server minecraftServer, String commandPrefix, String adminRoleID, String discordActivity, Plugin botTask, FileConfiguration config, String ServerStatusChannelID, String logChannelId, boolean logAsEmbed, String titleFormat, String footerFormat, String listThumbnailUrl) {
        this.discordToken = discordToken;
        this.discordBotEnabled = discordBotEnabled;
        this.minecraftServer = minecraftServer;
        this.commandPrefix = commandPrefix;
        this.adminRoleID = adminRoleID;
        this.discordActivity = discordActivity;
        this.botTask = botTask;
        this.config = config;
        this.ServerStatusChannelID = ServerStatusChannelID;
        this.logChannelId = logChannelId;
        this.logAsEmbed = logAsEmbed;
        this.titleFormat = titleFormat;
        this.footerFormat = footerFormat;
        this.listThumbnailUrl = listThumbnailUrl;
    }


    public Server getMinecraftServer() {
        return Bukkit.getServer();
    }

}
