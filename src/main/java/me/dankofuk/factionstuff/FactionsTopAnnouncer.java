package me.dankofuk.factionstuff; // Update the package name

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.clip.placeholderapi.PlaceholderAPI;
import me.dankofuk.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FactionsTopAnnouncer implements Listener {

        private Main main;
        private FileConfiguration config;

        public FactionsTopAnnouncer(FileConfiguration config) {
                this.config = config;

                boolean isEnabled = Main.getInstance().getConfig().getBoolean("announcer.enabled");
                if (isEnabled) {
                        scheduleAnnouncements();
                }
        }

        public void accessConfigs() {
                String title = Main.getInstance().getConfig().getString("announcer.title");
                String footer = Main.getInstance().getConfig().getString("announcer.footer");
                String username = Main.getInstance().getConfig().getString("announcer.username");
                String thumbnailUrl = Main.getInstance().getConfig().getString("announcer.thumbnailUrl");
                String avatarUrl = Main.getInstance().getConfig().getString("announcer.avatarUrl");
                String webhookUrl = Main.getInstance().getConfig().getString("announcer.webhookUrl");
                List<String> messages = Main.getInstance().getConfig().getStringList("announcer.message");
                long announcementInterval = Main.getInstance().getConfig().getLong("announcer.sendInterval") * 20;
                boolean isEnabled = Main.getInstance().getConfig().getBoolean("announcer.enabled");
        }

        private void scheduleAnnouncements() {
                new BukkitRunnable() {
                        @Override
                        public void run() {
                                if (Main.getInstance().getConfig().getBoolean("announcer.enabled")) {
                                        sendAnnouncement();
                                }
                        }
                }.runTaskTimer(Bukkit.getPluginManager().getPlugin("KushStaffUtils"), Main.getInstance().getConfig().getLong("announcer.sendInterval"), Main.getInstance().getConfig().getLong("announcer.sendInterval"));
        }

        private void sendAnnouncement() {
                if (Main.getInstance().getConfig().getString("announcer.webhookUrl") == null || Main.getInstance().getConfig().getString("announcer.webhookUrl").isEmpty()) {
                        return; // No webhook URL, nothing to send
                }

                try {
                        URL url = new URL(Main.getInstance().getConfig().getString("announcer.webhookUrl"));
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setRequestProperty("User-Agent", "FactionsTopAnnouncer");

                        connection.setDoOutput(true);

                        String message = Main.getInstance().getConfig().getStringList("announcer.message").stream()
                                .map(line -> PlaceholderAPI.setPlaceholders(null, line)) // Replace 'player' with the appropriate Player instance
                                .collect(Collectors.joining("\n"));

                        JsonObject json = new JsonObject();
                        json.addProperty("username", Main.getInstance().getConfig().getString("announcer.username"));
                        json.addProperty("avatar_url", Main.getInstance().getConfig().getString("announcer.avatarUrl"));

                        JsonObject embed = new JsonObject();
                        embed.addProperty("description", message);
                        embed.addProperty("color", getColorCode("#00FF00")); // Change color code as needed
                        embed.addProperty("title", Main.getInstance().getConfig().getString("announcer.title"));

                        JsonObject footerObj = new JsonObject();
                        footerObj.addProperty("text", Main.getInstance().getConfig().getString("announcer.footer"));

                        embed.add("footer", footerObj);

                        JsonObject thumbnail = new JsonObject();
                        thumbnail.addProperty("url", Main.getInstance().getConfig().getString("announcer.thumbnailUrl"));
                        embed.add("thumbnail", thumbnail);

                        JsonArray embeds = new JsonArray();
                        embeds.add(embed);
                        json.add("embeds", embeds);

                        String payload = json.toString();

                        try (OutputStream os = connection.getOutputStream()) {
                                os.write(payload.getBytes());
                        }

                        int responseCode = connection.getResponseCode();
                        String responseMessage = connection.getResponseMessage();

                } catch (MalformedURLException e) {
                        Bukkit.getLogger().warning("[FactionsTopAnnouncerWebhook] Invalid webhook URL specified: " + Main.getInstance().getConfig().getString("announcer.webhookUrl"));
                        e.printStackTrace();
                } catch (ProtocolException e) {
                        Bukkit.getLogger().warning("[FactionsTopAnnouncerWebhook] Invalid protocol specified in webhook URL: " + Main.getInstance().getConfig().getString("announcer.webhookUrl"));
                        e.printStackTrace();
                } catch (IOException e) {
                        Bukkit.getLogger().warning("[FactionsTopAnnouncerWebhook] Error sending message to Discord webhook.");
                        e.printStackTrace();
                }
        }

        private int getColorCode(String color) {
                color = color.replace("#", "");
                return Integer.parseInt(color, 16);
        }
}
