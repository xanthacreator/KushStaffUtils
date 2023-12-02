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

public class LBMuteListener implements Listener {

    public KushStaffUtils instance;

    public LBMuteListener(KushStaffUtils instance) {
        this.instance = instance;
    }


    public void registerEvents() {
        Events.get().register(new Events.Listener() {
            @Override
            public void entryAdded(Entry entry) {
                if ("mute".equals(entry.getType())) {
                    sendBanInfoToDiscord(entry);
                }
            }

            @Override
            public void entryRemoved(Entry entry) {
            }
        });
    }

    private void sendBanInfoToDiscord(Entry muteEntry) {
        Configuration config = KushStaffUtils.getInstance().getConfig();
        long time = System.currentTimeMillis() / 1000L;

        String webhookUrl = StringUtils.format(config.getString("litebans.mute.webhookUrl"));
        String embedTitle = StringUtils.format(config.getString("litebans.mute.embedTitle"));
        String embedDescription = StringUtils.format(config.getString("litebans.mute.embedDescription").replace("%time%", "<t:" + time + ":R>"));
        String embedFooter = StringUtils.format(config.getString("litebans.mute.embedFooter").replace("%time%", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date())));
        String thumbnailUrl = StringUtils.format(config.getString("litebans.mute.embedThumbnail").replace("%time%", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date())));
        String avatarUrl = StringUtils.format(config.getString("litebans.mute.embedAvatar"));

        UUID mutePlayerUUID = UUID.fromString(Objects.requireNonNull(muteEntry.getUuid()));
        OfflinePlayer mutePlayer = Bukkit.getOfflinePlayer(mutePlayerUUID);
        String mutePlayerName = mutePlayer.getName();
        String duration = muteEntry.getDurationString();
        String reason = muteEntry.getReason();
        String issuer = muteEntry.getExecutorName();

        WebhookUtils webhook = new WebhookUtils(webhookUrl);
        webhook.setAvatarUrl(avatarUrl);
        WebhookUtils.EmbedObject embed = new WebhookUtils.EmbedObject()
                .setTitle(embedTitle)
                .setDescription(embedDescription)
                .addField("Player", mutePlayerName, false)
                .addField("Duration", duration, false)
                .addField("Reason", reason, false)
                .addField("Muted By", issuer, false)
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
