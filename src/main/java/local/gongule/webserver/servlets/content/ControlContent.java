package local.gongule.webserver.servlets.content;

import org.eclipse.jetty.client.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class ControlContent extends Content{

    public ControlContent() {
//        actions.put("run_button", (HttpServletRequest request) -> SetupSaveBtnClick(request));
//        actions.put("pause_button", (HttpServletRequest request) -> SetupSaveBtnClick(request));
    }

    public String get(HttpServletRequest request) {
        Map<String, Object> contentVariables = new HashMap();
        return super.get(contentVariables);
    }

}
