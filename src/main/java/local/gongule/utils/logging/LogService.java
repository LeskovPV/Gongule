package local.gongule.utils.logging;

import local.gongule.Gongule;
import local.gongule.tools.data.Data;
import local.gongule.utils.resources.Resources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.commons.io.IOUtils;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class LogService {

    static final Logger logger = LogManager.getRootLogger();

    static final String resourceLogConfig = "xml/log4j2.xml";

    static {
        try { // Create log directory
            Path path = Paths.get(getFullDirName());
            if (!Files.exists(path))
                Files.createDirectories(path);
        } catch (Exception exception) {
            logger.error("Impossible create data directory '{}': {}", getFullDirName(), exception.getMessage());
        }
        Map<String, Object> pageVariables = new HashMap(0);
        pageVariables.put("logfile", getFullName());
        File logConfFile = Resources.getAsFile(resourceLogConfig, getDirName() + "config.xml", pageVariables, false);
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
        return Gongule.projectName + ".log";
    }

    public static String getFullName() {
        return getFullDirName() + getName();
    }

    public static File getAllLogFile() throws IOException{
        String allLogFileName = getFullDirName() + Gongule.projectName + "-log.txt";
        File allLogFile = new File(allLogFileName);
        if (allLogFile.exists())
            allLogFile.delete();
        OutputStream output = null;
        try {
            output = new BufferedOutputStream(new FileOutputStream(allLogFileName, true));
            File file = new File(getFullName() + "-1");
            if (file.exists())
                appendFile(output, new File(getFullName() + "-1"));
            appendFile(output, new File(getFullName()));
        } finally {
            IOUtils.closeQuietly(output);
        }
        return allLogFile;
    }

    private static void appendFile(OutputStream output, File source) throws IOException {
        InputStream input = null;
        try {
            input = new BufferedInputStream(new FileInputStream(source));
            IOUtils.copy(input, output);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }
}
