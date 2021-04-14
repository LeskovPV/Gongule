package local.gongule;

import local.gongule.tools.process.GongExecutor;
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
    public static void main(String[] args) {
        logger.warn("________________________________");
        logger.warn("Gongule is started on {} {}", SystemUtils.osName, SystemUtils.osRelease);
        applyProperties();
        WebServer.start();
        MainWindow.open(getFullProjectName());
        GongExecutor.init();
    }

    /**
     * Project name is current (main) class name - Gongule
     **/
    public static final String projectName = MethodHandles.lookup().lookupClass().getSimpleName();

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
     * Return full project name with version
     **/
    public static String getFullProjectName(){
        return projectName + "-" + getProjectVersion();
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
        setProjectVersion(properties.getProperty("project.version"));

        WebServer.setUseHttp(properties.getBooleanProperty("http.use"));
        WebServer.setHttpPort(properties.getIntegerProperty("http.port"));
        WebServer.setHttpsPort(properties.getIntegerProperty("https.port"));
        WebServer.setKeyStoreFile(properties.getProperty("key.storefile"));
        WebServer.setKeyStorePassword(properties.getProperty("key.storepassword"));
        WebServer.setKeyManagerPassword(properties.getProperty("key.managerpassword"));

        logger.info("Properties is appled");
    }

}
