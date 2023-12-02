package me.dankofuk.loggers.advancedbans;

import me.dankofuk.KushStaffUtils;
import me.dankofuk.utils.StringUtils;
import me.dankofuk.utils.WebhookUtils;
import me.leoko.advancedban.bukkit.event.PunishmentEvent;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class AKickListener implements Listener {

    private final KushStaffUtils instance;

    public AKickListener(KushStaffUtils instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onPunishment(PunishmentEvent event) {
        Punishment punishment = event.getPunishment();

        if (punishment.getType().equals(PunishmentType.KICK)) {
            sendInfoToDiscord(punishment);
        }
    }

    private UUID formatUUID(String rawUUID) {
        String uuidWithDashes = rawUUID.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5"
        );
        return UUID.fromString(uuidWithDashes);
    }

    private void sendInfoToDiscord(Punishment punishment) {
        Configuration config = KushStaffUtils.getInstance().getConfig();
        long time = System.currentTimeMillis() / 1000L;

        String webhookUrl = StringUtils.format(config.getString("advancedbans.kick.webhookUrl"));
        String embedTitle = StringUtils.format(config.getString("advancedbans.kick.embedTitle"));
        String embedDescription = StringUtils.format(config.getString("advancedbans.kick.embedDescription").replace("%time%", "<t:" + time + ":R>"));
        String embedFooter = StringUtils.format(config.getString("advancedbans.kick.embedFooter").replace("%time%", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date())));
        String thumbnailUrl = StringUtils.format(config.getString("advancedbans.kick.embedThumbnail"));
        String avatarUrl = StringUtils.format(config.getString("advancedbans.kick.embedAvatar"));

        UUID bannedPlayerUUID = formatUUID(punishment.getUuid());
        OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(bannedPlayerUUID);
        String bannedPlayerName = bannedPlayer.getName();
        String reason = punishment.getReason();
        String issuer = punishment.getOperator();

        WebhookUtils webhook = new WebhookUtils(webhookUrl);
        webhook.setAvatarUrl(avatarUrl);
        WebhookUtils.EmbedObject embed = new WebhookUtils.EmbedObject()
                .setTitle(embedTitle)
                .setDescription(embedDescription)
                .addField("Player", bannedPlayerName, false)
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

