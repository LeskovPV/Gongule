package local.gongule.webserver.servlets;

import local.gongule.tools.TemplateFillable;
import local.gongule.tools.resources.ResourceType;
import local.gongule.tools.resources.Resources;
import local.gongule.tools.SystemInformation;
import local.gongule.tools.colors.ColorSchema;
import local.gongule.webserver.WebServer;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.Arrays.asList;

import java.util.*;

// Обработка запроса ресурса
// вида: <server>:<port>/resource?<resourceName>
// например: localhost/resource?js/jquery/flot/jquery.flot.js
// или https://localhost/resource?png/logo.png
public class ResourceServlet extends HttpServlet implements TemplateFillable {

    // Список защищенных ресурсов, для получения которых требуется авторизация
    public static final List<String> privateResources = asList(
            "keystore"
    );



    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        //String resourceName = request.getQueryString();
        String[] requests = request.getQueryString().split("&");

        String resourceName = requests.length > 0 ? requests[0] : null;

        if (resourceName == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

//        if (privateResources.contains(resourceName))
//            if (!accountService.accessIsAllow(request)) return;

        try {
            Map<String, Object> pageVariables = new HashMap(0);
            switch (ResourceType.getByName(resourceName)){
                case CSS:
                case JS:
                    ColorSchema colorSchema = WebServer.getColorSchema();
                    pageVariables.put("site_color", colorSchema.getSiteColor());
                    pageVariables.put("deep_color", colorSchema.getDeepColor());
                    pageVariables.put("base_color", colorSchema.getBaseColor());
                    pageVariables.put("text_color", colorSchema.getTextColor());
                    pageVariables.put("half_color", colorSchema.getHalfColor());
                    pageVariables.put("font_family", WebServer.getFontFamily());
                    break;
                default:
            }
            if (pageVariables.isEmpty()) {
                response.getOutputStream().write(Resources.getAsBytes(resourceName));
            } else {
                String result = fillTemplate(resourceName, pageVariables);
                response.getOutputStream().write(result.getBytes("UTF-8") );
            }
        } catch (Exception exception) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

    }

}
