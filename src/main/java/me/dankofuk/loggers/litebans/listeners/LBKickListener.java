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

public class LBKickListener implements Listener {

    public KushStaffUtils instance;

    public LBKickListener(KushStaffUtils instance) {
        this.instance = instance;
    }


    public void registerEvents() {
        Events.get().register(new Events.Listener() {
            @Override
            public void entryAdded(Entry entry) {
                if ("kick".equals(entry.getType())) {
                    sendBanInfoToDiscord(entry);
                }
            }

            @Override
            public void entryRemoved(Entry entry) {
            }
        });
    }

    private void sendBanInfoToDiscord(Entry kickEntry) {
        Configuration config = KushStaffUtils.getInstance().getConfig();
        long time = System.currentTimeMillis() / 1000L;

        String webhookUrl = StringUtils.format(config.getString("litebans.kick.webhookUrl"));
        String embedTitle = StringUtils.format(config.getString("litebans.kick.embedTitle"));
        String embedDescription = StringUtils.format(config.getString("litebans.kick.embedDescription").replace("%time%", "<t:" + time + ":R>"));
        String embedFooter = StringUtils.format(config.getString("litebans.kick.embedFooter").replace("%time%", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date())));
        String thumbnailUrl = StringUtils.format(config.getString("litebans.kick.embedThumbnail"));
        String avatarUrl = StringUtils.format(config.getString("litebans.kick.embedAvatar"));

        UUID kickPlayerUUID = UUID.fromString(Objects.requireNonNull(kickEntry.getUuid()));
        OfflinePlayer kickPlayer = Bukkit.getOfflinePlayer(kickPlayerUUID);
        String kickPlayerName = kickPlayer.getName();
        String duration = kickEntry.getDurationString();
        String reason = kickEntry.getReason();
        String issuer = kickEntry.getExecutorName();

        WebhookUtils webhook = new WebhookUtils(webhookUrl);
        webhook.setAvatarUrl(avatarUrl);
        WebhookUtils.EmbedObject embed = new WebhookUtils.EmbedObject()
                .setTitle(embedTitle)
                .setDescription(embedDescription)
                .addField("Player", kickPlayerName, false)
                .addField("Reason", reason, false)
                .addField("Kicked By", issuer, false)
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
