package me.dankofuk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandLogViewer implements CommandExecutor {
    private final int pageSize;
    private final String logsFolder;

    public CommandLogViewer(String logsFolder, int pageSize) {
        this.logsFolder = logsFolder;
        this.pageSize = pageSize;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("commandlogger.viewlogs.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1 || args.length > 2) {
            player.sendMessage(ChatColor.RED + "Usage: /viewlogs <player> [page]");
            return true;
        }

        String playerNameOrUuid = args[0];
        UUID uuid = null;
        try {
            uuid = UUID.fromString(playerNameOrUuid);
        } catch (IllegalArgumentException e) {
            // If the argument is not a valid UUID, assume it's a player name
        }

        if (uuid != null) {
            // If the argument is a UUID, look up the player name and use that for the file name
            playerNameOrUuid = Bukkit.getOfflinePlayer(uuid).getName();
            if (playerNameOrUuid == null) {
                player.sendMessage(ChatColor.RED + "No logs found for player with UUID " + uuid);
                return true;
            }
        } else {
            // If the argument is not a UUID, look up the player UUID
            Player targetPlayer = Bukkit.getPlayer(playerNameOrUuid);
            if (targetPlayer != null) {
                uuid = targetPlayer.getUniqueId();
            } else {
                player.sendMessage(ChatColor.RED + "Player " + playerNameOrUuid + " is not online or does not exist.");
                return true;
            }
        }

        String fileName = uuid.toString() + ".txt";
        File logFile = new File(logsFolder, fileName);

        if (!logFile.exists()) {
            player.sendMessage(ChatColor.RED + "No logs found for player " + playerNameOrUuid);
            return true;
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error reading log file for player " + playerNameOrUuid);
            return true;
        }

        if (lines.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No logs found for player " + playerNameOrUuid);
            return true;
        }

        int pageCount = (lines.size() - 1) / pageSize + 1;
        int currentPage = 1;

        // Check if a specific page is requested
        if (args.length == 2) {
            try {
                currentPage = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid page number: " + args[1]);
                return true;
            }
            if (currentPage < 1 || currentPage > pageCount) {
                player.sendMessage(ChatColor.RED + "Invalid page number: " + args[1]);
                return true;
            }
        }
        // Send the requested page of logs to the player
        player.sendMessage(ChatColor.GOLD + "Logs for " + ChatColor.AQUA + playerNameOrUuid + ChatColor.GOLD + " (Page " + ChatColor.AQUA + currentPage + ChatColor.GOLD + " of " + ChatColor.AQUA + pageCount + ChatColor.GOLD + "):");

        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, lines.size());

        for (int i = startIndex; i < endIndex; i++) {
            player.sendMessage(ChatColor.YELLOW + lines.get(i));
        }

        // Add clickable/hoverable components for navigating through pages
        if (pageCount > 1) {
            TextComponent pageMessage = new TextComponent(ChatColor.GOLD + "Page " + ChatColor.AQUA + currentPage + ChatColor.GOLD + " of " + ChatColor.AQUA + pageCount + ChatColor.GOLD + ": ");

            if (currentPage > 1) {
                TextComponent backButton = new TextComponent(ChatColor.AQUA + "<< Previous");
                backButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Click to go to previous page.").create()));
                backButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewlogs " + playerNameOrUuid + " " + (currentPage - 1)));
                pageMessage.addExtra(backButton);
            }

            if (currentPage < pageCount) {
                if (currentPage > 1) {
                    pageMessage.addExtra(" | ");
                }
                TextComponent forwardButton = new TextComponent(ChatColor.AQUA + "Next >>");
                forwardButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Click to go to next page.").create()));
                forwardButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewlogs " + playerNameOrUuid + " " + (currentPage + 1)));
                pageMessage.addExtra(forwardButton);
            }

            player.spigot().sendMessage(pageMessage);
        }

        return true;
    }
}