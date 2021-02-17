package local.gongule.utils.logging;

import local.gongule.Gongule;
import local.gongule.tools.data.Data;
import local.gongule.utils.resources.Resources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class LogService{

    static final Logger logger = LogManager.getRootLogger();

    static final String resourceLogConfig = "xml/log4j2.xml";

    static {

        try { // Create log directory
            Path path = Paths.get(getFullDirName());
            if (!Files.exists(path))
                Files.createDirectories(path);
        } catch (Exception exception) {
            logger.error("Impossible create data directory '{}': {}", Data.getFullDirName(), exception.getMessage());
        }
        Map<String, Object> pageVariables = new HashMap(0);
        pageVariables.put("logfile", getFullName());
        File logConfFile = Resources.getAsFile(resourceLogConfig, getDirName() + "config.xml", pageVariables);
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        context.setConfigLocation(logConfFile.toURI());
    }

    public static String getDirName() {
        return "log/";
    }

    public static String getFullDirName() {
        return Resources.getJarDirName() + getDirName();
    }

    public static String getName() {
        return Gongule.getProjectName() + ".log";
    }

    public static String getFullName() {
        return getFullDirName() + getName();
    }

}
