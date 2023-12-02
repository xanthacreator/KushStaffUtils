package me.dankofuk.loggers.litebans.listeners;

import litebans.api.Entry;
import litebans.api.Events;
import me.dankofuk.KushStaffUtils;
import me.dankofuk.utils.StringUtils;
import me.dankofuk.utils.WebhookUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.Listener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class LBWarnListener implements Listener {

    public KushStaffUtils instance;

    public LBWarnListener(KushStaffUtils instance) {
        this.instance = instance;
    }


    public void registerEvents() {
        Events.get().register(new Events.Listener() {
            @Override
            public void entryAdded(Entry entry) {
                if ("warn".equals(entry.getType()) || "temp-warn".equals(entry.getType())) {
                    sendBanInfoToDiscord(entry);
                }
            }

            @Override
            public void entryRemoved(Entry entry) {
            }
        });
    }

    private void sendBanInfoToDiscord(Entry warnEntry) {
        Configuration config = KushStaffUtils.getInstance().getConfig();
        long time = System.currentTimeMillis() / 1000L;

        String webhookUrl = StringUtils.format(config.getString("litebans.warn.webhookUrl"));
        String embedTitle = StringUtils.format(config.getString("litebans.warn.embedTitle"));
        String embedDescription = StringUtils.format(config.getString("litebans.warn.embedDescription").replace("%time%", "<t:" + time + ":R>"));
        String embedFooter = StringUtils.format(config.getString("litebans.warn.embedFooter").replace("%time%", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date())));
        String thumbnailUrl = StringUtils.format(config.getString("litebans.warn.embedThumbnail"));
        String avatarUrl = StringUtils.format(config.getString("litebans.warn.embedAvatar"));

        UUID warnedPlayerUUID = UUID.fromString(Objects.requireNonNull(warnEntry.getUuid()));
        OfflinePlayer warnedPlayer = Bukkit.getOfflinePlayer(warnedPlayerUUID);
        String warnedPlayerName = warnedPlayer.getName();
        String duration = warnEntry.getDurationString();
        String reason = warnEntry.getReason();
        String issuer = warnEntry.getExecutorName();

        WebhookUtils webhook = new WebhookUtils(webhookUrl);
        webhook.setAvatarUrl(avatarUrl);
        WebhookUtils.EmbedObject embed = new WebhookUtils.EmbedObject()
                .setTitle(embedTitle)
                .setDescription(embedDescription)
                .addField("Player", warnedPlayerName, false)
                .addField("Reason", reason, false)
                .addField("Duration", duration, false)
                .addField("Warned By", issuer, false)
                .setFooter(embedFooter, null);

        if (!thumbnailUrl.isEmpty()) {
            embed.setThumbnail(thumbnailUrl);
        }

        webhook.addEmbed(embed);

        try {
            webhook.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
