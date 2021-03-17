package local.gongule.tools.data;

import com.thoughtworks.xstream.XStream;
import local.gongule.Gongule;
import local.gongule.utils.formatter.DateFormatter;
import local.gongule.utils.logging.Loggible;
import local.gongule.utils.formatter.TimeFunctions;
import local.gongule.utils.resources.Resources;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.chrono.ChronoLocalDate;
import java.util.*;
import java.time.*;

import static java.time.temporal.ChronoUnit.DAYS;

public class Data implements Serializable, Loggible {

    private Data() {}

    public class Note implements Serializable {
        public LocalDate date;
        public int courseIndex;

        public Note(int courseIndex, LocalDate date) {
            this.courseIndex = courseIndex;
            this.date = date;
        }

        public LocalDate getEndDate() {
            return date.plusDays(courses.get(courseIndex).dayIndexes.size() - 1);
        }

        public Course getCourse() {
            return courses.get(courseIndex);
        }
    }

    private List<Gong> gongs = new ArrayList(0);
    private List<Day> days = new ArrayList(0);
    private List<Course> courses = new ArrayList(0);
    public List<Note> calendar = new ArrayList(0);

    public int getCurrentNoteIndex() {
        LocalDate today = LocalDate.now();
        for (Note note : calendar)
            if (note.date.isBefore(today.plusDays(1)) && (note.getEndDate().isAfter(today.minusDays(1))))
                return calendar.indexOf(note);
        return -1;
    }

    public String getCurrentCourseName() {
        int currentNoteIndex = getCurrentNoteIndex();
        if (currentNoteIndex < 0)
            return "";
        return courses.get(calendar.get(currentNoteIndex).courseIndex).name;
    }

    public int getCurrentDayNumber() {
        int currentNoteIndex = getCurrentNoteIndex();
        if (currentNoteIndex < 0)
            return -1;
        ChronoLocalDate today = LocalDate.now();
        ChronoLocalDate begin = calendar.get(currentNoteIndex).date;
        return (int) DAYS.between(begin, today);
    }

    public int getCurrentDayIndex() {
        int currentNoteIndex = getCurrentNoteIndex();
        if (currentNoteIndex < 0)
            return -1;
        LocalDate today = LocalDate.now();
        Note note = calendar.get(currentNoteIndex);
        int courseIndex = note.courseIndex;
        int daysBetween = (int) DAYS.between(note.date, today);
        if ((daysBetween < 0) || (daysBetween >= courses.get(courseIndex).dayIndexes.size()))
            return -1;
        return courses.get(courseIndex).dayIndexes.get(daysBetween);
    }

    ////////////////////////////////////////////////////////////////
    // Calendar
    ////////////////////////////////////////////////////////////////

    public boolean addCalendarNote(int courseIndex, LocalDate courseDate) {
        int newNoteIndex = 0;
        Note newNote = new Note(courseIndex, courseDate);
        for (int noteIndex = 0; noteIndex < calendar.size(); noteIndex++) {
            if (calendar.get(noteIndex).getEndDate().isBefore(newNote.date)) {
                newNoteIndex = noteIndex + 1;
                continue;
            }
            if (calendar.get(noteIndex).date.isAfter(newNote.getEndDate())) {
                newNoteIndex = noteIndex;
                break;
            }
            calendar.remove(noteIndex);
            noteIndex--;
            newNoteIndex = calendar.size();
        }
        calendar.add(newNoteIndex, newNote);
        save();
        logger.info("Added '{}' into calendar at {}", newNote.getCourse().name, newNote.date.format(DateFormatter.get()));
        return true;
    }

    public boolean removeCalendarNote(int noteIndex) {
        if (noteIndex < 0) return false;
        if (noteIndex > calendar.size() - 1) return false;
        Note note = calendar.get(noteIndex);
        logger.info("Removed '{}' (at {}) from calendar", note.getCourse().name, note.date.format(DateFormatter.get()));
        calendar.remove(noteIndex);
        save();
        return true;
    }

    ////////////////////////////////////////////////////////////////
    // Course
    ////////////////////////////////////////////////////////////////

    public int getCoursesAmount() {
        return courses.size();
    }

    public Course getCourse(int index) {
        try {
            return courses.get(index);
        } catch (Exception exception) {
            return null;
        }
    }

    public boolean courseCreate(String name) {
        if (name == null) return false;
        if (name.trim().equals("")) return false;
        for (Course course: courses)
            if (course.name.equalsIgnoreCase(name.trim()))
                return false;
        courses.add(new Course(name.trim()));
        save();
        logger.info("Created course '{}'", name.trim());
        return true;
    }

    public boolean courseDelete(int courseIndex) {
        if (courseIndex < 0) return false;
        if (courseIndex > courses.size() - 1) return false;
        List<Integer> indexes = new ArrayList();
        for (Note note: calendar) {
            if (note.courseIndex == courseIndex)
                indexes.add(0, calendar.indexOf(note));
        }
        for (int index: indexes)
            calendar.remove(index);
        for (int index = 0; index < calendar.size(); index++)
            if (calendar.get(index).courseIndex > courseIndex)
                calendar.get(index).courseIndex = calendar.get(index).courseIndex - 1;
        logger.info("Deleted course '{}'", courses.get(courseIndex).name);
        courses.remove(courseIndex);
        save();
        return true;
    }

    ////////////////////////////////////////////////////////////////
    // Schedule (CourseDays)
    ////////////////////////////////////////////////////////////////

    public int getCourseDaysAmount(int courseIndex) {
        return (courses.size() == courseIndex) ? 0 : courses.get(courseIndex).dayIndexes.size();
    }

    public Day getCourseDay(int courseIndex, int dayIndex) {
        try {
            return days.get(courses.get(courseIndex).dayIndexes.get(dayIndex));
        } catch (Exception exception) {
            return null;
        }
    }

    public boolean createCourseDay(int courseIndex, int dayIndex) {
        courses.get(courseIndex).dayIndexes.add(dayIndex);
        save();
        return true;
    }

    public boolean removeCourseDay(int courseIndex, int dayIndex) {
        courses.get(courseIndex).dayIndexes.remove(dayIndex);
        save();
        return true;
    }

    ////////////////////////////////////////////////////////////////
    // Days
    ////////////////////////////////////////////////////////////////

    public int getDaysAmount() {
        return days.size();
    }

    public Day getDay(int index) {
        try {
            if ((0 > index) || (index > getDaysAmount())) {
                if (getDaysAmount() > 0)
                    index = 0;
                else
                    return null;
            }
            return days.get(index);
        } catch (Exception exception) {
            return null;
        }
    }

    public boolean dayDelete(int dayIndex) {
        for (Course course: courses) {
            List<Integer> indexes = new ArrayList();
            for (int index: course.dayIndexes)
                if (index == dayIndex)
                    indexes.add(0, course.dayIndexes.indexOf(index));
            for (int index: indexes)
                course.dayIndexes.remove(index);
            for (int index = 0; index < course.dayIndexes.size(); index++)
                if (course.dayIndexes.get(index) > dayIndex)
                    course.dayIndexes.set(index, course.dayIndexes.get(index) - 1);
        }
        days.remove(dayIndex);
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
    // Timetable (DayEvents)
    ////////////////////////////////////////////////////////////////

    public int getDayEventsAmount(int dayIndex) {
        Day day = getDay(dayIndex);
        return (day == null) ? 0 : day.events.size();
    }

    public Day.Event getDayEvent(int dayIndex, int index) {
        try {
            Day day = getDay(dayIndex);
            if (day == null) return null;
            return ((0 > index) || (index > day.events.size())) ? null : day.events.get(index);
        } catch (Exception exception) {
            return null;
        }
    }

    public boolean addDayEvent(int dayIndex, LocalTime eventTime, String eventName, int gongIndex) {
        if (gongIndex < 0)
            return false;
        int minutes = 5;
        Day.Event dayEvent = new Day.Event(eventTime, eventName, gongIndex);
        int newEventIndex = 0;
        if (TimeFunctions.isAfterMidnight(eventTime, minutes))
            for (int eventIndex = days.get(dayIndex).events.size() - 1; eventIndex >= 0; eventIndex--)
                if (TimeFunctions.isBeforeMidnight(days.get(dayIndex).events.get(eventIndex).time, minutes))
                    days.get(dayIndex).events.remove(eventIndex);
        for (int eventIndex = 0; eventIndex < days.get(dayIndex).events.size(); eventIndex++) {
            newEventIndex = eventIndex;
            if (TimeFunctions.isNear(days.get(dayIndex).events.get(eventIndex).time, eventTime, minutes)) {
                days.get(dayIndex).events.remove(eventIndex);
                break;
            }
            if (days.get(dayIndex).events.get(eventIndex).time.isAfter(eventTime))
                break;
            newEventIndex++;
        }
        if (TimeFunctions.isBeforeMidnight(eventTime, minutes))
            days.get(dayIndex).events.add(dayEvent);
        else
            days.get(dayIndex).events.add(newEventIndex, dayEvent);
        save();
        return true;
    }

    public boolean removeDayEvent(int dayIndex, int eventIndex) {
        days.get(dayIndex).events.remove(eventIndex);
        save();
        return true;
    }

    ////////////////////////////////////////////////////////////////
    // Gongs
    ////////////////////////////////////////////////////////////////

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

    ////////////////////////////////////////////////////////////////
    // Static
    ////////////////////////////////////////////////////////////////

    private static Data instance = load();

    public static Data getInstance() {
        return instance;
    }

    public static boolean setInstance(Data data) {
        if (data == null) return false;
        instance = data;
        return true;
    }

    public static boolean save(Data data) {
        return save(data, null);
    }

    public static boolean save(Data data, String fileName) {
        if (defaultName.equalsIgnoreCase(fileName)) {
            //Log.printWarn("Impossible save configuration as '" + defaultName + "'. It is reserved filename");
            return false;
        }
        String fullFileName = ((fileName == null) ? getFullCurrentName() : getFullAnyName(fileName));
        XStream xstream = new XStream();
        try {
            xstream.toXML(data, new FileWriter(fullFileName));
            return true;
        } catch(Exception exception) {
            logger.error("Impossible save to {}: {}", fullFileName, exception);
            return false;
        }
    }

    public static Data load() {
        return load(null);
    }

    public static Data load(String fileName) {
        String fullFileName = "";
        try { // Create data directory
            if (fileName != null)
                fullFileName = getFullAnyName(fileName);
            else {
                //Path path = Paths.get(Resources.getJarDirName() + Gongule.projectName + ".xml");
                fullFileName = Files.exists(Paths.get(getFullCurrentName())) ? getFullCurrentName() : getFullDefaultName();
                Path path = Paths.get(Data.getFullDirName());
                if (!Files.exists(path))
                    Files.createDirectories(path);

                path = Paths.get(getFullDefaultName());
                if (!Files.exists(path)) // Create default configuration
                    Resources.getAsFile("xml/data.xml", dirName + defaultName + ".xml", true); // Extract cfg-file from jar-package to jar-directory
                    // Load current configuration
            }
            Data result = (Data) new XStream().fromXML(new File(fullFileName));
            if (fullFileName.equalsIgnoreCase(getFullDefaultName()))
                if (result.calendar.size() > 0)
                    result.calendar.get(0).date = LocalDate.now().minusDays(1);
            return result;
        } catch(Exception exception) {
            logger.error("Impossible load '{}' configuration: {}", fullFileName, exception);
            return null;
        }

    }

    public static boolean detete(String fileName) {
        if (fileName.equalsIgnoreCase(defaultName))
            return false;
        File file = new File(getFullAnyName(fileName));
        return file.delete();
    }

    public static final String defaultName = "default";

    private static final String dirName = "data/";

    private static String getFullDefaultName() {
        return getFullDirName() + defaultName + ".xml";
    }

    private static String getFullCurrentName() {
        return Resources.getJarDirName() + Gongule.projectName + ".xml";
    }

    private static String getFullAnyName(String anyName) {
        return getFullDirName() + anyName + ".xml";
    }

    private static String getFullDirName() {
        return Resources.getJarDirName() + dirName;
    }

    public static List<String> getFiles() {
        List<String> result = new ArrayList();
        File directory = new File(getFullDirName());
        for(File file: directory.listFiles())
            result.add(file.getName().substring(0, file.getName().length() - 4));
        return result;
    }

}
