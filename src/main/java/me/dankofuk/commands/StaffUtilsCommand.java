package me.dankofuk.commands;

import me.dankofuk.KushStaffUtils;
import me.dankofuk.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StaffUtilsCommand implements CommandExecutor {
    public KushStaffUtils instance;


    public StaffUtilsCommand() {
        this.instance = KushStaffUtils.getInstance();
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, @NotNull String[] args) {
        if (cmd.getName().equalsIgnoreCase("stafflogger")) {
            if (!sender.hasPermission("commandlogger.reload")) {
                sender.sendMessage(ColorUtils.translateColorCodes(Objects.requireNonNull(instance.messagesConfig.getString("noPermissionMessage"))));
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                instance.reloadConfigOptions();
                sender.sendMessage(ColorUtils.translateColorCodes(Objects.requireNonNull(instance.messagesConfig.getString("reloadMessage"))));
                return true;
            }
            return false;
        }
        return false;
    }

}
