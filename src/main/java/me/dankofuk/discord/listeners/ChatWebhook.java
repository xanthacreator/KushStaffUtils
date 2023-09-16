package me.dankofuk.discord.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import me.dankofuk.KushStaffUtils;
import me.dankofuk.discord.managers.UUIDFetcher;
import me.dankofuk.utils.WebhookUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Objects;
import java.util.UUID;

public class ChatWebhook implements Listener {
    private KushStaffUtils main;
    public FileConfiguration config;

    public ChatWebhook(FileConfiguration config) {
        this.config = config;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!KushStaffUtils.getInstance().getConfig().getBoolean("chatwebhook.enabled")) {
            return;
        }

        String playerName = event.getPlayer().getName();
        String message = event.getMessage();

        String webhookMessage = KushStaffUtils.getInstance().getConfig().getString("chatwebhook.message");

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Player player = event.getPlayer();
            playerName = PlaceholderAPI.setPlaceholders(player, playerName);
            message = PlaceholderAPI.setPlaceholders(player, message);
            webhookMessage = PlaceholderAPI.setPlaceholders(player, webhookMessage);
        }
        long time = System.currentTimeMillis() / 1000L;

        webhookMessage = webhookMessage
                .replace("%player%", playerName)
                .replace("%player_name%", playerName)
                .replace("%message%", message)
                .replace("%time%", "<t:" + time + ":R>");

        sendWebhook(webhookMessage, playerName);
    }

    private void sendWebhook(String webhookMessage, String playerName) {
        try {
            webhookMessage = ChatColor.stripColor(webhookMessage);

            WebhookUtils webhook = new WebhookUtils(Objects.requireNonNull(KushStaffUtils.getInstance().getConfig().getString("chatwebhook.webhookUrl")));

            webhook.setContent(webhookMessage);

            String username = KushStaffUtils.getInstance().getConfig().getString("chatwebhook.username");
            webhook.setUsername(username.replace("%player%", playerName).replace("%player_name%", playerName));

            String crafatarBaseUrl = "https://crafatar.com/avatars/";
            UUID playerUuid = UUIDFetcher.getUUID(playerName);
            String avatarUrl = crafatarBaseUrl + playerUuid + "?overlay=head";
            webhook.setAvatarUrl(avatarUrl);

            webhook.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
