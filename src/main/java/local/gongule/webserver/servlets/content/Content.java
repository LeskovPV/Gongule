package local.gongule.webserver.servlets.content;

import local.gongule.utils.TemplateFillable;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class Content implements TemplateFillable {

    public PageType pageType;

    /**
     * Map of actions on page for search method by name
     */
    public Map<String, Function<HttpServletRequest, Boolean>> actions = new HashMap();

    /**
     * Apply action by name
     */
    public boolean applyAction(HttpServletRequest request) {
        String actionName = request.getParameter("action");
        return actions.containsKey(actionName) ? actions.get(actionName).apply(request) : false;
    }

    public void setPageType(PageType pageType) {
        this.pageType = pageType;
    }

    public abstract String get(HttpServletRequest request);

    protected String getFromTemplate(Map<String, Object> contentVariables) {
        return fillTemplate("html/content/" + pageType.getName().toLowerCase() + ".html", contentVariables);
    }

    protected String getAttribute(HttpServletRequest request, String attribute, String defaultValue){
        Object attributeValue = request.getSession().getAttribute(attribute);
        String value = (attributeValue == null) ? "" : attributeValue.toString();
        if (value.isEmpty())
            value = (defaultValue == null) ? request.getParameter(attribute) : defaultValue;
        if (value == null) value = "";
        setAttribute(request, attribute, value);
        return value;
    }

    protected String getAttribute(HttpServletRequest request, String attribute){
        return getAttribute(request, attribute, null);
    }

    protected void setAttribute(HttpServletRequest request, String attribute, String value){
        request.getSession().setAttribute(attribute, value);
    }

    protected String setAttribute(HttpServletRequest request, String attribute){
        String value = request.getParameter(attribute);
        setAttribute(request, attribute, value);
        return value;
    }

}
