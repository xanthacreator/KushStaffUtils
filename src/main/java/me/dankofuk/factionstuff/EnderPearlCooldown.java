package me.dankofuk.factionstuff;

import me.dankofuk.utils.ColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnderPearlCooldown implements Listener {

    public Plugin plugin;
    public Map<UUID, Long> cooldowns = new HashMap<>();
    public int enderpearlCooldownTime;
    public String enderpearlCooldownMessage = "&cYou must wait %time_left% seconds before using this again!";
    public boolean enderpearlEnabled;
    public int chorusCooldownTime;
    public String chorusCooldownMessage = "&cYou must wait %time_left% seconds before using this again!";
    public boolean chorusEnabled;

    public EnderPearlCooldown(Plugin plugin) {
        this.plugin = plugin;

        plugin.saveDefaultConfig();
        enderpearlCooldownTime = plugin.getConfig().getInt("enderpearl.cooldown-time", 10);
        enderpearlEnabled = plugin.getConfig().getBoolean("enderpearl.enabled", true);
        chorusCooldownTime = plugin.getConfig().getInt("chorus.cooldown-time", 10);
        chorusEnabled = plugin.getConfig().getBoolean("chorus.enabled", true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (enderpearlEnabled && event.getItem() != null && event.getItem().getType().toString().contains("ENDER_PEARL")) {
            if (cooldowns.containsKey(uuid) && cooldowns.get(uuid) > System.currentTimeMillis()) {
                // The player is still on cooldown
                long timeLeft = (cooldowns.get(uuid) - System.currentTimeMillis()) / 1000;
                player.sendMessage(ColorUtils.translateColorCodes(enderpearlCooldownMessage.replace("%time_left%", String.valueOf(timeLeft))));
                event.setCancelled(true);
            } else {
                // The player can use the Enderpearl
                cooldowns.put(uuid, System.currentTimeMillis() + (enderpearlCooldownTime * 1000));
            }
        }

        if (chorusEnabled && event.getItem() != null && event.getItem().getType().toString().contains("CHORUS_FRUIT")) {
            if (cooldowns.containsKey(uuid) && cooldowns.get(uuid) > System.currentTimeMillis()) {
                // The player is still on cooldown
                long timeLeft = (cooldowns.get(uuid) - System.currentTimeMillis()) / 1000;
                player.sendMessage(ColorUtils.translateColorCodes(chorusCooldownMessage.replace("%time_left%", String.valueOf(timeLeft))));
                event.setCancelled(true);
            } else {
                // The player can use the Chorus Fruit
                cooldowns.put(uuid, System.currentTimeMillis() + (chorusCooldownTime * 1000));
            }
        }
    }

    public void start() {
        if (enderpearlEnabled || chorusEnabled) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (UUID uuid : cooldowns.keySet()) {
                        if (cooldowns.get(uuid) < System.currentTimeMillis()) {
                            cooldowns.remove(uuid);
                        }
                    }
                }
            }.runTaskTimer(plugin, 20L, 20L);
        }
    }

    public void setEnderpearlCooldownMessage(String cooldownMessage) {
        this.enderpearlCooldownMessage = cooldownMessage;
    }

    public void setEnderpearlCooldown(int enderpearlCooldownTime) {
        this.enderpearlCooldownTime = enderpearlCooldownTime;
    }

    public void setEnderpearlEnabled(boolean enderpearlEnabled) {
        this.enderpearlEnabled = enderpearlEnabled;
    }

    public void setChorusCooldownMessage(String chorusCooldownMessage) {
        this.chorusCooldownMessage = chorusCooldownMessage;
    }

    public void setChorusCooldownTime(int chorusCooldownTime){
        this.chorusCooldownTime = chorusCooldownTime;
    }

    public void setChorusEnabled(boolean chorusEnabled) {
        this.chorusEnabled = chorusEnabled;
    }
}