package local.gongule.webserver.servlets.content;

import local.gongule.utils.colors.ColorSchema;
import local.gongule.utils.formatter.DateFormatter;
import local.gongule.utils.formatter.TimeFormatter;
import local.gongule.tools.data.Course;
import local.gongule.tools.data.Data;
import local.gongule.tools.data.Day;
import local.gongule.tools.process.GongExecutor;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class ControlContent extends Content {

    public ControlContent() {
        actions.put("add_note", (HttpServletRequest request) -> addNote(request));
        actions.put("remove_note", (HttpServletRequest request) -> removeNote(request));
        actions.put("run_process", (HttpServletRequest request) -> runProcess(request));
        actions.put("pause_process", (HttpServletRequest request) -> pauseProcess(request));
    }

    public String get(HttpServletRequest request) {
        Map<String, Object> contentVariables = new HashMap();
        String rows = "";
        Data data = Data.getInstance();
        for (int i = 0; i < data.calendar.size(); i++) {
            Map<String, Object> piecesVariables = new HashMap();
            Data.Note note = data.calendar.get(i);
            Course course = data.getCourse(note.courseIndex);
            piecesVariables.put("begin", note.date.format(DateFormatter.get()));
            piecesVariables.put("name", course.name);
            piecesVariables.put("end", note.getEndDate().format(DateFormatter.get()));
            piecesVariables.put("color", note.date.isBefore(LocalDate.now().plusDays(1)) && note.getEndDate().isAfter(LocalDate.now().minusDays(1)) ? ColorSchema.getInstance().getTextColor() : ColorSchema.getInstance().getHalfColor());
            piecesVariables.put("value", i);
            rows += fillTemplate("html/pieces/note.html", piecesVariables) + "\n";
        }
        contentVariables.put("calendar_notes", rows);
        String options = "";
        int courseIndex = Integer.valueOf(getAttribute(request, "select_course", "0"));
        if ((0 > courseIndex) || (courseIndex > data.getCoursesAmount())) courseIndex = 0;
        for (int i = 0; i < data.getCoursesAmount(); i++) {
            Map<String, Object> piecesVariables = new HashMap();
            piecesVariables.put("value", i);
            piecesVariables.put("selected", (i == courseIndex) ? "selected" : "");
            piecesVariables.put("caption", data.getCourse(i).name);
            options += fillTemplate("html/pieces/option.html", piecesVariables) + "\n";
        }
        int todayIndex = data.getCurrentDayIndex();
        contentVariables.put("course_options", options);
        contentVariables.put("today_date", LocalDate.now().format(DateFormatter.get()));
        contentVariables.put("today_day", (todayIndex >= 0) ? data.getDay(todayIndex).name : "");
        contentVariables.put("today_index", data.getCurrentDayNumber());
        contentVariables.put("today_course", data.getCurrentCourseName());
        contentVariables.put("today_display", (todayIndex < 0) ? "none" : "table-row");
        rows = "";
        if (todayIndex >= 0)
            for (Day.Event event: data.getDay(todayIndex).events) {
                Map<String, Object> piecesVariables = new HashMap();
                piecesVariables.put("color", event.time.isAfter(LocalTime.now()) ? ColorSchema.getInstance().getTextColor() : ColorSchema.getInstance().getHalfColor());
                piecesVariables.put("time", event.time.format(TimeFormatter.get()));
                piecesVariables.put("gong", data.getGong(event.gongIndex).name);
                piecesVariables.put("name", event.name);
                piecesVariables.put("value", -1);
                piecesVariables.put("remove_display", "none");
                rows += fillTemplate("html/pieces/event.html", piecesVariables) + "\n";
            }
        contentVariables.put("run_disabled", GongExecutor.processIsPaused() ? "" : "disabled");
        contentVariables.put("pause_disabled", GongExecutor.processIsPaused() ? "disabled" : "");

        contentVariables.put("events_display", rows.isEmpty() ? "none" : "table-row");
        contentVariables.put("noevents_display", rows.isEmpty() ? "table-row" : "none");
        contentVariables.put("today_events", rows);
        contentVariables.put("date_pattern", DateFormatter.pattern);
        contentVariables.put("date_size", DateFormatter.getSize());
        return super.getFromTemplate(contentVariables);
    }

    private boolean addNote(HttpServletRequest request) {
        try {
            int courseIndex = Integer.valueOf(request.getParameter("select_course"));
            LocalDate courseDate = LocalDate.parse(request.getParameter("course_date"), DateFormatter.get());
            setAttribute(request, "select_course", String.valueOf(courseIndex));
            Data data = Data.getInstance();
            if (!data.addCalendarNote(courseIndex, courseDate)) return false;
            logger.info("Added course '{}' to calendar on ", data.getCourse(courseIndex).name, courseDate.format(DateFormatter.get()));
            GongExecutor.reset();
            return true;
        } catch (Exception exception) {
            logger.error("Impossible add course to calendar: {}", exception);
            return false;
        }
    }

    private boolean removeNote(HttpServletRequest request){
        try {
            int noteIndex = Integer.valueOf(request.getParameter("remove_note"));
            Data data = Data.getInstance();
            String courseName = data.calendar.get(noteIndex).getCourse().name;
            LocalDate courseDate = data.calendar.get(noteIndex).date;
            if (!data.removeCalendarNote(noteIndex));
            logger.info("Removed course '{}' to calendar on ", courseName, courseDate.format(DateFormatter.get()));
            GongExecutor.reset();
            return true;
        } catch (Exception exception) {
            logger.error("Impossible remove course from calendar: {}", exception);
            return false;
        }
    }

    private boolean runProcess(HttpServletRequest request) {
        GongExecutor.run();
        return true;
    }

    private boolean pauseProcess(HttpServletRequest request) {
        GongExecutor.pause();
        return true;
    }

}
