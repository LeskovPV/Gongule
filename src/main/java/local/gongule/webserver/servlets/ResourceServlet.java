package local.gongule.webserver.servlets;

import local.gongule.utils.TemplateFillable;
import local.gongule.utils.resources.ResourceType;
import local.gongule.utils.resources.Resources;
import local.gongule.utils.colors.ColorSchema;
import local.gongule.webserver.WebServer;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class ResourceServlet extends HttpServlet implements TemplateFillable {

    /**
     * Private resources list
     */
    public static final List<String> privateResources = Arrays.asList("keystore");

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        String[] requests = request.getQueryString().split("&");
        String resourceName = requests.length > 0 ? requests[0] : null;
        if (resourceName == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        if (privateResources.contains(resourceName)) return;
        try {
            Map<String, Object> pageVariables = new HashMap(0);
            switch (ResourceType.getByName(resourceName)){
                case CSS:
                    ColorSchema colorSchema = ColorSchema.getInstance();
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
                response.getOutputStream().write(result.getBytes("UTF-8"));
            }
        } catch (Exception exception) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

    }

}
