package me.dankofuk.discord.listeners;

import java.util.List;
import java.util.stream.Collectors;

import me.dankofuk.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;

public class ListPlayers extends ListenerAdapter {
    private final DiscordBot discordBot;
    private final String commandPrefix;

    public ListPlayers(DiscordBot discordBot, String commandPrefix) {
        this.discordBot = discordBot;
        this.commandPrefix = commandPrefix;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String message = event.getMessage().getContentRaw();

        if (message.equalsIgnoreCase(commandPrefix + "online")) {
            List<String> playerNames = discordBot.getMinecraftServer().getOnlinePlayers().stream()
                    .map(player -> player.getName())
                    .collect(Collectors.toList());

            if (playerNames.isEmpty()) {
                event.getChannel().sendMessage(">  No players are online.").queue();
                return;
            }
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Online Players");
            embed.setThumbnail(event.getGuild().getIconUrl());
            embed.setFooter("Total Online: " + Bukkit.getOnlinePlayers().size());
            embed.setDescription(String.join("\n", playerNames));

            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        }
    }
}
