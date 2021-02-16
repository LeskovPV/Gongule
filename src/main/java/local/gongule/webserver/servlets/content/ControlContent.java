package local.gongule.webserver.servlets.content;

import local.gongule.Gongule;
import local.gongule.tools.formatter.DateFormatter;
import local.gongule.tools.Log;
import local.gongule.tools.formatter.TimeFormatter;
import local.gongule.tools.data.Course;
import local.gongule.tools.data.Data;
import local.gongule.tools.data.Day;
import local.gongule.tools.process.GongExecutor;
import local.gongule.tools.process.GongTask;
import local.gongule.webserver.WebServer;

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
        for (int i = 0; i < Gongule.getData().calendar.size(); i++) {
            Map<String, Object> piecesVariables = new HashMap();
            Data.Note note = Gongule.getData().calendar.get(i);
            Course course = Gongule.getData().getCourse(note.courseIndex);
            piecesVariables.put("begin", note.date.format(DateFormatter.get()));
            piecesVariables.put("name", course.name);
            piecesVariables.put("end", note.getEndDate().format(DateFormatter.get()));
            piecesVariables.put("color", note.date.isBefore(LocalDate.now().plusDays(1)) && note.getEndDate().isAfter(LocalDate.now().minusDays(1)) ? WebServer.getColorSchema().getTextColor() : WebServer.getColorSchema().getHalfColor());
            piecesVariables.put("value", i);
            rows += fillTemplate("html/pieces/note.html", piecesVariables) + "\n";
        }
        contentVariables.put("calendar_notes", rows);
        String options = "";
        int courseIndex = Integer.valueOf(getAttribute(request, "selected_course", "0"));
        for (int i = 0; i < Gongule.getData().getCoursesAmount(); i++) {
            Map<String, Object> piecesVariables = new HashMap();
            piecesVariables.put("value", i);
            piecesVariables.put("selected", (i == courseIndex) ? "selected" : "");
            piecesVariables.put("caption", Gongule.getData().getCourse(i).name);
            options += fillTemplate("html/pieces/option.html", piecesVariables) + "\n";
        }
        int todayIndex = Gongule.getData().getCurrentDayIndex();
        contentVariables.put("course_options", options);
        contentVariables.put("today_date", LocalDate.now().format(DateFormatter.get()));
        contentVariables.put("today_day", (todayIndex >= 0) ? Gongule.getData().getDay(todayIndex).name : "");
        contentVariables.put("today_index", Gongule.getData().getCurrentDayNumber());
        contentVariables.put("today_course", Gongule.getData().getCurrentCourseName());
        contentVariables.put("today_display", (todayIndex < 0) ? "none" : "table-row");
        rows = "";
        if (todayIndex >= 0)
            for (Day.Event event: Gongule.getData().getDay(todayIndex).events) {
                Map<String, Object> piecesVariables = new HashMap();
                piecesVariables.put("color", event.time.isAfter(LocalTime.now()) ? WebServer.getColorSchema().getTextColor() : WebServer.getColorSchema().getHalfColor());
                piecesVariables.put("time", event.time.format(TimeFormatter.get()));
                piecesVariables.put("gong", Gongule.getData().getGong(event.gongIndex).name);
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
            int courseIndex = Integer.valueOf(request.getParameter("selected_course"));
            LocalDate courseDate = LocalDate.parse(request.getParameter("course_date"), DateFormatter.get());
            setAttribute(request, "selected_course", String.valueOf(courseIndex));
            Gongule.getData().addCalendarNote(courseIndex, courseDate);
            return true;
        } catch (Exception exception) {
            Log.printError(exception);
            return false;
        }
    }

    private boolean removeNote(HttpServletRequest request){
        try {
            int noteIndex = Integer.valueOf(request.getParameter("remove_note"));
            Gongule.getData().removeCalendarNote(noteIndex);
            return true;
        } catch (Exception exception) {
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
