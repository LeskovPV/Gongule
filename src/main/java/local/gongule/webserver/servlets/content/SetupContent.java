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
        actions.put("select_color", (HttpServletRequest request) -> selectColor(request));
        actions.put("select_font", (HttpServletRequest request) -> selectFont(request));
        actions.put("save_configuration", (HttpServletRequest request) -> saveConfiguration(request));
        actions.put("load_configuration", (HttpServletRequest request) -> loadConfiguration(request));
        actions.put("delete_configuration", (HttpServletRequest request) -> deleteConfiguration(request));
        actions.put("set_delay", (HttpServletRequest request) -> setDelay(request));
        actions.put("set_level", (HttpServletRequest request) -> setLevel(request));
        actions.put("set_number", (HttpServletRequest request) -> setNumber(request));
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
        contentVariables.put("volume_level", GongSound.getMinVolumeLevel());
        contentVariables.put("volume_number", GongSound.getMaxVolumeNumber());
        contentVariables.put("advance_time", GongExecutor.getAdvanceTime());
        contentVariables.put("datetime_value", LocalDate.now().format(DateFormatter.get()) + " " + LocalTime.now().format(TimeFormatter.get(true)));
        contentVariables.put("min_temperature", CoolingRelay.getInstance().getMinTemperature());
        contentVariables.put("max_temperature", CoolingRelay.getInstance().getMaxTemperature());
        contentVariables.put("cpu_temperature",SystemUtils.getCPUTemperature(55));
        contentVariables.put("raspbian_display", SystemUtils.isRaspbian ? "table-row" : "none");
        contentVariables.put("half_color", ColorSchema.getInstance().getHalfColor());
        return super.getFromTemplate(contentVariables);
    }

    private boolean deleteGong(HttpServletRequest request) {
        try {
            Data data = Data.getInstance();
            int gongIndex = Integer.valueOf(request.getParameter("delete_gong"));
            String gongName = data.getGong(gongIndex).name;
            if (!data.gongDelete(gongIndex)) return false;
            logger.info("Gong '{}' is deleted", gongName);
            GongExecutor.reset();
            return true;
        } catch (Exception exception) {
            logger.error("Impossible delete gong: {}", exception.getMessage());
            return false;
        }
    }

    private boolean playGong(HttpServletRequest request) {
        try {
            Gong gong = Data.getInstance().getGong(Integer.valueOf(request.getParameter("play_gong")));
            GongSound.play(gong, "The manual control",false);
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    private boolean createGong(HttpServletRequest request) {
        try {
            String gongName = request.getParameter("gong_name");
            int gongAmount = Integer.valueOf(request.getParameter("gong_amount"));
            if (!Data.getInstance().gongCreate(gongName, gongAmount)) return false;
            logger.info("Created gong '{}'", gongName);
            return true;
        } catch (Exception exception) {
            logger.error("Impossible create gong: {}", exception.getMessage());
            return false;
        }
    }

    private boolean selectColor(HttpServletRequest request) {
        WebServer.setBaseColor(request.getParameter("select_color"));
        return true;
    }

    private boolean selectFont(HttpServletRequest request) {
        try {
            WebServer.setFontIndex(Integer.valueOf(request.getParameter("select_font")));
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
            logger.error("Impossible save configuration as '{}'", name);
            return false;
        }
    }

    private boolean loadConfiguration(HttpServletRequest request) {
        String name = request.getParameter("select_configuration");
        boolean result = Data.setInstance(Data.load(name));
        if (result)
            logger.info("Configuration '{}' is loaded", name);
        else
            logger.info("Impossible load '{}' configuration", name);
        if (result) GongExecutor.reset();
        return result;
    }

    private boolean deleteConfiguration(HttpServletRequest request) {
        String name = request.getParameter("select_configuration");
        boolean result = Data.detete(name);
        if (result)
            logger.info("Configuration '{}' is deleted", name);
        else
            logger.error("Impossible delete '{}' configuration", name);
        return result;
    }

    private boolean setDelay(HttpServletRequest request) {
        try {
            int strikesDelay = Integer.valueOf(request.getParameter("strikes_delay"));
            GongSound.setStrikesDelay(strikesDelay);
            //logger.info("Changed a delay between gong strikes. New value is {}", strikesDelay);
        } catch (Exception exception) {
            logger.error("Impossible set delay between gong strikes: {}", exception.getMessage());
            return false;
        }
        return true;
    }

    private boolean setLevel(HttpServletRequest request) {
        try {
            int volumeLevel = Integer.valueOf(request.getParameter("volume_level"));
            GongSound.setMinVolumeLevel(volumeLevel);
            //logger.info("Changed a volume level percent of first gong. New value is {}", volumeLevel);
        } catch (Exception exception) {
            logger.error("Impossible set volume level percent of first gong: {}", exception.getMessage());
            return false;
        }
        return true;
    }

    private boolean setNumber(HttpServletRequest request) {
        try {
            int volumeNumber = Integer.valueOf(request.getParameter("volume_number"));
            GongSound.setMaxVolumeNumber(volumeNumber);
            //logger.info("Changed a maximum volume gong number. New value is {}", volumeNumber);
        } catch (Exception exception) {
            logger.error("Impossible set maximum volume gong number: {}", exception.getMessage());
            return false;
        }
        return true;
    }

    private boolean setAdvance(HttpServletRequest request) {
        try {
            int advanceTime = Integer.valueOf(request.getParameter("advance_time"));
            GongExecutor.setAdvanceTime(advanceTime);
            //logger.info("Changed a amplifier power turn-on advance time. New value is {}", advanceTime);
        } catch (Exception exception) {
            logger.error("Impossible set amplifier power turn-on advance time: {}", exception.getMessage());
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
            GongExecutor.runMidnightReset();
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
            logger.error("Impossible set temperatures: {}", exception.getMessage());
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
