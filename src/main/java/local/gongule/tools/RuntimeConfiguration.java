package local.gongule.tools;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;

public class RuntimeConfiguration {

    private String fileName;

    private Properties properties = new Properties();

    public RuntimeConfiguration(String fileName){
        this.fileName = fileName;
    }

    private void save(String comments) {
        try {
            properties.store(new FileWriter(fileName), "");
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
        return properties.contains(name) ? properties.getProperty(name) : defaultValue;
    }

}
