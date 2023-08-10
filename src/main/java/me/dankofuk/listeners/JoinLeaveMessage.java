package me.dankofuk.listeners;

import me.dankofuk.utils.ColorUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeaveMessage implements Listener {

    private String joinMessage;
    private String leaveMessage;
    private FileConfiguration config;


    public JoinLeaveMessage(String joinMessage, String leaveMessage) {
        this.joinMessage = joinMessage;
        this.leaveMessage = leaveMessage;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerName = event.getPlayer().getName(); // Get the player's name
        String joinMessage = ColorUtils.translateColorCodes(config.getString("messages.join-message").replace("%player%", playerName));
        event.setJoinMessage(joinMessage);
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName(); // Get the player's name
        String leaveMessage = ColorUtils.translateColorCodes(config.getString("messages.leave-message").replace("%player%", playerName));
        event.setQuitMessage(leaveMessage);
    }
}
