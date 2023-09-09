package me.dankofuk.listeners;

import me.dankofuk.Main;
import me.dankofuk.utils.WebhookUtils;
import me.dankofuk.utils.StringUtils;
import org.bukkit.GameMode;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getLogger;


// This class was made by Terrornator - feature suggestion he made
public class MiddleClick implements Listener {
    @EventHandler
    public void onMiddleClick(InventoryClickEvent e){
        Player p = (Player) e.getWhoClicked();
        Configuration config = Main.getInstance().getConfig();
        Logger log = getLogger();
        WebhookUtils webhook = new WebhookUtils(config.getString("creative-logging.webhook-url"));
        if (!p.hasPermission("commandlogger.creative-logging.log")){
            return;
        }
        if (!p.getGameMode().equals(GameMode.CREATIVE)){
            return;
        }
        if (!Objects.requireNonNull(e.getClickedInventory()).getType().equals(InventoryType.PLAYER)){
            return;
        }
        if (Objects.requireNonNull(e.getCursor()).getItemMeta()==null){
            return;
        }
        if (!e.getClick().isCreativeAction()){
            return;
        }
        if (e.getCursor().hasItemMeta()){
            String displayName = Objects.requireNonNull(e.getCursor().getItemMeta()).getDisplayName().replaceAll("§[0-9a-fk-or]", "");
            webhook.addEmbed(new WebhookUtils.EmbedObject()
                    .setTitle(config.getString("creative-logging.creative.title"))
                    .addField("Player name:", StringUtils.format(config.getString("creative-logging.creative.player"), "%player%", p.getName()),false)
                    .addField("Item:", StringUtils.format(config.getString("creative-logging.creative.item"), "%item%",e.getCursor().getType().toString()),false)
                    .addField("Amount:", StringUtils.format(config.getString("creative-logging.creative.amount"),"%amount%", String.valueOf(e.getCursor().getAmount())),false)
                    .addField("Name of item:", StringUtils.format(config.getString("creative-logging.creative.name"), "%name%", displayName),false)
                    .addField("Lore:", StringUtils.format(config.getString("creative-logging.creative.lore"), "%lore%", Objects.requireNonNull(e.getCursor().getItemMeta().getLore()).toString().replaceAll("ยง[0-9a-fk-or]", " ")),false)
                    .addField("Location:", StringUtils.format(config.getString("creative-logging.creative.location"), "%location-x%", String.valueOf(p.getLocation().getX()), "%location-y%", String.valueOf(p.getLocation().getY()), "%location-z%", String.valueOf(p.getLocation().getZ())),false)
                    .addField("World:", StringUtils.format(config.getString("creative-logging.creative.world"), "%world%", p.getWorld().getName()),false)
                    .setThumbnail("https://crafatar.com/avatars/"+p.getUniqueId()+"?overlay=head")
                    .setColor(Color.BLACK));
            try {
                webhook.execute();
            }
            catch (IOException err) {
                log.severe(err.getMessage());
            }
        }
        else{
            webhook.addEmbed(new WebhookUtils.EmbedObject()
                    .setTitle(config.getString("creative-logging.creative.title"))
                    .addField("Player name:", StringUtils.format(config.getString("creative-logging.creative.player"), "%player%", p.getName()),false)
                    .addField("Item:", StringUtils.format(config.getString("creative-logging.creative.item"), "%item%",e.getCursor().getType().toString()),false)
                    .addField("Amount:", StringUtils.format(config.getString("creative-logging.creative.amount"),"%amount%", String.valueOf(e.getCursor().getAmount())),false)
                    .addField("Location:", StringUtils.format(config.getString("creative-logging.creative.location"), "%location-x%", String.valueOf(p.getLocation().getX()), "%location-y%", String.valueOf(p.getLocation().getY()), "%location-z%", String.valueOf(p.getLocation().getZ())),false)
                    .addField("World:", StringUtils.format(config.getString("creative-logging.creative.world"), "%world%", p.getWorld().getName()),false)
                    .setThumbnail("https://crafatar.com/avatars/"+p.getUniqueId()+"?overlay=head")
                    .setColor(Color.BLACK));
            try {
                webhook.execute();
            }
            catch (IOException err) {
                log.severe(err.getMessage());
            }
        }
    }
}
