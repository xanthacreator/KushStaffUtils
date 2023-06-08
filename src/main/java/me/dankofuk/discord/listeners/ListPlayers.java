package me.dankofuk.discord.listeners;

import me.dankofuk.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.stream.Collectors;

public class ListPlayers extends ListenerAdapter {
    private final DiscordBot discordBot;
    private final String commandPrefix;
    private String titleFormat;
    private String footerFormat;
    private String listThumbnailUrl;

    public ListPlayers(DiscordBot discordBot, String commandPrefix, String titleFormat, String footerFormat, String listThumbnailUrl) {
        this.discordBot = discordBot;
        this.commandPrefix = commandPrefix;
        this.titleFormat = titleFormat;
        this.footerFormat = footerFormat;
        this.listThumbnailUrl = listThumbnailUrl;
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
            embed.setTitle(titleFormat.replace("%online%", String.valueOf(playerNames.size())));
            embed.setThumbnail(listThumbnailUrl);
            embed.setFooter(footerFormat.replace("%online%", String.valueOf(playerNames.size())));
            embed.setDescription(String.join("\n", playerNames));

            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        }
    }
}
