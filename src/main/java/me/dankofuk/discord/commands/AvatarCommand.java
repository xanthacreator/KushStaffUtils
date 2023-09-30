package me.dankofuk.discord.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

public class AvatarCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("avatar")) {
            OptionMapping userOption = event.getOption("user");

            if (userOption == null || userOption.getType() != OptionType.USER) {
                event.reply("Please specify a valid user.").setEphemeral(true).queue();
                return;
            }

            String username = Objects.requireNonNull(userOption.getAsUser()).getAsTag();
            String avatarUrl = Objects.requireNonNull(userOption.getAsUser()).getEffectiveAvatarUrl();

            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.BLUE);
            embed.setTitle("Avatar for " + "`"+username+"`");
            embed.setImage(avatarUrl);
            embed.setFooter("Avatar Command -"+username+"'s " + "Avatar");

            event.replyEmbeds(embed.build()).queue();
        }
    }
}

