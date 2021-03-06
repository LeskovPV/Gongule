package local.gongule.tools;

import local.gongule.Gongule;
import local.gongule.utils.ParsableProperties;
import local.gongule.utils.resources.Resources;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigFile {

    static private String fileName = Resources.getJarDirName() + Gongule.projectName + ".cfg";

    static private ConfigFile instance = new ConfigFile();

    static public ConfigFile getInstance() {
        return instance;
    }

    private ParsableProperties properties = new ParsableProperties();

    /**
     * Constructor
     */
    private ConfigFile() {}

    private void save() {
        save("");
    }
    private void save(String comments) {
        try {
            properties.store(new FileWriter(fileName), "Gongule config\n" + comments);
        } catch (IOException exception) { }
    }

    private void load(){
        try {
            properties.load(new FileReader(fileName));
        } catch (IOException exception) { }
    }

    public void set(String name, String value) {
        properties.setProperty(name, value);
        save();
    }

    public void set(String name, int value) {
        properties.setIntegerProperty(name, value);
        save();
    }

    public void set(String name, double value) {
        properties.setDoubleProperty(name, value);
        save();
    }

    public void set(String name, boolean value) {
        properties.setBooleanProperty(name, value);
        save();
    }

    public String get(String name) {
        load();
        return properties.getProperty(name);
    }

    public String get(String name, String defaultValue) {
        load();
        return properties.containsKey(name) ? properties.getProperty(name) : defaultValue;
    }

    public double get(String name, double defaultValue) {
        load();
        return properties.containsKey(name) ? properties.getDoubleProperty(name, defaultValue) : defaultValue;
    }

    public int get(String name, int defaultValue) {
        load();
        return properties.containsKey(name) ? properties.getIntegerProperty(name, defaultValue) : defaultValue;
    }

    public boolean get(String name, boolean defaultValue) {
        load();
        return properties.containsKey(name) ? properties.getBooleanProperty(name, defaultValue) : defaultValue;
    }

}
