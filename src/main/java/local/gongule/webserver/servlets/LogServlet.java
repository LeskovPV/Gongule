package local.gongule.webserver.servlets;

import local.gongule.utils.logging.LogService;
import local.gongule.utils.servlets.DownloadServlet;
import java.io.IOException;
import java.io.File;

public class LogServlet extends DownloadServlet {

    @Override
    protected File getFile() throws IOException{
        return LogService.getAllLogFile();
    }

}