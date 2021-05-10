package local.gongule.utils.resources;

import local.gongule.utils.TemplateFillable;
import local.gongule.utils.logging.LogService;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Map;

// Контейнер методов для работы с ресурсами из jar-файла
public class Resources implements TemplateFillable{

    static public String getJarDirName(){
        // Full path to jar-package
        String jarFileName = Resources.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        // Full path to jar-package directory
        return (new File(jarFileName)).getParent() + "/";
    }

    static public InputStream getAsStream(String resourceName){
        return Resources.class.getResourceAsStream("/local/gongule/resources/" + resourceName);
    }

    static public File getAsFile(String resourceName, String targetName, Map<String, Object> pageVariables, boolean replace) {
        try {
            File targetFile = new File(getJarDirName() + targetName);
            if (!targetFile.exists() || replace) {
                String result = new Resources().fillTemplate(resourceName, pageVariables);
                FileWriter fileWriter = new FileWriter(targetFile.getPath());
                fileWriter.write(result);
                fileWriter.flush();
            }
            return targetFile;
        } catch (Exception exception) {
            return null;
        }
    }

    static public File getAsFile(String resourceName, String targetName, boolean replace){
        try {
            // Полный путь до внешнего файла
            File targetFile = new File(getJarDirName() + targetName);
            // Если таковой файл ещё не существует или его нужно перезаписать,
            if (!targetFile.exists() || replace) {
                // то копируем его из ресурсов, заархивированных внутри jar-файла
                FileOutputStream outputStream = new FileOutputStream(targetFile.getPath());
                outputStream.write(getAsBytes(resourceName));
            }
            return targetFile;
        } catch (Exception exception) {
            return null;
        }
    }

    // Извлекает ресурс resourceName из jar-пакета в файл targetName в тот же каталог, где находится сам пакет
    // Взвращает полный путь до файла targetName, или null если что-то пошло не так
    static public File getAsFile(String resourceName, String targetName) {
        return getAsFile(resourceName, targetName, false);
    }


    // Извлекает ресурс resourceName из jar-пакета в тот же каталог, где находится сам пакет
    // Взвращает полный путь до извлечённого файла, или null если что-то пошло не так
    static public File getAsFile(String resourceName){
        return getAsFile(resourceName, resourceName);
    }

    static public File[] getAsFiles(String resourceName){
       return getAsFiles(resourceName, resourceName);
    }

    static public File[] getAsFiles(String resourceName, String targetName){
        File directory = getAsFile(resourceName, targetName, true);
        ArrayList<File> result = new ArrayList();
        for(File file: directory.listFiles())
            result.add(file);
        return (File[]) result.toArray();
    }

    static public byte[] getAsBytes(String resourceName){
        try {
            InputStream inputStream = getAsStream(resourceName);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        } catch (IOException exception) {
            return new byte[0];
        }
    }

    static public Image getAsImage(String resourceName) {
        try {
            return new ImageIcon(
                    ImageIO.read(
                            getAsStream(resourceName)
                    )
            ).getImage();
        } catch (Exception exception) {
            //Journal.add(NoteType.ERROR, "?get_resource", resourceName, exception.getMessage());
            return null;
        }
    }

}
