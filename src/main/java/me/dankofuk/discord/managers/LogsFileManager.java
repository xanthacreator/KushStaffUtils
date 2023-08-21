package me.dankofuk.discord.managers;

import me.dankofuk.Main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LogsFileManager {
    public Main main;

    public LogsFileManager(Main main) {
        this.main = main;
    }

    public static List<String> readLogFile(String uuid) throws Exception {
        String logFilePath = "plugins/KushStaffUtils/logs/" + uuid + ".txt";
        Path path = Paths.get(logFilePath);

        return Files.readAllLines(path);
    }
}
