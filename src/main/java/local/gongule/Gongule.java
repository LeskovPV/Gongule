package local.gongule;

import local.gongule.tools.ConfigFile;
import local.gongule.tools.process.GongExecutor;
import local.gongule.tools.process.GongSound;
import local.gongule.tools.relays.CoolingRelay;
import local.gongule.tools.relays.PowerRelay;
import local.gongule.utils.logging.LogService;
import local.gongule.utils.logging.Loggible;
import local.gongule.utils.resources.Resources;
import local.gongule.utils.ParsableProperties;
import local.gongule.utils.system.SystemUtils;
import local.gongule.webserver.WebServer;
import local.gongule.windows.MainWindow;
import java.lang.invoke.MethodHandles;

/**
 * Main class of gong schedule
 */
public class Gongule implements Loggible {

    /**
     *  Point of entry
     */
    static public void main(String[] args) {
        logger.warn("________________________________");
        logger.warn("Gongule is started on {} {}", SystemUtils.osName, SystemUtils.osRelease);
        logger.info("Relay init: cooling turn-{}", CoolingRelay.getInstance().get() ? "off" : "on");
        logger.info("Relay init: audio-amplifier power turn-{}", PowerRelay.getInstance().get() ? "on": "off");
        applyProperties();
        WebServer.start();
        MainWindow.open(getFullProjectName());
        GongExecutor.init();
    }

    /**
     * Project name is current (main) class name - Gongule
     **/
    static public final String projectName = MethodHandles.lookup().lookupClass().getSimpleName();

    /**
     * Project version
     * Default value assign here
     * Real value assign from properties file in applyProperties method
     **/
    static private String projectVersion = "0.00";

    /**
     * Return project version
     **/
    static public String getProjectVersion() {
        return projectVersion;
    }

    /**
     * Set project version
     **/
    static public void setProjectVersion(String value) {
        if (value == null) return;
        if (value.trim().isEmpty()) return;
        projectVersion = value.trim();
    }

    /**
     * Return full project name with version
     **/
    static public String getFullProjectName(){
        return projectName + "-" + getProjectVersion();
    }

    /**
     * Apply properties from jar-package resources
     */
    static private void applyProperties() {
        ParsableProperties properties = new ParsableProperties();
        try {
            properties.load(Resources.getAsStream("properties")); // load properties from jar-package resources
        } catch (Exception exception) {
            logger.error("Unpossible apply properties", exception);
            System.exit(0);
        }

        Gongule.setProjectVersion(properties.getProperty("project.version"));
        WebServer.setUseHttp(properties.getBooleanProperty("http.use"));
        WebServer.setHttpPort(properties.getIntegerProperty("http.port"));
        WebServer.setHttpsPort(properties.getIntegerProperty("https.port"));
        WebServer.setKeyStoreFile(properties.getProperty("key.storefile"));
        WebServer.setKeyStorePassword(properties.getProperty("key.storepassword"));
        WebServer.setKeyManagerPassword(properties.getProperty("key.managerpassword"));

        ConfigFile configFile = ConfigFile.getInstance();
        if (!getProjectVersion().equals(configFile.get("projectVersion"))) { // if version is changed
            LogService.updateCfgFile();
            WebServer.updateKeyStoreFile();
            GongSound.updateGongFile();
            configFile.set("projectVersion", getProjectVersion());
        }

        logger.info("Properties is appled");
    }

}
