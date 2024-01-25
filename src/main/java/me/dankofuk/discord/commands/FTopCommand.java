package me.dankofuk.discord.commands;

import me.clip.placeholderapi.PlaceholderAPI;
import me.dankofuk.KushStaffUtils;
import me.dankofuk.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

public class FTopCommand extends ListenerAdapter {
    public DiscordBot discordBot;

    public FTopCommand(DiscordBot discordBot) {
        this.discordBot = discordBot;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("ftop")) {
            String message = discordBot.config.getStringList("announcer.messages").stream()
                    .map(line -> {
                        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                            return PlaceholderAPI.setPlaceholders(null, line);
                        } else {
                            return line;
                        }
                    })
                    .collect(Collectors.joining("\n"));

            EmbedBuilder helpEmbed = new EmbedBuilder();
            helpEmbed.setColor(Color.RED);
            helpEmbed.setTitle(discordBot.config.getString("announcer.title"));
            helpEmbed.setDescription(message.replace("%time%", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date())));
            helpEmbed.setFooter(discordBot.config.getString("announcer.footer"));
            event.replyEmbeds(helpEmbed.build()).setEphemeral(true).queue();
        }
    }
}