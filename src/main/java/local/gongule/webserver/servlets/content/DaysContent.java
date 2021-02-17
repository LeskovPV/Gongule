package local.gongule.webserver.servlets.content;

import local.gongule.Gongule;
import local.gongule.utils.formatter.TimeFormatter;
import local.gongule.tools.data.Day;
import local.gongule.webserver.WebServer;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class DaysContent extends Content {

    public DaysContent() {
        actions.put("create_day", (HttpServletRequest request) -> createDay(request));
        actions.put("change_day", (HttpServletRequest request) -> changeDay(request));
        actions.put("delete_day", (HttpServletRequest request) -> deleteDay(request));
        actions.put("add_event", (HttpServletRequest request) -> addEvent(request));
        actions.put("remove_event", (HttpServletRequest request) -> removeEvent(request));
    }

    public String get(HttpServletRequest request) {
        Map<String, Object> contentVariables = new HashMap();
        int selectedDay = Integer.valueOf(getAttribute(request, "selected_day", "0"));
        String rows = "";
        for (int i = 0; i < Gongule.getData().getDayEventsAmount(selectedDay); i++) {
            Map<String, Object> piecesVariables = new HashMap();
            Day.Event event = Gongule.getData().getDayEvent(selectedDay, i);
            piecesVariables.put("color", WebServer.getColorSchema().getTextColor());
            piecesVariables.put("time", event.time.format(TimeFormatter.get()));
            piecesVariables.put("gong", Gongule.getData().getGong(event.gongIndex).name);
            piecesVariables.put("name", event.name);
            piecesVariables.put("value", String.valueOf(i));
            piecesVariables.put("remove_display", "table-cell");
            rows += fillTemplate("html/pieces/event.html", piecesVariables) + "\n";
        }
        contentVariables.put("day_events", rows);

        String options = "";
        for (int i = 0; i < Gongule.getData().getDaysAmount(); i++) {
            Map<String, Object> piecesVariables = new HashMap();
            piecesVariables.put("value", i);
            piecesVariables.put("selected", (i == selectedDay) ? "selected" : "");
            piecesVariables.put("caption", Gongule.getData().getDay(i).name);
            options += fillTemplate("html/pieces/option.html", piecesVariables) + "\n";
        }
        contentVariables.put("day_options", options);
        options = "";
        int gongIndex = Integer.valueOf(getAttribute(request, "selected_gong", "0"));
        for (int i = 0; i < Gongule.getData().getGongsAmount(); i++) {
            Map<String, Object> piecesVariables = new HashMap();
            piecesVariables.put("value", i);
            piecesVariables.put("selected", (i == gongIndex) ? "selected" : "");
            piecesVariables.put("caption", Gongule.getData().getGong(i).name);
            options += fillTemplate("html/pieces/option.html", piecesVariables) + "\n";
        }
        contentVariables.put("time_pattern", TimeFormatter.pattern);
        contentVariables.put("time_size", TimeFormatter.getSize());
        contentVariables.put("gong_options", options);
        contentVariables.put("day_display", Gongule.getData().getDaysAmount() > 0 ? "table-row" : "none");
        return super.getFromTemplate(contentVariables);
    }

    private boolean createDay(HttpServletRequest request) {
        try {
            Gongule.getData().dayCreate(request.getParameter("day_name"));
            setAttribute(request, "selected_day", String.valueOf(Gongule.getData().getDaysAmount()-1));
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean changeDay(HttpServletRequest request) {
        try {
            Integer selectedDay = Integer.valueOf(request.getParameter("selected_day"));
            setAttribute(request, "selected_day", selectedDay.toString());
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean deleteDay(HttpServletRequest request) {
        try {
            Gongule.getData().dayDelete(Integer.valueOf(request.getParameter("selected_day")));
            setAttribute(request, "selected_day", new Integer(0).toString());
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean addEvent(HttpServletRequest request) {
        try {
            int dayIndex = Integer.valueOf(request.getParameter("selected_day"));
            LocalTime eventTime = LocalTime.parse(request.getParameter("event_time"), TimeFormatter.get());
            String eventName = request.getParameter("event_name");
            int gongIndex = Integer.valueOf(request.getParameter( "selected_gong"));
            setAttribute(request, "selected_gong", String.valueOf(gongIndex));
            Gongule.getData().addDayEvent(dayIndex, eventTime, eventName, gongIndex);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean removeEvent(HttpServletRequest request) {
        try {
            int dayIndex = Integer.valueOf(request.getParameter("selected_day"));
            int eventIndex = Integer.valueOf(request.getParameter("remove_event"));
            Gongule.getData().removeDayEvent(dayIndex, eventIndex);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

}
