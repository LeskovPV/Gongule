package local.gongule;

import local.gongule.tools.devices.CoolingRelay;
import local.gongule.tools.process.GongExecutor;
import local.gongule.tools.process.GongSound;
import local.gongule.utils.logging.Loggible;
import local.gongule.utils.resources.Resources;
import local.gongule.utils.ParsableProperties;
import local.gongule.webserver.WebServer;
import local.gongule.windows.MainWindow;
import java.io.FileInputStream;
import java.lang.invoke.MethodHandles;

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
        GongExecutor.init();
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

    /**
     * Return project name
     * Project name is current (main) class name
     **/
    public static String getProjectName() {
        return MethodHandles.lookup().lookupClass().getSimpleName();
    }

    /**
     * Return full project name with version
     **/
    public static String getFullName(){
        return getProjectName() + "-" + getProjectVersion();
    }

    private static String projectWebsite = "https://github.com/LeskovPV/Gongule";

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
        logger.info("Properties is appled");
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
            logger.error("Impossible open config file ({}): {}", Resources.getJarDirName() + cfgFileName, exception);
            return;
        }
        GongSound.setStrikesDelay(properties.getIntegerProperty("gong.strikes_delay"));
        GongExecutor.setAdvanceTime(properties.getIntegerProperty("gong.advance_time"));
        CoolingRelay.getInstance().setRelayTemperature(properties.getDoubleProperty("gong.cpu_temperature"));
        WebServer.setUseHttp(properties.getBooleanProperty("web.use_http"));
        WebServer.setHttpPort(properties.getIntegerProperty("web.http_port"));
        WebServer.setHttpsPort(properties.getIntegerProperty("web.https_port"));
        WebServer.setKeyStore(properties.getProperty("web.key_store"));
        WebServer.setStorePassword(properties.getProperty("web.store_password"));
        WebServer.setManagerPassword(properties.getProperty("web.manager_password"));
        logger.info("Configuration is appled");
    }


}
