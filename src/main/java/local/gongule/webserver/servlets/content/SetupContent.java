package local.gongule.webserver.servlets.content;

import local.gongule.tools.process.GongExecutor;
import local.gongule.tools.process.GongSound;
import local.gongule.tools.relays.CoolingRelay;
import local.gongule.utils.FontFamily;
import local.gongule.tools.data.Data;
import local.gongule.tools.data.Gong;
import local.gongule.utils.colors.ColorSchema;
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
        actions.put("set_delay", (HttpServletRequest request) -> setDelay(request));
        actions.put("set_advance", (HttpServletRequest request) -> setAdvance(request));
        actions.put("set_datetime", (HttpServletRequest request) -> setDateTime(request));
        actions.put("set_temperatures", (HttpServletRequest request) -> setTemperatures(request));
        actions.put("sys_reboot", (HttpServletRequest request) -> rebootSystem(request));
        actions.put("sys_shutdown", (HttpServletRequest request) -> shutdownSystem(request));
    }

    public String get(HttpServletRequest request) {
        Map<String, Object> contentVariables = new HashMap();
        String rows = "";
        Data data = Data.getInstance();
        for(int i = 0; i < data.getGongsAmount(); i++) {
            Gong gong = data.getGong(i);
            Map<String, Object> piecesVariables = new HashMap();
            piecesVariables.put("name", gong.name);
            piecesVariables.put("amount", gong.amount);
            piecesVariables.put("value", i);
            rows += fillTemplate("html/pieces/gong.html", piecesVariables) + "\n";
        }
        contentVariables.put("gong_rows", rows);
        contentVariables.put("color_value", ColorSchema.getInstance().getBaseColor());
        String options = "";
        for (int i = 0; i < FontFamily.values.size(); i++) {
            Map<String, Object> piecesVariables = new HashMap();
            piecesVariables.put("value", i);
            piecesVariables.put("selected", (i == WebServer.getFontIndex()) ? "selected" : "");
            piecesVariables.put("caption", FontFamily.values.get(i));
            options += fillTemplate("html/pieces/option.html", piecesVariables) + "\n";
        }
        contentVariables.put("font_options", options);
        options = "";
        List<String> files = Data.getFiles();
        for(String file: files) {
            Map<String, Object> piecesVariables = new HashMap();
            piecesVariables.put("value", file);
            piecesVariables.put("selected", (file.equalsIgnoreCase(data.defaultName)) ? "selected" : "");
            piecesVariables.put("caption", file);
            options += fillTemplate("html/pieces/option.html", piecesVariables) + "\n";
        }
        contentVariables.put("configuration_options", options);
        contentVariables.put("strikes_delay", GongSound.getStrikesDelay());
        contentVariables.put("advance_time", GongExecutor.getAdvanceTime());
        contentVariables.put("datetime_value", LocalDate.now().format(DateFormatter.get()) + " " + LocalTime.now().format(TimeFormatter.get(true)));
        contentVariables.put("min_temperature", CoolingRelay.getInstance().getMinTemperature());
        contentVariables.put("max_temperature", CoolingRelay.getInstance().getMaxTemperature());
        contentVariables.put("cpu_temperature",SystemUtils.getCPUTemperature(55));
        contentVariables.put("raspbian_display", SystemUtils.isRaspbian ? "table-row" : "table-row");
        contentVariables.put("half_color", ColorSchema.getInstance().getHalfColor());
        return super.getFromTemplate(contentVariables);
    }

    private boolean deleteGong(HttpServletRequest request) {
        String gongIndex = request.getParameter("delete_gong");
        try {
            Data.getInstance().gongDelete(Integer.valueOf(gongIndex));
            logger.info("Gong '" + gongIndex + "' is deleted");
            GongExecutor.reset();
            return true;
        } catch (Exception exception) {
            logger.info("Impossible delete '" + gongIndex + "' configuration");
            return false;
        }
    }

    private boolean playGong(HttpServletRequest request) {
        try {
            Gong gong = Data.getInstance().getGong(Integer.valueOf(request.getParameter("play_gong")));
            GongSound.play(gong, "The testing",false);
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    private boolean createGong(HttpServletRequest request) {
        try {
            return Data.getInstance().gongCreate(
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
        if (Data.getInstance().save(name)) {
            logger.info("Configuration save as '{}'", name);
            return true;
        } else {
            logger.warn("Impossible save configuration as '{}'", name);
            return false;
        }
    }

    private boolean loadConfiguration(HttpServletRequest request) {
        String name = request.getParameter("selected_configuration");
        boolean result = Data.setInstance(Data.load(name));
        logger.info(result ? "Configuration '{}' is loaded" : "Impossible load '{}' configuration", name);
        if (result) GongExecutor.reset();
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

    private boolean setDelay(HttpServletRequest request) {
        try {
            GongSound.setStrikesDelay(Integer.valueOf(request.getParameter("strikes_delay")));
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    private boolean setAdvance(HttpServletRequest request) {
        try {
            GongExecutor.setAdvanceTime(Integer.valueOf(request.getParameter("advance_time")));
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    private boolean setDateTime(HttpServletRequest request) {
        try {
            String[] datetimes = request.getParameter("datetime_value").trim().split(" ");
            if (datetimes.length != 2) throw new Exception();
            LocalDate date = LocalDate.parse(datetimes[0], DateFormatter.get());
            LocalTime time = LocalTime.parse(datetimes[1], TimeFormatter.get(true));
            SystemUtils.setDateTime(date, time);
            GongExecutor.reset();
            return true;
        } catch (Exception exception) {
            logger.error("Impossible parse '{}' to datetime value", request.getParameter("datetime_value"));
            return false;
        }
    }

    private boolean setTemperatures(HttpServletRequest request) {
        try {
            CoolingRelay.getInstance().setTemperatures(
                    Double.valueOf(request.getParameter("min_temperature")),
                    Double.valueOf(request.getParameter("max_temperature"))
            );
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    private boolean rebootSystem(HttpServletRequest request) {
        logger.warn("Reboot system by web-interface from remote host ({})", request.getRemoteHost());
        SystemUtils.reboot();
        return true;
    }

    private boolean shutdownSystem(HttpServletRequest request) {
        logger.warn("Shutdown system by web-interface from remote host ({})", request.getRemoteHost());
        SystemUtils.shutdown();
        return true;
    }

}
