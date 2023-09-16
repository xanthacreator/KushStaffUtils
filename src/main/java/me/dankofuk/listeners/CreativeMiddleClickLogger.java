package me.dankofuk.listeners;

import me.dankofuk.KushStaffUtils;
import me.dankofuk.utils.WebhookUtils;
import me.dankofuk.utils.StringUtils;
import org.bukkit.GameMode;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.meta.ItemMeta;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getLogger;

public class CreativeMiddleClickLogger implements Listener {
    public KushStaffUtils instance;

    public CreativeMiddleClickLogger(KushStaffUtils instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onMiddleClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Configuration config = KushStaffUtils.getInstance().getConfig();
        Logger log = getLogger();
        WebhookUtils webhook = new WebhookUtils(config.getString("creative-logging.webhook-url"));

        if (!p.hasPermission("commandlogger.creative-logging.log")) {
            return;
        }

        if (!p.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }

        if (e.getClickedInventory() == null || !e.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
            return;
        }

        if (e.getCursor() == null || e.getCursor().getItemMeta() == null) {
            return;
        }

        InventoryAction action = e.getAction();

        if (action == InventoryAction.DROP_ALL_CURSOR || action == InventoryAction.DROP_ALL_SLOT || action == InventoryAction.DROP_ONE_CURSOR || action == InventoryAction.DROP_ONE_SLOT) {
            webhook.addEmbed(new WebhookUtils.EmbedObject()
                    .setTitle(config.getString("creative-logging.drop.title"))
                    .addField(">  Player name:", StringUtils.format(config.getString("creative-logging.drop.player"), "%player%", p.getName()), false)
                    .addField(">  Item:", StringUtils.format(config.getString("creative-logging.drop.item"), "%item%", e.getCursor().getType().toString()), false)
                    .addField(">  Amount:", StringUtils.format(config.getString("creative-logging.drop.amount"), "%amount%", String.valueOf(e.getCursor().getAmount())), false)
                    .addField(">  Location:", StringUtils.format(config.getString("creative-logging.drop.location"), "%location-x%", String.valueOf(p.getLocation().getX()), "%location-y%", String.valueOf(p.getLocation().getY()), "%location-z%", String.valueOf(p.getLocation().getZ())), false)
                    .addField(">  World:", StringUtils.format(config.getString("creative-logging.drop.world"), "%world%", p.getWorld().getName()), false)
                    .setThumbnail("https://crafatar.com/avatars/" + p.getUniqueId() + "?overlay=head")
                    .setColor(Color.CYAN));

            try {
                webhook.execute();
            } catch (IOException err) {
                log.severe(err.getMessage());
            }
        } else if (e.getCursor().hasItemMeta()) {
            ItemMeta itemMeta = e.getCursor().getItemMeta();
            String displayName = "";
            String loreText = "";

            if (itemMeta.hasDisplayName()) {
                displayName = itemMeta.getDisplayName().replaceAll("§[0-9a-fk-or]", "");
            }

            if (itemMeta.hasLore()) {
                loreText = Objects.requireNonNull(itemMeta.getLore()).toString().replaceAll("§[0-9a-fk-or]", " ");
            }

            webhook.addEmbed(new WebhookUtils.EmbedObject()
                    .setTitle(config.getString("creative-logging.creative.title"))
                    .addField(">  Player name:", StringUtils.format(config.getString("creative-logging.creative.player"), "%player%", p.getName()), false)
                    .addField(">  Item Material:", StringUtils.format(config.getString("creative-logging.creative.item"), "%item%", e.getCursor().getType().toString()), false)
                    .addField(">  Amount:", StringUtils.format(config.getString("creative-logging.creative.amount"), "%amount%", String.valueOf(e.getCursor().getAmount())), false)
                    .addField(">  Name of item:", StringUtils.format(config.getString("creative-logging.creative.name"), "%name%", displayName), false)
                    .addField(">  Lore:", StringUtils.format(config.getString("creative-logging.creative.lore"), "%lore%", loreText), false)
                    .addField(">  Location:", StringUtils.format(config.getString("creative-logging.creative.location"), "%location-x%", String.valueOf(p.getLocation().getX()), "%location-y%", String.valueOf(p.getLocation().getY()), "%location-z%", String.valueOf(p.getLocation().getZ())), false)
                    .addField(">  World:", StringUtils.format(config.getString("creative-logging.creative.world"), "%world%", p.getWorld().getName()), false)
                    .setThumbnail("https://crafatar.com/avatars/" + p.getUniqueId() + "?overlay=head")
                    .setColor(Color.CYAN));

            try {
                webhook.execute();
            } catch (IOException err) {
                log.severe(err.getMessage());
            }
        } else {
            webhook.addEmbed(new WebhookUtils.EmbedObject()
                    .setTitle(config.getString("creative-logging.creative.title"))
                    .addField(">  Player name:", StringUtils.format(config.getString("creative-logging.creative.player"), "%player%", p.getName()), false)
                    .addField(">  Item Material:", StringUtils.format(config.getString("creative-logging.creative.item"), "%item%", e.getCursor().getType().toString()), false)
                    .addField(">  Amount:", StringUtils.format(config.getString("creative-logging.creative.amount"), "%amount%", String.valueOf(e.getCursor().getAmount())), false)
                    .addField(">  Location:", StringUtils.format(config.getString("creative-logging.creative.location"), "%location-x%", String.valueOf(p.getLocation().getX()), "%location-y%", String.valueOf(p.getLocation().getY()), "%location-z%", String.valueOf(p.getLocation().getZ())), false)
                    .addField(">  World:", StringUtils.format(config.getString("creative-logging.creative.world"), "%world%", p.getWorld().getName()), false)
                    .setThumbnail("https://crafatar.com/avatars/" + p.getUniqueId() + "?overlay=head")
                    .setColor(Color.CYAN));

            try {
                webhook.execute();
            } catch (IOException err) {
                log.severe(err.getMessage());
            }
        }
    }
}
