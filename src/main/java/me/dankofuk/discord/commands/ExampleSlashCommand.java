package me.dankofuk.discord.commands;

import me.dankofuk.discord.DiscordBot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;


public class ExampleSlashCommand extends ListenerAdapter {
    public DiscordBot discordBot;


    public ExampleSlashCommand(DiscordBot discordBot) {
        this.discordBot = discordBot;
    }


    public DiscordBot getDiscordBot() {
        return discordBot;
    }


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        if (command.equalsIgnoreCase("example")) {
            String userTag = event.getUser().getName();
            event.reply("Welcome user! " + userTag + " Test").queue();
        }
    }
}
