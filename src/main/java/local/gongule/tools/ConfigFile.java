package local.gongule.tools;

import local.gongule.Gongule;
import local.gongule.utils.resources.Resources;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;

public class ConfigFile {

    static private String fileName = Resources.getJarDirName() + Gongule.projectName + ".cfg";

    static private ConfigFile instance = new ConfigFile();

    static public ConfigFile getInstance() {
        return instance;
    }

    private Properties properties = new Properties();

    /**
     * Constructor
     */
    private ConfigFile() {}

    private void save(String comments) {
        try {
            properties.store(new FileWriter(fileName), comments);
        } catch (IOException exception) { }
    }

    private void load(){
        try {
            properties.load(new FileReader(fileName));
        } catch (IOException exception) { }
    }

    public void set(String name, String value) {
        properties.setProperty(name, value);
        save("last change" + LocalDateTime.now().toString());
    }

    public String get(String name) {
        load();
        return properties.getProperty(name);
    }

    public String get(String name, String defaultValue) {
        load();
        return properties.containsKey(name) ? properties.getProperty(name) : defaultValue;
    }

}
