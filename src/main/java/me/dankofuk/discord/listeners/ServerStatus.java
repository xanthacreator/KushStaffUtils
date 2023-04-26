package me.dankofuk.discord.listeners;

import me.dankofuk.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class ServerStatus extends ListenerAdapter {

    private final DiscordBot discordBot;
    private final String ServerStatusChannelID;

    public ServerStatus(DiscordBot discordBot, String ServerStatusChannelID) {
        this.discordBot = discordBot;
        this.ServerStatusChannelID = ServerStatusChannelID;
    }


    public void sendStatusUpdateMessage(boolean serverStarted) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(serverStarted ? Color.GREEN : Color.RED);
        embed.setTitle(serverStarted ? "Server Started" : "Server Shutdown");
        embed.setFooter(serverStarted ? "Server Log" : "Server Log");

        TextChannel channel = discordBot.getJda().getTextChannelById(ServerStatusChannelID);
        if (channel != null) {
            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }
}
