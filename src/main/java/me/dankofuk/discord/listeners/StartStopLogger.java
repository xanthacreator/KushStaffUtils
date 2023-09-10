package me.dankofuk.discord.listeners;

import me.dankofuk.Main;
import me.dankofuk.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.configuration.file.FileConfiguration;

import java.awt.*;

public class StartStopLogger extends ListenerAdapter {

    private DiscordBot discordBot;
    public Main main;

    public StartStopLogger(DiscordBot discordBot) {
        this.discordBot = discordBot;
    }

    public void accessConfigs() {
        String serverStatusChannelId = Main.getInstance().getConfig().getString("serverstatus.channelId");
        boolean enabled = Main.getInstance().getConfig().getBoolean("serverstatus.enabled");
    }

    public void sendStatusUpdateMessage(boolean serverStarted) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(serverStarted ? Color.GREEN : Color.RED);
            embed.setTitle(serverStarted ? "\uD83D\uDFE2 Server has `started`!" : "\uD83D\uDED1 Server has `shutdown`!");
            embed.setFooter(serverStarted ? "Server Log" : "Server Log");

            TextChannel channel = discordBot.getJda().getTextChannelById(Main.getInstance().getConfig().getString("serverstatus.channelId"));
            if (channel != null) {
                channel.sendMessageEmbeds(embed.build()).queue();
            }
        }
    }
