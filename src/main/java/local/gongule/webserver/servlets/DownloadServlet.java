package local.gongule.webserver.servlets;

import local.gongule.utils.logging.LogService;
import local.gongule.utils.logging.Loggible;
import local.gongule.utils.resources.Resources;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DownloadServlet extends HttpServlet implements Loggible {

    protected File getFile(HttpServletRequest request) throws IOException {
        String queryString = request.getQueryString();
        if ("log".equalsIgnoreCase(queryString))
            return LogService.getAllLogFile();
        if ("man".equalsIgnoreCase(queryString)) {
            return Resources.getAsFile("man/manual.pdf", "Gongule.pdf", true);
        }
        return null;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        File downloadFile = getFile(request);
        if (downloadFile == null) {
            response.sendRedirect("");
            return;
        }
        FileInputStream inputStream = new FileInputStream(downloadFile);

        ServletContext context = getServletContext();

        String mimeType = context.getMimeType(downloadFile.getPath());
        if (mimeType == null) mimeType = "application/octet-stream";

        response.setContentType(mimeType);
        response.setContentLength((int) downloadFile.length());

        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
        response.setHeader(headerKey, headerValue);

        OutputStream outStream = response.getOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        outStream.close();
    }

}

