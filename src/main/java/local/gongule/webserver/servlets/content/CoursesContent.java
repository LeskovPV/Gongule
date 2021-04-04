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
        actions.put("change_course", (HttpServletRequest request) -> selectCourse(request));
        actions.put("delete_course", (HttpServletRequest request) -> deleteCourse(request));
        actions.put("add_day", (HttpServletRequest request) -> addDay(request));
        actions.put("remove_day", (HttpServletRequest request) -> removeDay(request));
    }

    public String get(HttpServletRequest request) {
        Map<String, Object> contentVariables = new HashMap();
        int selectedCourse = Integer.valueOf(getAttribute(request, "select_course", "0"));
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
        int dayIndex = Integer.valueOf(getAttribute(request, "select_day", "0"));
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
            String courseName = request.getParameter("course_name");
            if (!data.courseCreate(courseName)) return false;
            logger.info("Course '{}' is deleted", courseName);
            setAttribute(request, "select_course", String.valueOf(data.getCoursesAmount()-1));
            return true;
        } catch (Exception exception) {
            logger.error("Impossible create course: {}", exception.getMessage());
            return false;
        }
    }

    private boolean selectCourse(HttpServletRequest request) {
        try {
            Integer selectedCourse = Integer.valueOf(request.getParameter("select_course"));
            setAttribute(request, "select_course", selectedCourse.toString());
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean deleteCourse(HttpServletRequest request) {
        try {
            Data data = Data.getInstance();
            int courseIndex = Integer.valueOf(request.getParameter("select_course"));
            String courseName = data.getCourse(courseIndex).name;
            if (!data.courseDelete(courseIndex)) return false;
            logger.info("Course '{}' is deleted", courseName);
            setAttribute(request, "select_course", "0");
            GongExecutor.reset();
            return true;
        } catch (Exception exception) {
            logger.error("Impossible delete course: {}", exception.getMessage());
            return false;
        }
    }

    private boolean addDay(HttpServletRequest request) {
        try {
            int courseIndex = Integer.valueOf(request.getParameter("select_course"));
            int dayIndex = Integer.valueOf(request.getParameter("select_day"));
            setAttribute(request, "select_day", String.valueOf(dayIndex));
            Data data = Data.getInstance();
            if (!data.createCourseDay(courseIndex, dayIndex)) return false;
            logger.info("Added day '{}' to '{}'", data.getDay(dayIndex).name, data.getCourse(courseIndex).name);
            GongExecutor.reset();
            return true;
        } catch (Exception exception) {
            logger.error("Impossible add day to course: {}", exception.getMessage());
            return false;
        }
    }

    private boolean removeDay(HttpServletRequest request) {
        try {
            int courseIndex = Integer.valueOf(request.getParameter("select_course"));
            int dayIndex = Integer.valueOf(request.getParameter("remove_day"));
            Data data = Data.getInstance();
            String dayName = data.getDay(dayIndex).name;
            if (!data.removeCourseDay(courseIndex, dayIndex)) return false;
            logger.info("Removed day '{}' from '{}'", dayName, data.getCourse(courseIndex).name);
            GongExecutor.reset();
            return true;
        } catch (Exception exception) {
            logger.error("Impossible remove day from course: {}", exception.getMessage());
            return false;
        }
    }

}
