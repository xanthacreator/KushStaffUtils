package me.dankofuk.discord.commands;

import me.dankofuk.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class ReloadCommand extends ListenerAdapter {
    private static DiscordBot discordBot;
    private static Plugin botTask;
    public FileConfiguration config;

    public DiscordBot getDiscordBot() {
        return discordBot;
    }

    public ReloadCommand (FileConfiguration config) {
        this.config = config;
    }

    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("reload"))
            if (event.getMember() != null && event.getMember().getRoles().stream()
                    .anyMatch(role -> role.getId().equals(discordBot.getAdminRoleID()))) {
                try {
                    this.config.load(new File("plugins/KushStaffUtils/config.yml"));
                } catch (IOException|org.bukkit.configuration.InvalidConfigurationException e) {
                    e.printStackTrace();
                }
                Server minecraftServer = Bukkit.getServer();
                Bukkit.getScheduler().getPendingTasks().stream()
                        .filter(task -> (task.getOwner() == botTask))
                        .forEach(task -> task.cancel());
                discordBot.accessConfigs();
                discordBot.stop();
                EmbedBuilder stoppedEmbed = new EmbedBuilder();
                stoppedEmbed.setColor(Color.RED);
                stoppedEmbed.setTitle("Bot stopped!");
                stoppedEmbed.setDescription(">  `Reloading configuration....`");
                stoppedEmbed.setFooter(OffsetDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
                event.replyEmbeds(stoppedEmbed.build(), new net.dv8tion.jda.api.entities.MessageEmbed[0]).queue();
                try {
                    discordBot.start();
                    System.out.println("[KushStaffUtils - Discord Bot] Reloading Discord Bot...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                EmbedBuilder startedEmbed = new EmbedBuilder();
                startedEmbed.setColor(Color.GREEN);
                startedEmbed.setTitle("Bot started!");
                startedEmbed.setDescription(">  `Reload Complete!`");
                startedEmbed.setFooter(OffsetDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
                event.replyEmbeds(startedEmbed.build(), new net.dv8tion.jda.api.entities.MessageEmbed[0]).queue();
            } else {
                EmbedBuilder noPerms = new EmbedBuilder();
                noPerms.setColor(Color.RED);
                noPerms.setTitle("Error #NotDankEnough");
                noPerms.setDescription(">  `You lack the required permissions for this command!`");
                event.replyEmbeds(noPerms.build(), new net.dv8tion.jda.api.entities.MessageEmbed[0]).queue();
            }
    }
}
