package local.gongule.webserver.servlets.content;

import local.gongule.tools.data.Data;
import local.gongule.tools.data.Day;
import local.gongule.tools.process.GongExecutor;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class CoursesContent extends Content {

    public CoursesContent() {
        actions.put("create_course", (HttpServletRequest request) -> createCourse(request));
        actions.put("change_course", (HttpServletRequest request) -> changeCourse(request));
        actions.put("delete_course", (HttpServletRequest request) -> deleteCourse(request));
        actions.put("add_day", (HttpServletRequest request) -> addDay(request));
        actions.put("remove_day", (HttpServletRequest request) -> removeDay(request));
    }

    public String get(HttpServletRequest request) {
        Map<String, Object> contentVariables = new HashMap();
        int selectedCourse = Integer.valueOf(getAttribute(request, "selected_course", "0"));
        String rows = "";
        Data data = Data.getInstance();
        for (int i = 0; i < data.getCourseDaysAmount(selectedCourse); i++) {
            Map<String, Object> piecesVariables = new HashMap();
            Day day = data.getCourseDay(selectedCourse, i);
            piecesVariables.put("number", i);
            piecesVariables.put("name", day.name);
            piecesVariables.put("value", i);
            rows += fillTemplate("html/pieces/day.html", piecesVariables) + "\n";
        }
        contentVariables.put("course_days", rows);
        String options = "";
        for (int i = 0; i < data.getCoursesAmount(); i++) {
            Map<String, Object> piecesVariables = new HashMap();
            piecesVariables.put("value", i);
            piecesVariables.put("selected", (i == selectedCourse) ? "selected" : "");
            piecesVariables.put("caption", data.getCourse(i).name);
            options += fillTemplate("html/pieces/option.html", piecesVariables) + "\n";
        }
        contentVariables.put("course_options", options);

        options = "";
        int dayIndex = Integer.valueOf(getAttribute(request, "selected_day", "0"));
        for (int i = 0; i < data.getDaysAmount(); i++) {
            Map<String, Object> piecesVariables = new HashMap();
            piecesVariables.put("value", i);
            piecesVariables.put("selected", (i == dayIndex) ? "selected" : "");
            piecesVariables.put("caption", data.getDay(i).name);
            options += fillTemplate("html/pieces/option.html", piecesVariables) + "\n";
        }
        contentVariables.put("day_options", options);
        contentVariables.put("course_display", data.getCoursesAmount() > 0 ? "table-row" : "none");
        return super.getFromTemplate(contentVariables);
    }

    private boolean createCourse(HttpServletRequest request) {
        try {
            Data data = Data.getInstance();
            data.courseCreate(request.getParameter("course_name"));
            setAttribute(request, "selected_course", String.valueOf(data.getCoursesAmount()-1));
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean changeCourse(HttpServletRequest request) {
        try {
            Integer selectedCourse = Integer.valueOf(request.getParameter("selected_course"));
            setAttribute(request, "selected_course", selectedCourse.toString());
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean deleteCourse(HttpServletRequest request) {
        try {
            Data.getInstance().courseDelete(Integer.valueOf(request.getParameter("selected_course")));
            setAttribute(request, "selected_course", new Integer(0).toString());
            GongExecutor.reset();
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean addDay(HttpServletRequest request) {
        try {
            int courseIndex = Integer.valueOf(request.getParameter("selected_course"));
            int dayIndex = Integer.valueOf(request.getParameter("selected_day"));
            setAttribute(request, "selected_day", String.valueOf(dayIndex));
            Data.getInstance().createCourseDay(courseIndex, dayIndex);
            GongExecutor.reset();
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean removeDay(HttpServletRequest request) {
        try {
            int courseIndex = Integer.valueOf(request.getParameter("selected_course"));
            int dayIndex = Integer.valueOf(request.getParameter("remove_day"));
            Data.getInstance().removeCourseDay(courseIndex, dayIndex);
            GongExecutor.reset();
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

}
