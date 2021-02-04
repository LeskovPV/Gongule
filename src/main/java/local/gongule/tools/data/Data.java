package local.gongule.tools.data;

import com.thoughtworks.xstream.XStream;
import local.gongule.Gongule;
import local.gongule.tools.Log;
import local.gongule.tools.resources.Resources;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.time.*;

public class Data implements Serializable {

    //private static final long serialVersionUID = -519958441268523608L;
    public class Event implements Serializable {
        public LocalDate date = LocalDate.now();
        public Course course = new Course();
    }

    private List<Gong> gongs = new ArrayList(0);
    public List<Day> days = new ArrayList(0);
    public List<Course> courses = new ArrayList(0);
    public List<Event> calendar = new ArrayList(0);

    public Map<Date, Gong> getTodayEvent() {
        Map<Date, Gong> result = new HashMap(0);
        return result;
    }

    ////////////////////////////////////////////////////////////////
    // Days
    ////////////////////////////////////////////////////////////////

    public int getDaysAmount() {
        return days.size();
    }

    public Day getDay(int index) {
        try {
            return days.get(index);
        } catch (Exception exception) {
            return null;
        }
    }

    public boolean dayDelete(int index) {
        for (Course course: courses)
            for (Day day: course.schedule)
                if (day == days.get(index))
                    course.schedule.remove(index);
        days.remove(index);
        save();
        return true;
    }

    public boolean dayCreate(String name) {
        if (name == null) return false;
        if (name.trim().equals("")) return false;
        for (Day day: days)
            if (day.name.equalsIgnoreCase(name.trim()))
                return false;
        days.add(new Day(name.trim()));
        save();
        return true;
    }

    ////////////////////////////////////////////////////////////////
    // Events
    ////////////////////////////////////////////////////////////////

    public int getDayEventsAmount(int dayIndex) {
        return (days.size() == 0) ? 0 : days.get(dayIndex).events.size();
    }

    public Day.Event getDayEvent(int dayIndex, int index) {
        try {
            return days.get(dayIndex).events.get(index);
        } catch (Exception exception) {
            return null;
        }
    }

    public boolean createDayEvent(int dayIndex, LocalTime time, String name, int gongIndex) {
        //int gongIndex = getGongIndex(gongName);
        if (gongIndex < 0)
            return false;
        Day.Event dayEvent = new Day.Event(time, name, gongIndex);
        days.get(dayIndex).events.add(dayEvent);
        save();
        return true;
    }

    public boolean deleteDayEvent(int dayIndex, int eventIndex) {
        days.get(dayIndex).events.remove(eventIndex);
        save();
        return true;
    }

    ////////////////////////////////////////////////////////////////
    // Gongs
    ////////////////////////////////////////////////////////////////

    public int getGongIndex(String name) {
        for (Gong gong: gongs)
            if (gong.name.equalsIgnoreCase(name))
                return gongs.indexOf(gong);
        return -1;
    }

    public int getGongsAmount() {
        return gongs.size();
    }

    public Gong getGong(int index) {
        try {
            return gongs.get(index);
        } catch (Exception exception) {
            return null;
        }
    }

    public boolean gongDelete(int gongIndex) {
        for (Day day: days) {
            List<Integer> indexes = new ArrayList();
            for (Day.Event dayEvent: day.events)
                if (dayEvent.gongIndex == gongIndex)
                    indexes.add(0, day.events.indexOf(dayEvent));
            for (int index: indexes)
                day.events.remove(index);
            for (Day.Event dayEvent: day.events)
                if (dayEvent.gongIndex > gongIndex)
                    dayEvent.gongIndex = dayEvent.gongIndex - 1;
        }
        gongs.remove(gongIndex);
        save();
        return true;
    }

    public boolean gongPlay(int index) {
        gongs.get(index).play();
        return true;
    }

    public boolean gongCreate(String name, int amount) {
        if (name == null) return false;
        if (name.trim().equals("")) return false;
        for (Gong gong: gongs)
            if (gong.name.equalsIgnoreCase(name.trim()))
                return false;
        gongs.add(new Gong(name.trim(), amount));
        save();
        return true;
    }

    ////////////////////////////////////////////////////////////////
    // File system
    ////////////////////////////////////////////////////////////////

    public boolean save() {
        return save(this, null);
    }

    public boolean save(String fileName) {
        return save(this, fileName);
    }

    public static boolean save(Data data) {
        return save(data, null);
    }

    public static boolean save(Data data, String fileName) {
        if (getDefaultName().equalsIgnoreCase(fileName)) {
            //Log.printWarn("Impossible save configuration as '" + defaultName + "'. It is reserved filename");
            return false;
        }
        String fullFileName = ((fileName == null) ? Resources.getJarDirName() + Gongule.getFullName() : getFullDirName() + fileName) + ".xml";
        XStream xstream = new XStream();
        try {
            xstream.toXML(data, new FileWriter(fullFileName));
            return true;
        } catch(Exception exception) {
            Log.printError("Impossible save to " + fullFileName, exception);
            return false;
        }
    }

    public static Data load() {
        return load(null);
    }

    public static Data load(String fileName) {
        XStream xstream = new XStream();
        String fullFileName = getFullDirName() + fileName + ".xml";
        if (fileName == null) {
            Path path = Paths.get(Resources.getJarDirName() + Gongule.getFullName() + ".xml");
            fullFileName = Files.exists(path) ? path.toString() : getFullDirName() + getDefaultName() + ".xml";
        }
        try {
            return (Data) xstream.fromXML(new File(fullFileName));
        } catch(Exception exception) {
            Log.printError("Impossible load '" + fullFileName + "' configuration", exception);
            return null;
        }
    }

    public static boolean detete(String fileName) {
        if (fileName.equalsIgnoreCase(getDefaultName()))
            return false;
        File file = new File(getFullDirName() + fileName + ".xml");
        return file.delete();
    }

    public static String getDefaultName() {
        return "default";
    }

    public static String getDirName() {
        return "data/";
    }

    public static String getFullDirName() {
        return Resources.getJarDirName() + getDirName();
    }

    public static List<String> getFiles() {
        List<String> result = new ArrayList();
        File directory = new File(getFullDirName());
        for(File file: directory.listFiles())
            result.add(file.getName().substring(0, file.getName().length() - 4));
        return result;
    }

}
