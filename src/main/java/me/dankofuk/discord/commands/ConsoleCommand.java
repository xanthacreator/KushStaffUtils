package me.dankofuk.discord.commands;

import me.dankofuk.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class ConsoleCommand extends ListenerAdapter {

    private final DiscordBot discordBot;

    public ConsoleCommand(DiscordBot discordBot) {
        this.discordBot = discordBot;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("command")) {
                String subCommand = event.getSubcommandName();
                String inGameCommand = event.getOption("command").getAsString();

                // Check if user has permissions to execute the command
                boolean hasPermission = event.getMember().getRoles().stream()
                        .anyMatch(role -> role.getId().equals(discordBot.getAdminRoleID()));

                if (!hasPermission) {
                    EmbedBuilder noPerms = new EmbedBuilder();
                    noPerms.setColor(Color.RED);
                    noPerms.setTitle("Error #NotDankEnough");
                    noPerms.setDescription(">  `You lack the required permissions for this command!`");
                    noPerms.setFooter(OffsetDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
                    event.replyEmbeds(noPerms.build()).queue();
                    return;
                }

                Bukkit.getScheduler().runTask(discordBot.botTask, () -> {
                    boolean success = discordBot.getMinecraftServer().dispatchCommand(discordBot.getMinecraftServer().getConsoleSender(), inGameCommand);

                    EmbedBuilder embed = new EmbedBuilder();
                    if (success) {
                        embed.setTitle("Server Command Executed");
                        embed.setDescription("Command `" + inGameCommand + "` executed successfully on the Minecraft server.");
                        embed.setColor(Color.GREEN);
                    } else {
                        embed.setTitle("Server Command Error");
                        embed.setDescription("Error executing command `" + inGameCommand + "` on the Minecraft server.");
                        embed.setColor(Color.RED);
                    }

                    event.replyEmbeds(embed.build()).queue();
                });
        }
    }
}
