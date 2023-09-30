package me.dankofuk.discord.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

public class ServerInfoCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("serverinfo")) {
            Guild guild = event.getGuild();

            if (guild == null) {
                event.reply("This command can only be used in a server (guild).").setEphemeral(true).queue();
                return;
            }

            // Gather server information
            String ownerName = "N/A"; // Default value in case owner information is not available

            if (guild.getOwner() != null) {
                guild.getOwner().getUser();
                ownerName = guild.getOwner().getUser().getAsTag();
            }
            int boostLevel = guild.getBoostTier().getKey();
            int boostCount = guild.getBoostCount();
            int memberCount = guild.getMemberCount();
            int botCount = (int) guild.getMembers().stream()
                    .filter(member -> member.getUser().isBot())
                    .count();
            String vanityCode = guild.getVanityUrl();

            // Create an embed to display the server information
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.GREEN);
            embed.setTitle(guild.getName() + " Server Information");
            embed.addField("Owner", ownerName, true);
            embed.addField("Boost Level", "Level " + boostLevel, true);
            embed.addField("Boost Count", String.valueOf(boostCount), true);
            embed.addField("Member Count", String.valueOf(memberCount), true);
            embed.addField("Bot Count", String.valueOf(botCount), true);

            if (vanityCode != null) {
                embed.addField("Vanity Link", "https://discord.gg/" + vanityCode, true);
            }

            embed.setFooter("Server Info Command");

            event.replyEmbeds(embed.build()).queue();
        }
    }
}


