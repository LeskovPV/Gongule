package local.gongule;

import com.pi4j.io.gpio.RaspiPin;
import local.gongule.tools.*;
import local.gongule.tools.data.Data;
import local.gongule.tools.data.Gong;
import local.gongule.tools.devices.CoolingDevice;
import local.gongule.tools.process.GongSound;
import local.gongule.utils.logging.Loggible;
import local.gongule.utils.resources.Resources;
import local.gongule.utils.ParsableProperties;
import local.gongule.webserver.WebServer;
import local.gongule.windows.MainWindow;
import java.io.File;
import java.io.FileInputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main class of gong schedule
 */
public class Gongule implements Loggible {

    /**
     *  Point of entry
     */
    public static void main(String[] args) {
        logger.warn("Gongule is started");
        applyProperties();
        applyCongiguration();
        WebServer.start();
        MainWindow.open(getProjectName());
    }

    public static RuntimeConfiguration runtimeConfiguration;

    /**
     * Return project name
     * Project name is current (main) class name
     **/
    public static String getProjectName() {
        return MethodHandles.lookup().lookupClass().getSimpleName();
    }

    /**
     * Apply properties from jar-package resources
     */
    private static void applyProperties() {
        ParsableProperties properties = new ParsableProperties();
        try {
            properties.load(Resources.getAsStream("properties")); // load properties from jar-package resources
        } catch (Exception exception) {
            logger.error("Unpossible apply properties", exception);
            System.exit(0);
        }
        setProjectVersion(properties.getProperty("gongule.version"));
        setProjectWebsite(properties.getProperty("gongule.website"));
        runtimeConfiguration = new RuntimeConfiguration(Resources.getJarDirName() + getProjectName() + ".tmp");
        logger.info("Properties is appled");
    }

    private static String projectWebsite = "https://www.google.com/search?q=" + getProjectName();

    /**
     * Return project version
     **/
    public static String getProjectWebsite() {
        return projectWebsite;
    }

    /**
     * Set project version
     **/
    public static void setProjectWebsite(String value) {
        if (value == null) return;
        if (value.trim().isEmpty()) return;
        projectWebsite = value.trim();
    }

    /**
     * Project version
     * Default value assign here
     * Real value assign from properties file in applyProperties method
     **/
    private static String projectVersion = "0.00";

    /**
     * Return project version
     **/
    public static String getProjectVersion() {
        return projectVersion;
    }

    /**
     * Set project version
     **/
    public static void setProjectVersion(String value) {
        if (value == null) return;
        if (value.trim().isEmpty()) return;
        projectVersion = value.trim();
    }

    private static File gongFile = null;

    public static File getGongFile() {
        if (gongFile == null)
            // Extract wav-file from jar-package to jar-directory
            return Resources.getAsFile("wav/gong.wav", getProjectName() + ".wav", false);
        else
            return gongFile;
    }

    private static Data data = null;

    public static boolean setData(Data newdata) {
        if (newdata != null)
            data = newdata;
        return (newdata != null);
    }

    public static Data getData() {
        if (data == null) {
            try { // Create data directory
                Path path = Paths.get(Data.getFullDirName());
                if (!Files.exists(path))
                    Files.createDirectories(path);
            } catch (Exception exception) {
                logger.error("Impossible create data directory '{}': {}", Data.getFullDirName(), exception);
            }
            // Create default configuration
            Resources.getAsFile("xml/data.xml", Data.getDirName() + Data.getDefaultName() + ".xml", true); // Extract cfg-file from jar-package to jar-directory
            // Load current configuration
            data = Data.load();
            if (data == null)
                data = new Data();
            data.save();
        }
        return  data;
    }

    /**
     * Return full project name with version
     **/
    public static String getFullName1(){
        return getProjectName() + "-" + getProjectVersion();
    }

    /**
     * Load congigure from cfg-file
     */
    private static void applyCongiguration(){
        ParsableProperties properties = new ParsableProperties();
        String jarCfgName = "cfg/pattern.cfg"; // default cfg-file in jar-package resources
        String cfgFileName = getProjectName() + ".cfg"; // specific cfg-file in jar-directory
        Resources.getAsFile(jarCfgName, cfgFileName, false); // Extract cfg-file from jar-package to jar-directory
        try {
            FileInputStream inputStream = new FileInputStream(Resources.getJarDirName() + cfgFileName);
            properties.load(inputStream);
        }catch (Exception exception){
            logger.error("Impossible apply configuration: {}", exception);
            return;
        }
        //properties.store();
        GongSound.setDelay(properties.getIntegerProperty("gong.delay"));


        WebServer.setHttpPort(properties.getIntegerProperty("web.http_port"));
        WebServer.setHttpsPort(properties.getIntegerProperty("web.https_port"));
        WebServer.setUseHttp(properties.getBooleanProperty("web.use_http"));
        WebServer.setKeyStore(properties.getProperty("web.key_store"));
        WebServer.setStorePassword(properties.getProperty("web.store_password"));
        WebServer.setManagerPassword(properties.getProperty("web.manager_password"));

        int fanPin = properties.getIntegerProperty("pi.fan_pin", -1);
        double cpuTemperature = properties.getDoubleProperty("pi.cpu_temperature", 50);
        CoolingDevice coolingDevice = new CoolingDevice(RaspiPin.getPinByAddress(fanPin), cpuTemperature);

        logger.info("Configuration is appled");
    }

}
