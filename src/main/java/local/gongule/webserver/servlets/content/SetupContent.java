package local.gongule.webserver.servlets.content;

import local.gongule.Gongule;
import local.gongule.tools.FontFamily;
import local.gongule.tools.Log;
import local.gongule.tools.data.Data;
import local.gongule.tools.data.Gong;
import local.gongule.webserver.WebServer;

import javax.servlet.http.HttpServletRequest;
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
        // Log.printInfo(file);
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
        return super.getFromTemplate(contentVariables);
    }

    private boolean deleteGong(HttpServletRequest request) {
        String gongIndex = request.getParameter("delete_gong");
        try {
            Gongule.getData().gongDelete(Integer.valueOf(gongIndex));
            Log.printInfo("Gong '" + gongIndex + "' is deleted");
            return true;
        } catch (Exception exception) {
            Log.printInfo("Impossible delete '" + gongIndex + "' configuration");
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
            Log.printInfo("Configuration save as '" + name + "'");
            return true;
        } else {
            Log.printWarn("Impossible save configuration as '" + name + "'");
            return false;
        }
    }

    private boolean loadConfiguration(HttpServletRequest request) {
        String name = request.getParameter("selected_configuration");
        boolean result = Gongule.setData(Data.load(name));
        Log.printInfo(result ? "Configuration '" + name + "' is loaded" : "Impossible load '" + name + "' configuration");
        return result;
    }

    private boolean deleteConfiguration(HttpServletRequest request) {
        String name = request.getParameter("selected_configuration");
        boolean result = Data.detete(name);
        if (result)
            Log.printInfo("Configuration '" + name + "' is deleted");
        else
            Log.printInfo("Impossible delete '" + name + "' configuration");
        return result;
    }

}
