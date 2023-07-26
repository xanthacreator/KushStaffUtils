package me.dankofuk.listeners;

import me.dankofuk.utils.ColorUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

public class FlyBoostListener implements Listener {

    private Plugin plugin;
    private FileConfiguration config;

    public FlyBoostListener(Plugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.isFlying()) {
            return;
        }
        if (!config.getBoolean("player-speed-limiter.enabled")) {
            // Fly Speed Limiter is not enabled, so do nothing.
            return;
        }
        if (!player.hasPermission("flyboostlimiter.bypass")) {
            double maxMoveSpeed = config.getDouble("player-speed-limiter.move-max-speed");
            double currentMoveSpeed = event.getFrom().distance(event.getTo());
            if (currentMoveSpeed > maxMoveSpeed) {
                // Prevent the player from moving beyond the maximum speed
                player.teleport(event.getFrom());
                player.kickPlayer(ColorUtils.translateColorCodes(config.getString("player-speed-limiter.kick-message").replace("\\n", "\n")));
            }
        }
    }
}
