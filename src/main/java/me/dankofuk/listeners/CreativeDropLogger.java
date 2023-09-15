package me.dankofuk.listeners;

import java.awt.Color;
import java.io.IOException;
import java.util.logging.Logger;

import me.dankofuk.KushStaffUtils;
import me.dankofuk.utils.StringUtils;
import me.dankofuk.utils.WebhookUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.meta.ItemMeta;

public class CreativeDropLogger implements Listener {
    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        FileConfiguration fileConfiguration = KushStaffUtils.getInstance().getConfig();
        Logger log = Bukkit.getLogger();
        WebhookUtils webhook = new WebhookUtils(fileConfiguration.getString("creative-logging.webhook-url"));
        if (!p.hasPermission("commandlogger.creative-logging.log"))
            return;
        if (e.getItemDrop().getItemStack().getItemMeta() == null)
            return;
        if (e.getItemDrop().getItemStack().hasItemMeta()) {
            ItemMeta itemMeta = e.getItemDrop().getItemStack().getItemMeta();
            String displayName = "";
            String loreText = "";

            if (itemMeta.hasDisplayName()) {
                displayName = itemMeta.getDisplayName().replaceAll("§[0-9a-fk-or]", "");
            }

            if (itemMeta.hasLore()) {
                loreText = itemMeta.getLore().toString().replaceAll("§[0-9a-fk-or]", " ");
            }
                    webhook.addEmbed((new WebhookUtils.EmbedObject())
                            .setTitle(fileConfiguration.getString("creative-logging.drop.title"))
                            .addField("Player name:", StringUtils.format(fileConfiguration.getString("creative-logging.drop.player"), new String[] { "%player%", p.getName() }), false)
                            .addField("Item:", StringUtils.format(fileConfiguration.getString("creative-logging.drop.item"), new String[] { "%item%", e.getItemDrop().getItemStack().getType().toString() }), false)
                            .addField("Enchants:", StringUtils.format(fileConfiguration.getString("creative-logging.drop.enchants"), new String[] { "%enchants%", e.getItemDrop().getItemStack().getItemMeta().getEnchants().toString() }), false)
                            .addField("Amount:", StringUtils.format(fileConfiguration.getString("creative-logging.drop.amount"), new String[] { "%amount%", String.valueOf(e.getItemDrop().getItemStack().getAmount()) }), false)
                            .addField("Name of item:", StringUtils.format(fileConfiguration.getString("creative-logging.drop.name"), "%name%", displayName), false)
                            .addField("Lore:", StringUtils.format(fileConfiguration.getString("creative-logging.drop.lore"), "%lore%", loreText), false)
                            .addField("Location:", StringUtils.format(fileConfiguration.getString("creative-logging.drop.location"), new String[] { "%location-x%", String.valueOf(p.getLocation().getX()), "%location-y%", String.valueOf(p.getLocation().getY()), "%location-z%", String.valueOf(p.getLocation().getZ()) }), false)
                            .addField("World:", StringUtils.format(fileConfiguration.getString("creative-logging.drop.world"), new String[] { "%world%", p.getWorld().getName() }), false)
                            .setThumbnail("https://crafatar.com/avatars/" + p.getUniqueId() + "?overlay=head")
                            .setColor(Color.RED));
            try {
                webhook.execute();
            } catch (IOException err) {
                log.severe(err.getMessage());
            }
    } else {
                webhook.addEmbed((new WebhookUtils.EmbedObject())
                        .setTitle(fileConfiguration.getString("creative-logging.drop.title"))
                        .addField("Player name:", StringUtils.format(fileConfiguration.getString("creative-logging.drop.player"), new String[] { "%player%", p.getName() }), false)
                        .addField("Item:", StringUtils.format(fileConfiguration.getString("creative-logging.drop.item"), new String[] { "%item%", e.getItemDrop().getItemStack().getType().toString() }), false)
                        .addField("Enchants:", StringUtils.format(fileConfiguration.getString("creative-logging.drop.enchants"), new String[] { "%enchants%", e.getItemDrop().getItemStack().getItemMeta().getEnchants().toString() }), false)
                        .addField("Amount:", StringUtils.format(fileConfiguration.getString("creative-logging.drop.amount"), new String[] { "%amount%", String.valueOf(e.getItemDrop().getItemStack().getAmount()) }), false)
                        .addField("Location:", StringUtils.format(fileConfiguration.getString("creative-logging.drop.location"), new String[] { "%location-x%", String.valueOf(p.getLocation().getX()), "%location-y%", String.valueOf(p.getLocation().getY()), "%location-z%", String.valueOf(p.getLocation().getZ()) }), false)
                        .addField("World:", StringUtils.format(fileConfiguration.getString("creative-logging.drop.world"), new String[] { "%world%", p.getWorld().getName() }), false)
                        .setThumbnail("https://crafatar.com/avatars/" + p.getUniqueId() + "?overlay=head")
                        .setColor(Color.RED));
                try {
                    webhook.execute();
                } catch (IOException err) {
                    log.severe(err.getMessage());
                }
            }
        }
}

