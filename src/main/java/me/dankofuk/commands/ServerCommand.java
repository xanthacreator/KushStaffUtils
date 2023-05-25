package me.dankofuk.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public class ServerCommand implements CommandExecutor, Listener {

    private final Plugin plugin;

    public ServerCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by players.");
            return true;
        } if (!player.hasPermission("commandlogger.server.use")) {
            player.sendMessage(ChatColor.RED + "You do not have permission for this command!");
            return true;
        }

        openServerSettingsGUI(player);
        return true;
    }

    private void openServerSettingsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Server Settings");

        // Add gamerules to the GUI
        for (GameRuleToggle rule : GameRuleToggle.values()) {
            ItemStack item = new ItemStack(rule.getMaterial());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + rule.getName());
            meta.setLore(Arrays.asList(
                    rule.isEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled",
                    ChatColor.GRAY + "Click to toggle"
            ));
            item.setItemMeta(meta);
            gui.setItem(rule.getSlot(), item);
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Server Settings")) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) {
                return;
            }

            for (GameRuleToggle rule : GameRuleToggle.values()) {
                if (item.getType() == rule.getMaterial()) {
                    rule.toggle();
                    ItemMeta meta = item.getItemMeta();
                    meta.setLore(Arrays.asList(
                            rule.isEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled",
                            ChatColor.GRAY + "Click to toggle"
                    ));
                    item.setItemMeta(meta);
                    player.sendMessage(rule.getName() + ": " + (rule.isEnabled() ? ChatColor.GREEN + "True" : ChatColor.RED + "False"));

                    return;
                }
            }
        }
    }

    private enum GameRuleToggle {
        DO_DAYLIGHT_CYCLE("doDaylightCycle", Material.DAYLIGHT_DETECTOR, 10),
        DO_MOB_SPAWNING("doMobSpawning", Material.DIRT, 12),
        DO_WEATHER_CYCLE("doWeatherCycle", Material.WATER_BUCKET, 14),
        KEEP_INVENTORY("keepInventory", Material.CHEST, 16);

        private final String name;
        private final Material material;
        private final int slot;
        private boolean enabled;

        GameRuleToggle(String name, Material material, int slot) {
            this.name = name;
            this.material = material;
            this.slot = slot;
            this.enabled = getGameRuleValue(name);
        }

        public String getName() {
            return name;
        }

        public Material getMaterial() {
            return material;
        }

        public int getSlot() {
            return slot;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void toggle() {
            enabled = !enabled;
            setGameRuleValue(name, enabled);
        }
    }

    private static boolean getGameRuleValue(String gameRule) {
        return Bukkit.getWorlds().get(0).getGameRuleValue(gameRule).equalsIgnoreCase("true");
    }

    private static void setGameRuleValue(String gameRule, boolean value) {
        Bukkit.getWorlds().get(0).setGameRuleValue(gameRule, String.valueOf(value));
    }
}
