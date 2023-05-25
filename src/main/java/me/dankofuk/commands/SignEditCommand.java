package me.dankofuk.commands;

import me.dankofuk.Main;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SignEditCommand implements CommandExecutor {

    public Plugin plugin;


    public SignEditCommand(Plugin plugin) {

        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /signedit <line> <text>");
            return true;
        }

        int line;
        try {
            line = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid line number.");
            return true;
        }

        if (line < 1 || line > 4) {
            player.sendMessage(ChatColor.RED + "Line number should be between 1 and 4.");
            return true;
        }

        StringBuilder textBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            textBuilder.append(args[i]).append(" ");
        }
        String text = ChatColor.translateAlternateColorCodes('&', textBuilder.toString().trim());

        if (!player.hasPermission("commandlogger.signedit.use")) {
            player.sendMessage(ChatColor.RED + "You do not have permission for this command!");
            return true;
        }

        // Get the block the player is looking at
        if (player.getTargetBlock(null, 5).getState() instanceof Sign) {
            Sign sign = (Sign) player.getTargetBlock(null, 5).getState();
            sign.setLine(line - 1, text);
            sign.update();
            player.sendMessage("Sign line " + line + " has been updated.");
        } else {
            player.sendMessage("You are not looking at a sign.");
        }

        return true;
    }
}
