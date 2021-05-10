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

    static public final String osName = System.getProperty("os.name");
    static public final String osVersion = System.getProperty("os.version");
    static public final String osArchitecture = System.getProperty("os.arch");
    static public final String osRelease = getOSRelease();
    static public final boolean isRaspbian = isRaspbian();

    static private String getOSRelease() {
        if (isLinux())
            try (FileInputStream fileInputStream = new FileInputStream("/etc/os-release")) {
                Properties properties = new Properties();
                properties.load(fileInputStream);
                return properties.getProperty("ID");
            } catch (Exception exception) { }
        return "";
    }

    static private boolean isRaspbian() {
        return osRelease.equalsIgnoreCase("raspbian");
    }

    static public boolean isLinux() {
        String os = osName.toLowerCase();
        return os.contains("nix") || os.contains("nux") || os.contains("aix");
    }

    static public boolean isWindows() {
        String os = osName.toLowerCase();
        return os.contains("win");
    }

    static public boolean isMacos() {
        String os = osName.toLowerCase();
        return os.contains("mac") || os.contains("darwin");
    }

    static public boolean isSunos() {
        String os = osName.toLowerCase();
        return os.contains("sunos");
    }

    static public double getCPUTemperature(double defaultValue){
        if (isRaspbian())
            try (FileInputStream fstream = new FileInputStream("/sys/class/thermal/thermal_zone0/temp")) {
                BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                return Integer.parseInt(br.readLine())/1000d;
            } catch (Exception exception) {}
        return defaultValue;
    }

    static public boolean reboot() {
        return shutdown(true);
    }

    static public boolean shutdown() {
        return shutdown(false);
    }

    static private boolean shutdown(boolean reboot) {
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

    static public LocalDate setDateTime(LocalDate date, LocalTime time) {
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
            logger.warn("Changed date and time. New value is {} {}", date.format(DateFormatter.get()), time.format(TimeFormatter.get(true)));
        } catch (Exception exception) {
            logger.error("Impossible set datetime: {}", exception.getMessage());
        }
        return LocalDate.now();
    }

}

