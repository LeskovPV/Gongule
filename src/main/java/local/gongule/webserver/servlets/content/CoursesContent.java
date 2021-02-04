package local.gongule.webserver.servlets.content;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class CoursesContent  extends Content{

    public CoursesContent() {
//        actions.put("save_button", (HttpServletRequest request) -> SetupSaveBtnClick(request));
    }

    public String get(HttpServletRequest request) {
        Map<String, Object> contentVariables = new HashMap();
        return super.get(contentVariables);
    }
}
