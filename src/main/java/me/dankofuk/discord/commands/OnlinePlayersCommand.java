package me.dankofuk.discord.commands;

import me.dankofuk.KushStaffUtils;
import me.dankofuk.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class OnlinePlayersCommand extends ListenerAdapter {
    private KushStaffUtils main;
    private DiscordBot discordBot;

    public OnlinePlayersCommand(DiscordBot discordBot) {
        this.discordBot = discordBot;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("online")) {
            if (!KushStaffUtils.getInstance().getConfig().getBoolean("online_players.requireAdminRole") || (event.getMember() != null && event.getMember().getRoles().stream()
                    .anyMatch(role -> role.getId().equals(discordBot.getAdminRoleID())))) {

                List<String> playerNames = discordBot.getMinecraftServer().getOnlinePlayers().stream()
                        .map(player -> player.getName())
                        .collect(Collectors.toList());

                if (playerNames.isEmpty()) {
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle(KushStaffUtils.getInstance().getConfig().getString("online_players.noPlayersTitle"));
                    embed.setDescription(":negative_squared_cross_mark: `There are no players online currently!`");
                    embed.setThumbnail(KushStaffUtils.getInstance().getConfig().getString("online_players.thumbnailUrl"));
                    event.replyEmbeds(embed.build()).queue();
                    return;
                }

                Instant currentTime = Instant.now();
                String unixTimestamp = String.valueOf(currentTime.getEpochSecond());

                String titleWithPlaceholders = KushStaffUtils.getInstance().getConfig().getString("online_players.title").replaceAll("%online%", String.valueOf(playerNames.size()))
                        .replaceAll("%time%", "<t:" + unixTimestamp + ":R>");

                String footerWithPlaceholders = KushStaffUtils.getInstance().getConfig().getString("online_players.footer").replaceAll("%online%", String.valueOf(playerNames.size()));

                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(titleWithPlaceholders);
                embed.setDescription(String.join("\n", playerNames));
                embed.setFooter(footerWithPlaceholders);
                embed.setThumbnail(KushStaffUtils.getInstance().getConfig().getString("online_players.thumbnailUrl"));
                event.replyEmbeds(embed.build()).queue();
            } else {
                event.reply(":no_entry_sign: You don't have permission to use this command.").queue();
            }
        }
    }
}
