package me.dankofuk.discord.commands;

import me.dankofuk.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConsoleCommand extends ListenerAdapter {

    private final DiscordBot discordBot;
    private String commandPrefix;
    private FileConfiguration config;
    public Plugin botTask;
    public String discordToken;
    private boolean discordBotEnabled;
    private Server minecraftServer;
    private JDA jda;
    private String adminRoleID;

    public DiscordBot getDiscordBot() {
        return discordBot;
    }

    public ConsoleCommand(DiscordBot discordBot, String commandPrefix, FileConfiguration config, Server minecraftServer) {
        this.discordBot = discordBot;
        this.commandPrefix = commandPrefix;
        this.config = config;
        this.minecraftServer = minecraftServer;
        this.botTask = discordBot.botTask;
        this.discordToken = discordBot.discordToken;
        this.discordBotEnabled = discordBot.discordBotEnabled;
        this.adminRoleID = discordBot.adminRoleID;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String message = event.getMessage().getContentRaw();

        if (message.toLowerCase().startsWith(commandPrefix.toLowerCase() + "servercommand ")) {
            String command = message.substring((commandPrefix + "servercommand ").length()).trim();

            // Check if user has permissions to execute the command
            List<Role> roles = Objects.requireNonNull(event.getMember()).getRoles();
            boolean hasPermission = false;
            for (Role role : roles) {
                if (role.getId().equals(adminRoleID)) { // Replace ROLE_ID_HERE with the ID of the role that can execute server commands
                    hasPermission = true;
                    break;
                }
            }

            if (!hasPermission) {
                EmbedBuilder noPerms = new EmbedBuilder();
                noPerms.setColor(Color.RED);
                noPerms.setTitle("Error #NotDankEnough");
                noPerms.setDescription(">  `You lack the required permissions for this command!`");
                noPerms.setFooter(OffsetDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
                event.getChannel().sendMessageEmbeds(noPerms.build()).queue();
                return;
            }

            if (command.isEmpty()) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("Server Command Error");
                embed.setDescription("Cannot find command `" + command + "` on the Minecraft server.");
                embed.setColor(Color.RED);
                TextChannel channel = event.getChannel().asTextChannel();
                channel.sendMessageEmbeds(embed.build())
                        .queue(sentMessage -> System.out.println("Error message sent"),
                                error -> System.err.println("Error sending error message: " + error.getMessage()));

            } else {
                // Execute the command on the Minecraft server
                Bukkit.getScheduler().runTask(botTask, () -> {
                    boolean success = minecraftServer.dispatchCommand(minecraftServer.getConsoleSender(), command);

                    EmbedBuilder embed = new EmbedBuilder();
                    if (success) {
                        embed.setTitle("Server Command Executed");
                        embed.setDescription("Command `" + command + "` executed successfully on the Minecraft server.");
                        embed.setColor(Color.GREEN);
                        TextChannel channel = event.getChannel().asTextChannel();

                        channel.sendMessageEmbeds(embed.build())
                                .queue(sentMessage -> System.out.println("Server command executed"),
                                        error -> System.err.println("Error executing server command: " + error.getMessage()));
                    } else {
                        embed.setTitle("Server Command Error");
                        if (command.startsWith("!")) {
                            embed.setDescription("Cannot find command. Type `" + commandPrefix + "help` for help.");
                        } else {
                            embed.setDescription("Error executing command `" + command + "` on the Minecraft server.");
                        }
                        embed.setColor(Color.RED);
                        TextChannel channel = event.getChannel().asTextChannel();
                        channel.sendMessageEmbeds(embed.build())
                                .queue(sentMessage -> System.out.println("Error message sent"),
                                        error -> System.err.println("Error sending error message: " + error.getMessage()));
                    }
                });
            }
        }
    }

    public Server getMinecraftServer() {
        return Bukkit.getServer();
    }
}

