package me.dankofuk.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class VanishCommand implements CommandExecutor, Listener {

    private Set<Player> vanishedPlayers = new HashSet<>();
    private File dataFolder;

    public VanishCommand(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("vanish.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (vanishedPlayers.contains(player)) {
            // Player is already vanished, make them visible
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.showPlayer(player);
            }
            vanishedPlayers.remove(player);
            player.sendMessage(ChatColor.GREEN + "You are now visible.");
        } else {
            // Player is not vanished, make them invisible
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.hasPermission("vanish.see")) {
                    onlinePlayer.hidePlayer(player);
                }
            }
            vanishedPlayers.add(player);
            player.sendMessage(ChatColor.GREEN + "You are now invisible.");
        }

        // Save player data to file
        savePlayerData(player);

        return true;
    }

    private File getPlayerDataFile(Player player) {
        return new File(dataFolder, player.getUniqueId() + ".yml");
    }

    private void savePlayerData(Player player) {
        File playerFile = getPlayerDataFile(player);

        try {
            YamlConfiguration playerConfig = new YamlConfiguration();
            playerConfig.set("isVanish", vanishedPlayers.contains(player));
            playerConfig.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPlayerData(Player player) {
        File playerFile = getPlayerDataFile(player);

        if (playerFile.exists()) {
            YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
            boolean isVanish = playerConfig.getBoolean("isVanish");

            if (isVanish) {
                vanishedPlayers.add(player);
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (!onlinePlayer.hasPermission("vanish.see")) {
                        onlinePlayer.hidePlayer(player);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) { //TODO FIX THIS
        Player player = event.getPlayer();
        loadPlayerData(player);

        if (vanishedPlayers.contains(player)) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.hasPermission("vanish.see")) {
                    onlinePlayer.hidePlayer(player);
                }
            }
        }
    }
}
