package me.dankofuk.discord.commands;

import me.dankofuk.discord.DiscordBot;
import me.dankofuk.discord.managers.UUIDFetcher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.util.UUID;

public class LogsCommand extends ListenerAdapter {

    private DiscordBot discordBot;
    private boolean requireAdminRole;
    private Plugin plugin;

    public LogsCommand(DiscordBot discordBot, boolean requireAdminRole) {
        this.discordBot = discordBot;
        this.requireAdminRole = requireAdminRole;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("logs")) {
            if (!requireAdminRole || (event.getMember() != null && event.getMember().getRoles().stream()
                    .anyMatch(role -> role.getId().equals(discordBot.getAdminRoleID())))) {
                FileConfiguration config = plugin.getConfig();
                boolean logCommands = config.getBoolean("per-user-logging.enabled");
                if (logCommands) {
                    String username = event.getOption("user").getAsString();
                    try {
                        // Use your UUIDFetcher to get the Minecraft UUID from the username
                        UUID minecraftUUID = UUIDFetcher.getUUID(username);
                        if (minecraftUUID != null) {
                            // Convert UUID to a string without dashes for file name
                            String fileName = minecraftUUID + ".txt";

                            // Construct the file path
                            String filePath = "plugins/KushStaffUtils/logs/" + fileName;

                            // Check if the log file exists
                            File logFile = new File(filePath);
                            if (logFile.exists()) {
                                // Create a FileUpload from the log file
                                FileUpload file = FileUpload.fromData(logFile);
                                event.reply("Log file for " + username).addFiles(file).queue();
                            } else {
                                event.reply("Log file not found for the specified user.").queue();
                            }
                        } else {
                            event.reply("Could not find Minecraft UUID for the specified username.").queue();
                        }
                    } catch (Exception e) {
                        event.reply("An error occurred while fetching or sending the log file.").queue();
                        e.printStackTrace();
                    }
                } else {
                    EmbedBuilder noPerms = new EmbedBuilder();
                    noPerms.setColor(Color.RED);
                    noPerms.setTitle("Error: `Logs Feature` is not enabled.");
                    noPerms.setDescription(">  `Enable the feature in the config for this to work.");
                    event.replyEmbeds(noPerms.build()).queue();
                }
            } else {
                EmbedBuilder noPerms = new EmbedBuilder();
                noPerms.setColor(Color.RED);
                noPerms.setTitle("Error #NotDankEnough");
                noPerms.setDescription(">  `You lack the required permissions for this command!`");
                event.replyEmbeds(noPerms.build()).queue();
            }
        }
    }
}
