package me.dankofuk.discord.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class DiscordChat2Game extends ListenerAdapter {

    private boolean enabled;
    private String channelId;
    private String format;
    private boolean roleIdRequired;
    private String roleId;

    public DiscordChat2Game(boolean enabled, String channelId, String format, boolean roleIdRequired, String roleId) {
        this.enabled = enabled;
        this.channelId = channelId;
        this.format = format;
        this.roleIdRequired = roleIdRequired;
        this.roleId = roleId;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Check if the feature is enabled
        if (!enabled) {
            return;
        }

        // Check if the message was sent in the specified channel
        if (!event.getChannel().getId().equals(channelId)) {
            return;
        }

        // Check if the message was sent by a user and not a bot
        if (!event.getAuthor().isBot()) {
            String messageContent = event.getMessage().getContentDisplay();
            // Format the message using the configured format
            String formattedMessage = format.replace("%author%", event.getAuthor().getName())
                    .replace("%message%", messageContent);
            // Translate color codes in the formatted message
            formattedMessage = ChatColor.translateAlternateColorCodes('&', formattedMessage);

            // Check if the role ID is required and the user has the required role
            if (roleIdRequired && (roleId == null || roleId.isEmpty() || !userHasRequiredRole(event))) {
                sendNoPermissionEmbed(event);
                return;
            }

            // Broadcast the formatted message to all online players
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(formattedMessage);
            }
        }
    }

    private boolean userHasRequiredRole(MessageReceivedEvent event) {
        String userId = event.getAuthor().getId();
        return event.getGuild().getMemberById(userId)
                .getRoles().stream()
                .anyMatch(role -> role.getId().equals(roleId));
    }

    private void sendNoPermissionEmbed(MessageReceivedEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.RED);
        embedBuilder.setColor(Color.RED);
        embedBuilder.setTitle("Error #NotDankEnough");
        embedBuilder.setDescription(">  `You lack the required permissions to type in game chat from discord!`");

        event.getMessage().replyEmbeds(embedBuilder.build()).queue(reply -> {
            // Delete the reply after a certain duration (e.g., 10 seconds)
            reply.delete().queueAfter(6, TimeUnit.SECONDS);
        });
    }
}
