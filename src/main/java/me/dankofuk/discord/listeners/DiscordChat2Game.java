package me.dankofuk.discord.listeners;

import me.dankofuk.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class DiscordChat2Game extends ListenerAdapter {

    private Main main;

    public DiscordChat2Game() {
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!Main.getInstance().getConfig().getBoolean("discord2game.enabled")) {
            return;
        }

        if (!event.getChannel().getId().equals(Main.getInstance().getConfig().getString("discord2game.channelId"))) {
            return;
        }

        if (!event.getAuthor().isBot()) {
            String messageContent = event.getMessage().getContentDisplay();
            String formattedMessage = Main.getInstance().getConfig().getString("discord2game.message").replace("%author%", event.getAuthor().getName())
                    .replace("%message%", messageContent);
            formattedMessage = ChatColor.translateAlternateColorCodes('&', formattedMessage);

            if (Main.getInstance().getConfig().getBoolean("discord2game.roleIdRequired") && (Main.getInstance().getConfig().getString("discord2game.roleId") == null || Main.getInstance().getConfig().getString("discord2game.roleId").isEmpty() || !userHasRequiredRole(event))) {
                sendNoPermissionEmbed(event);
                return;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(formattedMessage);
            }
        }
    }

    private boolean userHasRequiredRole(MessageReceivedEvent event) {
        String userId = event.getAuthor().getId();
        return event.getGuild().getMemberById(userId)
                .getRoles().stream()
                .anyMatch(role -> role.getId().equals(Main.getInstance().getConfig().getString("discord2game.roleId")));
    }

    private void sendNoPermissionEmbed(MessageReceivedEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.RED);
        embedBuilder.setColor(Color.RED);
        embedBuilder.setTitle("Error #NotDankEnough");
        embedBuilder.setDescription(">  `You lack the required permissions to type in game chat from discord!`");

        event.getMessage().replyEmbeds(embedBuilder.build()).queue(reply -> {
            reply.delete().queueAfter(6, TimeUnit.SECONDS);
        });
    }
}
