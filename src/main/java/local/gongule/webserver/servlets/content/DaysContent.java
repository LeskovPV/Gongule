package local.gongule.webserver.servlets.content;

import local.gongule.Gongule;
import local.gongule.tools.data.Day;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class DaysContent extends Content {

    public DaysContent() {
        actions.put("create_day", (HttpServletRequest request) -> createDay(request));
        actions.put("change_day", (HttpServletRequest request) -> changeDay(request));
        actions.put("delete_day", (HttpServletRequest request) -> deleteDay(request));
        actions.put("create_event", (HttpServletRequest request) -> createEvent(request));
        actions.put("delete_event", (HttpServletRequest request) -> deleteEvent(request));
    }

    public String get(HttpServletRequest request) {
        Map<String, Object> contentVariables = new HashMap();
        String options = "";
        int selectedDay = Integer.valueOf(getAttribute(request, "selected_day", "0"));
        for (int i = 0; i < Gongule.getData().getDaysAmount(); i++) {
            Map<String, Object> piecesVariables = new HashMap();
            piecesVariables.put("value", i);
            piecesVariables.put("selected", (i == selectedDay) ? "selected" : "");
            piecesVariables.put("caption", Gongule.getData().getDay(i).name);
            options += fillTemplate("html/pieces/option.html", piecesVariables) + "\n";
        }
        contentVariables.put("day_options", options);
        String rows = "";
        for (int i = 0; i < Gongule.getData().getDayEventsAmount(selectedDay); i++) {
            Map<String, Object> piecesVariables = new HashMap();
            Day.Event event = Gongule.getData().getDayEvent(selectedDay, i);
            piecesVariables.put("time", event.time.toString());
            piecesVariables.put("gong", Gongule.getData().getGong(event.gongIndex).name);
            piecesVariables.put("name", event.name);
            piecesVariables.put("value", String.valueOf(i));
            rows += fillTemplate("html/pieces/event.html", piecesVariables) + "\n";
        }
        contentVariables.put("day_events", rows);
        options = "";
        for (int i = 0; i < Gongule.getData().getGongsAmount(); i++) {
            Map<String, Object> piecesVariables = new HashMap();
            piecesVariables.put("value", i);
            piecesVariables.put("selected", (i == 0) ? "selected" : "");
            piecesVariables.put("caption", Gongule.getData().getGong(i).name);
            options += fillTemplate("html/pieces/option.html", piecesVariables) + "\n";
        }
        contentVariables.put("gong_options", options);
        contentVariables.put("day_display", Gongule.getData().getDaysAmount() > 0 ? "table-row" : "none");
        return super.get(contentVariables);
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

    private boolean createDay(HttpServletRequest request) {
        try {
            Gongule.getData().dayCreate(request.getParameter("day_name"));
            setAttribute(request, "selected_day", String.valueOf(Gongule.getData().getDaysAmount()-1));
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean createEvent(HttpServletRequest request) {
        try {
            int dayIndex = Integer.valueOf(getAttribute(request, "selected_day"));
            LocalTime eventTime = LocalTime.parse(request.getParameter("event_time"));
            String eventName = request.getParameter("event_name");
            int gongIndex = Integer.valueOf(request.getParameter( "selected_gong"));
            Gongule.getData().createDayEvent(dayIndex, eventTime, eventName, gongIndex);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean deleteEvent(HttpServletRequest request) {
        try {
            int dayIndex = Integer.valueOf(getAttribute(request, "selected_day"));
            int eventIndex = Integer.valueOf(request.getParameter("delete_event"));
            Gongule.getData().deleteDayEvent(dayIndex, eventIndex);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

}
