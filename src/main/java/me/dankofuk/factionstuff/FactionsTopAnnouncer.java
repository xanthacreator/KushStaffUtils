package me.dankofuk.factionstuff; // Update the package name

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.clip.placeholderapi.PlaceholderAPI;
import me.dankofuk.KushStaffUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FactionsTopAnnouncer implements Listener {

        private KushStaffUtils main;
        private final FileConfiguration config;
        private BukkitTask announcementTask;

        public FactionsTopAnnouncer(FileConfiguration config) {
                this.config = config;

                boolean isEnabled = KushStaffUtils.getInstance().getConfig().getBoolean("announcer.enabled");
                if (isEnabled) {
                        scheduleAnnouncements();
                }
        }

        public void accessConfigs() {
                String title = KushStaffUtils.getInstance().getConfig().getString("announcer.title");
                String footer = KushStaffUtils.getInstance().getConfig().getString("announcer.footer");
                String username = KushStaffUtils.getInstance().getConfig().getString("announcer.username");
                String thumbnailUrl = KushStaffUtils.getInstance().getConfig().getString("announcer.thumbnailUrl");
                String avatarUrl = KushStaffUtils.getInstance().getConfig().getString("announcer.avatarUrl");
                String webhookUrl = KushStaffUtils.getInstance().getConfig().getString("announcer.webhookUrl");
                List<String> messages = KushStaffUtils.getInstance().getConfig().getStringList("announcer.messages");
                long announcementInterval = KushStaffUtils.getInstance().getConfig().getLong("announcer.sendInterval") * 20;
                boolean isEnabled = KushStaffUtils.getInstance().getConfig().getBoolean("announcer.enabled");
        }

        private void scheduleAnnouncements() {
                announcementTask = new BukkitRunnable() {
                        @Override
                        public void run() {
                                if (KushStaffUtils.getInstance().getConfig().getBoolean("announcer.enabled")) {
                                        sendAnnouncement();
                                }
                        }
                }.runTaskTimer(Bukkit.getPluginManager().getPlugin("KushStaffUtils"), 0L, KushStaffUtils.getInstance().getConfig().getLong("announcer.sendInterval") * 20L); // Delay = 0, Period = sendInterval * 20 ticks
        }

        public void cancelAnnouncements() {
                if (announcementTask != null) {
                        announcementTask.cancel();
                }
        }

        public void reloadAnnouncer() {
                accessConfigs();
                cancelAnnouncements();
                scheduleAnnouncements();
        }

        private void sendAnnouncement() {
                if (KushStaffUtils.getInstance().getConfig().getString("announcer.webhookUrl") == null || Objects.requireNonNull(KushStaffUtils.getInstance().getConfig().getString("announcer.webhookUrl")).isEmpty()) {
                        return; // No webhook URL, nothing to send
                }

                try {
                        URL url = new URL(Objects.requireNonNull(KushStaffUtils.getInstance().getConfig().getString("announcer.webhookUrl")));
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setRequestProperty("User-Agent", "FactionsTopAnnouncer");

                        connection.setDoOutput(true);

                        String message = KushStaffUtils.getInstance().getConfig().getStringList("announcer.messages").stream()
                                .map(line -> {
                                        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                                                return PlaceholderAPI.setPlaceholders(null, line);
                                        } else {
                                                return line;
                                        }
                                })
                                .collect(Collectors.joining("\n"));

                        JsonObject json = new JsonObject();
                        json.addProperty("username", KushStaffUtils.getInstance().getConfig().getString("announcer.username"));
                        json.addProperty("avatar_url", KushStaffUtils.getInstance().getConfig().getString("announcer.avatarUrl"));

                        JsonObject embed = new JsonObject();
                        embed.addProperty("description", message);
                        embed.addProperty("color", getColorCode("#00FF00")); // Change color code as needed
                        embed.addProperty("title", KushStaffUtils.getInstance().getConfig().getString("announcer.title"));

                        JsonObject footerObj = new JsonObject();
                        footerObj.addProperty("text", KushStaffUtils.getInstance().getConfig().getString("announcer.footer"));

                        embed.add("footer", footerObj);

                        JsonObject thumbnail = new JsonObject();
                        thumbnail.addProperty("url", KushStaffUtils.getInstance().getConfig().getString("announcer.thumbnailUrl"));
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
                        Bukkit.getLogger().warning("[FactionsTopAnnouncerWebhook] Invalid webhook URL specified: " + KushStaffUtils.getInstance().getConfig().getString("announcer.webhookUrl"));
                        e.printStackTrace();
                } catch (ProtocolException e) {
                        Bukkit.getLogger().warning("[FactionsTopAnnouncerWebhook] Invalid protocol specified in webhook URL: " + KushStaffUtils.getInstance().getConfig().getString("announcer.webhookUrl"));
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
