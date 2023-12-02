package me.dankofuk.discord.verify;

import me.dankofuk.KushStaffUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;

public class SendPanel extends ListenerAdapter {

    public String roleID;
    public KushStaffUtils instance;
    public JDA jda;

    public SendPanel(JDA jda, KushStaffUtils instance) {
        this.jda = jda;
        this.instance = instance;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        if (event.getName().equals("sendverifypanel")) {
            String channelId = event.getOption("channel").getAsString();
            MessageChannel channel = jda.getChannelById(MessageChannel.class, channelId);
            if (channel != null) {
                channel.sendMessage("Click the button to verify!").setActionRow(
                        Button.primary("verify_button", "Verify")
                ).queue();
                event.reply("Verification panel sent!").setEphemeral(true).queue();
            } else {
                event.reply("Invalid channel ID").setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().equals("verify_button")) {
            Guild guild = event.getGuild();
            if (guild != null) {
                Configuration config = KushStaffUtils.getInstance().getConfig();
                String roleId = config.getString("bot.verify_panel.giveRoleOnClicks");
                Role role = guild.getRoleById(roleId);
                if (role != null) {
                    guild.addRoleToMember(UserSnowflake.fromId(event.getUser().getId()), role).queue();
                    event.reply("You have been verified and assigned the role!").setEphemeral(true).queue();
                } else {
                    event.reply("Role not found.").setEphemeral(true).queue();
                }
            }
        }
    }
}