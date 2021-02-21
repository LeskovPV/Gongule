package local.gongule.webserver.servlets.content;

import local.gongule.Gongule;
import local.gongule.utils.FontFamily;
import local.gongule.tools.data.Data;
import local.gongule.tools.data.Gong;
import local.gongule.utils.formatter.DateFormatter;
import local.gongule.utils.formatter.TimeFormatter;
import local.gongule.utils.system.SystemUtils;
import local.gongule.webserver.WebServer;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetupContent extends Content{

    public SetupContent() {
        actions.put("delete_gong", (HttpServletRequest request) -> deleteGong(request));
        actions.put("play_gong", (HttpServletRequest request) -> playGong(request));
        actions.put("create_gong", (HttpServletRequest request) -> createGong(request));
        actions.put("change_color", (HttpServletRequest request) -> changeColor(request));
        actions.put("change_font", (HttpServletRequest request) -> changeFont(request));
        actions.put("save_configuration", (HttpServletRequest request) -> saveConfiguration(request));
        actions.put("load_configuration", (HttpServletRequest request) -> loadConfiguration(request));
        actions.put("delete_configuration", (HttpServletRequest request) -> deleteConfiguration(request));
        actions.put("sys_shutdown", (HttpServletRequest request) -> shutdownSystem(request));
        actions.put("change_time", (HttpServletRequest request) -> changeTime(request));
        actions.put("change_date", (HttpServletRequest request) -> changeDate(request));
    }

    public String get(HttpServletRequest request) {
        Map<String, Object> contentVariables = new HashMap();
        String rows = "";
        for(int i = 0; i < Gongule.getData().getGongsAmount(); i++) {
            Gong gong = Gongule.getData().getGong(i);
            Map<String, Object> piecesVariables = new HashMap();
            piecesVariables.put("name", gong.name);
            piecesVariables.put("amount", gong.amount);
            piecesVariables.put("value", i);
            rows += fillTemplate("html/pieces/gong.html", piecesVariables) + "\n";
        }
        contentVariables.put("gong_rows", rows);
        contentVariables.put("color_value", WebServer.getColorSchema().getBaseColor());
        String options = "";
        for (int i = 0; i < FontFamily.values.size(); i++) {
            Map<String, Object> piecesVariables = new HashMap();
            piecesVariables.put("value", i);
            piecesVariables.put("selected", (i == WebServer.getFontIndex(false)) ? "selected" : "");
            piecesVariables.put("caption", FontFamily.values.get(i));
            options += fillTemplate("html/pieces/option.html", piecesVariables) + "\n";
        }
        contentVariables.put("font_options", options);
        // logger.info(file);
        options = "";
        List<String> files = Data.getFiles();
        for(String file: files) {
            Map<String, Object> piecesVariables = new HashMap();
            piecesVariables.put("value", file);
            piecesVariables.put("selected", (file.equalsIgnoreCase(Data.getDefaultName())) ? "selected" : "");
            piecesVariables.put("caption", file);
            options += fillTemplate("html/pieces/option.html", piecesVariables) + "\n";
        }
        contentVariables.put("configuration_options", options);
        contentVariables.put("time_value", LocalTime.now().format(TimeFormatter.get(true)));
        contentVariables.put("date_value", LocalDate.now().format(DateFormatter.get()));
        contentVariables.put("datetime_size", TimeFormatter.getSize(true) > DateFormatter.getSize() ? TimeFormatter.getSize(true) : DateFormatter.getSize());
        return super.getFromTemplate(contentVariables);
    }

    private boolean deleteGong(HttpServletRequest request) {
        String gongIndex = request.getParameter("delete_gong");
        try {
            Gongule.getData().gongDelete(Integer.valueOf(gongIndex));
            logger.info("Gong '" + gongIndex + "' is deleted");
            return true;
        } catch (Exception exception) {
            logger.info("Impossible delete '" + gongIndex + "' configuration");
            return false;
        }
    }

    private boolean playGong(HttpServletRequest request) {
        try {
            Gongule.getData().gongPlay(
                    Integer.valueOf(request.getParameter("play_gong")));
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    private boolean createGong(HttpServletRequest request) {
        try {
            return Gongule.getData().gongCreate(
                    request.getParameter("gong_name"),
                    Integer.valueOf(request.getParameter("gong_amount")));
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean changeColor(HttpServletRequest request) {
        WebServer.setBaseColor(request.getParameter("selected_color"));
        return true;
    }

    private boolean changeFont(HttpServletRequest request) {
        try {
            WebServer.setFontIndex(Integer.valueOf(request.getParameter("selected_font")));
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    private boolean saveConfiguration(HttpServletRequest request) {
        String name = request.getParameter("configuration_name");
        if (Gongule.getData().save(name)) {
            logger.info("Configuration save as '{}'", name);
            return true;
        } else {
            logger.warn("Impossible save configuration as '{}'", name);
            return false;
        }
    }

    private boolean loadConfiguration(HttpServletRequest request) {
        String name = request.getParameter("selected_configuration");
        boolean result = Gongule.setData(Data.load(name));
        logger.info(result ? "Configuration '{}' is loaded" : "Impossible load '{}' configuration", name);
        return result;
    }

    private boolean deleteConfiguration(HttpServletRequest request) {
        String name = request.getParameter("selected_configuration");
        boolean result = Data.detete(name);
        if (result)
            logger.info("Configuration '{}' is deleted", name);
        else
            logger.info("Impossible delete '{}' configuration", name);
        return result;
    }

    private boolean shutdownSystem(HttpServletRequest request) {
        SystemUtils.shutdown();
        return true;
    }

    private boolean changeTime(HttpServletRequest request) {
        //String time = request.getParameter("time_value");
        LocalTime time = LocalTime.now();
        //LocalTime time = LocalTime.now();
        try {
            time = LocalTime.parse(request.getParameter("time_value"), TimeFormatter.get(true));
        } catch (Exception exception) {
            logger.error("Impossible parse '{}' to time value", request.getParameter("time_value"));
            return false;
        }
        SystemUtils.setTime(time);
        return true;
    }

    private boolean changeDate(HttpServletRequest request) {
        LocalDate date = LocalDate.now();
        try {
            date = LocalDate.parse(request.getParameter("date_value"), DateFormatter.get());
        } catch (Exception exception) {
            logger.error("Impossible parse '{}' to date value", request.getParameter("date_value"));
            return false;
        }
        SystemUtils.setDate(date);
        return true;
    }

}
