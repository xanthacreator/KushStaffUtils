package me.dankofuk.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeaveMessage implements Listener {

    public String joinMessage;
    public String leaveMessage;
    public String playerName;
    public FileConfiguration config;


    public JoinLeaveMessage(String joinMessage, String leaveMessage, String playerName) {
        this.joinMessage = joinMessage;
        this.leaveMessage = leaveMessage;
        this.playerName = playerName;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String joinMessage = config.getString("messages.join-message");
        event.setJoinMessage(playerName + joinMessage);
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String leaveMessage = config.getString("messages.leave-message");
        event.setQuitMessage(playerName + leaveMessage);
    }
}
