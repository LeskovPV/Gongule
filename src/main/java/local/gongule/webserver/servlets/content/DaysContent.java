package local.gongule.webserver.servlets.content;

import local.gongule.tools.data.Data;
import local.gongule.tools.process.GongExecutor;
import local.gongule.utils.colors.ColorSchema;
import local.gongule.utils.formatter.TimeFormatter;
import local.gongule.tools.data.Day;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class DaysContent extends Content {

    public DaysContent() {
        actions.put("create_day", (HttpServletRequest request) -> createDay(request));
        actions.put("select_day", (HttpServletRequest request) -> selectDay(request));
        actions.put("delete_day", (HttpServletRequest request) -> deleteDay(request));
        actions.put("add_event", (HttpServletRequest request) -> addEvent(request));
        actions.put("remove_event", (HttpServletRequest request) -> removeEvent(request));
    }

    public String get(HttpServletRequest request) {
        Data data = Data.getInstance();
        Map<String, Object> contentVariables = new HashMap();
        int dayIndex = Integer.valueOf(getAttribute(request, "select_day", "0"));
        if ((0 > dayIndex) || (dayIndex > data.getDaysAmount())) dayIndex = 0;
        String rows = "";
        for (int i = 0; i < data.getDayEventsAmount(dayIndex); i++) {
            Map<String, Object> piecesVariables = new HashMap();
            Day.Event event = data.getDayEvent(dayIndex, i);
            piecesVariables.put("color", ColorSchema.getInstance().getTextColor());
            piecesVariables.put("time", event.time.format(TimeFormatter.get()));
            piecesVariables.put("gong", data.getGong(event.gongIndex).name);
            piecesVariables.put("name", event.name);
            piecesVariables.put("value", String.valueOf(i));
            piecesVariables.put("remove_display", "table-cell");
            rows += fillTemplate("html/pieces/event.html", piecesVariables) + "\n";
        }
        contentVariables.put("day_events", rows);
        String options = "";
        for (int i = 0; i < data.getDaysAmount(); i++) {
            Map<String, Object> piecesVariables = new HashMap();
            piecesVariables.put("value", i);
            piecesVariables.put("selected", (i == dayIndex) ? "selected" : "");
            piecesVariables.put("caption", data.getDay(i).name);
            options += fillTemplate("html/pieces/option.html", piecesVariables) + "\n";
        }
        contentVariables.put("day_options", options);
        options = "";
        int gongIndex = Integer.valueOf(getAttribute(request, "select_gong", "0"));
        if ((0 > gongIndex) || (gongIndex > data.getGongsAmount())) gongIndex = 0;
        for (int i = 0; i < data.getGongsAmount(); i++) {
            Map<String, Object> piecesVariables = new HashMap();
            piecesVariables.put("value", i);
            piecesVariables.put("selected", (i == gongIndex) ? "selected" : "");
            piecesVariables.put("caption", data.getGong(i).name);
            options += fillTemplate("html/pieces/option.html", piecesVariables) + "\n";
        }
        contentVariables.put("time_pattern", TimeFormatter.pattern);
        contentVariables.put("time_size", TimeFormatter.getSize());
        contentVariables.put("gong_options", options);
        contentVariables.put("day_display", data.getDaysAmount() > 0 ? "table-row" : "none");
        return super.getFromTemplate(contentVariables);
    }

    private boolean createDay(HttpServletRequest request) {
        try {
            Data.getInstance().dayCreate(request.getParameter("day_name"));
            setAttribute(request, "select_day", String.valueOf(Data.getInstance().getDaysAmount()-1));
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean selectDay(HttpServletRequest request) {
        try {
            Integer selectedDay = Integer.valueOf(request.getParameter("select_day"));
            setAttribute(request, "select_day", selectedDay.toString());
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean deleteDay(HttpServletRequest request) {
        try {
            Data.getInstance().dayDelete(Integer.valueOf(request.getParameter("select_day")));
            setAttribute(request, "select_day", new Integer(0).toString());
            GongExecutor.reset();
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean addEvent(HttpServletRequest request) {
        try {
            int dayIndex = Integer.valueOf(request.getParameter("select_day"));
            LocalTime eventTime = LocalTime.parse(request.getParameter("event_time"), TimeFormatter.get());
            String eventName = request.getParameter("event_name");
            int gongIndex = Integer.valueOf(request.getParameter( "select_gong"));
            setAttribute(request, "select_gong", String.valueOf(gongIndex));
            Data.getInstance().addDayEvent(dayIndex, eventTime, eventName, gongIndex);
            GongExecutor.reset();
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean removeEvent(HttpServletRequest request) {
        try {
            int dayIndex = Integer.valueOf(request.getParameter("select_day"));
            int eventIndex = Integer.valueOf(request.getParameter("remove_event"));
            Data.getInstance().removeDayEvent(dayIndex, eventIndex);
            GongExecutor.reset();
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

}
