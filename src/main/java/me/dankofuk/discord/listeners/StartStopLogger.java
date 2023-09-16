package me.dankofuk.discord.listeners;

import me.dankofuk.KushStaffUtils;
import me.dankofuk.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class StartStopLogger extends ListenerAdapter {

    private final DiscordBot discordBot;
    public KushStaffUtils main;

    public StartStopLogger(DiscordBot discordBot) {
        this.discordBot = discordBot;
    }

    public void accessConfigs() {
        String serverStatusChannelId = KushStaffUtils.getInstance().getConfig().getString("serverstatus.channelId");
        boolean enabled = KushStaffUtils.getInstance().getConfig().getBoolean("serverstatus.enabled");
    }

    public void sendStatusUpdateMessage(boolean serverStarted) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(serverStarted ? Color.GREEN : Color.RED);
            embed.setTitle(serverStarted ? "\uD83D\uDFE2 Server has `started`!" : "\uD83D\uDED1 Server has `shutdown`!");
            embed.setFooter("Server Log");

            TextChannel channel = discordBot.getJda().getTextChannelById(KushStaffUtils.getInstance().getConfig().getString("serverstatus.channelId"));
            if (channel != null) {
                channel.sendMessageEmbeds(embed.build()).queue();
            }
        }
    }
