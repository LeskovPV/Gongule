package local.gongule.webserver.servlets;

import local.gongule.Gongule;
import local.gongule.tools.Log;
import local.gongule.tools.TemplateFillable;
import local.gongule.webserver.servlets.content.*;
import local.gongule.webserver.WebServer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainServlet extends HttpServlet implements TemplateFillable {

    //private AccountService accountService;

    private Map<PageType, Content> content = new HashMap() {{
        put(PageType.CONTROL,   new ControlContent());
        put(PageType.COURSES,   new CoursesContent());
        put(PageType.DAYS,      new DaysContent());
        put(PageType.SETUP,     new SetupContent());
    }};

    public MainServlet() {
        //this.accountService = accountService;
        for (PageType pageType : PageType.values())
            content.get(pageType).setPageType(pageType);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        if(!accountService.accessIsAllow(request)){
//            response.sendRedirect("/entry");
//            return;
//        }

        String queryString = request.getQueryString();
        String[] requests = (queryString == null) ? new String[0] : request.getQueryString().split("&");

        PageType pageType = PageType.getValueOf(requests.length > 0 ? requests[0] : "");

        Map<String, Object> pageVariables = new HashMap();
        for (PageType pt : PageType.values()) {
            pageVariables.put(pt.getName() + "_disabled", pt == pageType ? "disabled" : "");
            pageVariables.put(pt.getName() + "_title", pt.getTitle());
        }

        pageVariables.put("site_title", Gongule.getFullName());
        pageVariables.put("page_name", pageType.getName());
        pageVariables.put("page_title", pageType.getTitle());
        pageVariables.put("page_content", content.get(pageType).get(request));
        pageVariables.put("website_link", Gongule.getProjectWebsite());
        pageVariables.put("deep_color", WebServer.getColorSchema().getDeepColor());

        String result = fillTemplate("html/main.html", pageVariables);

        response.getOutputStream().write( result.getBytes("UTF-8") );
        response.setContentType("text/html; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK );
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
        Log.printInfo("===================================");
        for (String name: request.getParameterMap().keySet())
            Log.printInfo(name + " = " + request.getParameter(name));
        String actionName = request.getParameter("action");
        for (PageType pageType : PageType.values()) {
            // check menu button click
            if (actionName.equals(pageType.getName() + "_menu")) {
                response.sendRedirect("/main?" + pageType.getName());
                return;
            }
            // check action on page
            if (content.get(pageType).applyAction(actionName, request)) {
                response.sendRedirect("/main?" + pageType.getName());
                return;
            }
        }

//        response.sendRedirect("/main?" + PageType.getDefault().getName());

    }

}
