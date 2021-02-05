package local.gongule.webserver.servlets.content;

import local.gongule.Gongule;
import local.gongule.tools.Log;
import local.gongule.tools.data.Course;
import local.gongule.tools.data.Data;
import local.gongule.tools.data.Day;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ControlContent extends Content{

    public ControlContent() {
        actions.put("add_note", (HttpServletRequest request) -> addNote(request));
        actions.put("remove_note", (HttpServletRequest request) -> removeNote(request));
    }

    public String get(HttpServletRequest request) {
        Map<String, Object> contentVariables = new HashMap();
        String rows = "";
        for (int i = 0; i < Gongule.getData().calendar.size(); i++) {
            Map<String, Object> piecesVariables = new HashMap();
            Data.Note note = Gongule.getData().calendar.get(i);
            Course course = Gongule.getData().getCourse(note.courseIndex);
            piecesVariables.put("begin", note.date);
            piecesVariables.put("name", course.name);
            piecesVariables.put("end", note.date.plusDays(course.dayIndexes.size()));
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
        contentVariables.put("course_options", options);
        contentVariables.put("today_date", LocalDate.now());
        contentVariables.put("today_display", (Gongule.getData().getTodayEvent().size() == 0) ? "none" : "table-row");
        rows = "";
        for (Day.Event event: Gongule.getData().getTodayEvent()) {
            Map<String, Object> piecesVariables = new HashMap();
            piecesVariables.put("time", event.time.toString());
            piecesVariables.put("gong", Gongule.getData().getGong(event.gongIndex).name);
            piecesVariables.put("name", event.name);
            piecesVariables.put("value", -1);
            piecesVariables.put("remove_display", "none");
            rows += fillTemplate("html/pieces/event.html", piecesVariables) + "\n";
        }

        contentVariables.put("today_events", rows);
        return super.get(contentVariables);
    }

    private boolean addNote(HttpServletRequest request) {
        Log.printInfo("Now is " + LocalDate.now());
        try {
            int courseIndex = Integer.valueOf(request.getParameter("selected_course"));
            LocalDate courseDate = LocalDate.parse(request.getParameter("course_date"));
            setAttribute(request, "selected_course", String.valueOf(courseIndex));
            Log.printInfo("Date = " + courseDate);
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


}
