package local.gongule.utils.system;

import local.gongule.utils.formatter.DateFormatter;
import local.gongule.utils.formatter.TimeFormatter;
import local.gongule.utils.logging.Loggible;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class SystemUtils implements Loggible {

    public static final String osName = System.getProperty("os.name");
    public static final String osVersion = System.getProperty("os.version");
    public static final String osArchitecture = System.getProperty("os.arch");
    public static final String osRelease = getOSRelease();
    public static final boolean isRaspbian = isRaspbian();

    private static String getOSRelease() {
        if (isLinux())
            try (FileInputStream fileInputStream = new FileInputStream("/etc/os-release")) {
                Properties properties = new Properties();
                properties.load(fileInputStream);
                return properties.getProperty("ID");
            } catch (Exception exception) {}
        return "";
    }

    private static boolean isRaspbian() {
        return osRelease.equalsIgnoreCase("raspbian");
    }

    public static boolean isLinux() {
        String os = osName.toLowerCase();
        return os.contains("nix") || os.contains("nux") || os.contains("aix") || isRaspbian();
    }

    public static boolean isWindows() {
        String os = osName.toLowerCase();
        return os.contains("win");
    }

    public static boolean isMacos() {
        String os = osName.toLowerCase();
        return os.contains("mac") || os.contains("darwin");
    }

    public static boolean isSunos() {
        String os = osName.toLowerCase();
        return os.contains("sunos");
    }

    public static double getCPUTemperature(double defaultValue){
        if (isRaspbian())
            try (FileInputStream fstream = new FileInputStream("/sys/class/thermal/thermal_zone0/temp")) {
                BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                return Integer.parseInt(br.readLine())/1000d;
            } catch (Exception exception) {}
        return defaultValue;
    }

    public static boolean reboot() {
        return shutdown(true);
    }

    public static boolean shutdown() {
        return shutdown(false);
    }

    private static boolean shutdown(boolean reboot) {
        String command = null;
        if (isWindows()) command = "shutdown -" + (reboot ? "r" : "s") + " -t 0";
        if (isLinux()) command = "shutdown -" + (reboot ? "r" : "h") + " now";
        if (command == null)
            logger.warn("Shutdown command is not supported in this operation system ({})", osName);
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException exception) {
            logger.error("Shutdown error: {}", exception.getMessage());
            return false;
        }
        return true;
    }

    public static LocalDate setDateTime(LocalDate date, LocalTime time) {
        String command = null;
        if (isWindows()) command = "cmd /C date " + date.format(DateTimeFormatter.ofPattern("dd-MM-yy"));
        if (isLinux()) command = "date +%Y%m%d%T -s '" +
                date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + " " +
                time.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "'";
        if (command == null)
            logger.warn("Datetime changing is not supported in this operation system ({})", osName);
        try {
            String[] cmd = {"/bin/bash","-c", command};
            final Process timeProcess = Runtime.getRuntime().exec(cmd);
            timeProcess.waitFor();
            timeProcess.exitValue();
            logger.warn("Changed datetime. New value is {}", date.format(DateFormatter.get()));
        } catch (Exception exception) {
            logger.error("Impossible set datetime: {}", exception.getMessage());
        }
        return LocalDate.now();
    }

}

