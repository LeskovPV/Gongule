package local.gongule.utils.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.util.Collection;

public class UploadServlet extends HttpServlet {

    //protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    //}
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<!DOCTYPE html><html lang=\"en\"><head><title>File Upload</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head><body><form method=\"POST\" action=\"upload\" enctype=\"multipart/form-data\" >File:<input type=\"file\" name=\"file\" id=\"file\" /> <br/></br><input type=\"submit\" value=\"Upload\" name=\"upload\" id=\"upload\" /></form></body></html>");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        // Create path components to save the file
        final String path = "/home/temp/";
        final Part filePart = request.getPart("file");
        final Collection<Part> fileParts = request.getParts();
        final String fileName = getFileName(filePart);

        OutputStream out = null;
        InputStream filecontent = null;
        final PrintWriter writer = response.getWriter();

        try {
            out = new FileOutputStream(new File(path + File.separator + fileName));
            filecontent = filePart.getInputStream();

            int read = 0;
            final byte[] bytes = new byte[1024];

            while ((read = filecontent.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            System.out.println("File being uploaded to {1}");
        } catch (FileNotFoundException fne) {
            writer.println("You either did not specify a file to upload or are "
                    + "trying to upload a file to a protected or nonexistent " + "location.");
            writer.println("<br/> ERROR: " + fne.getMessage());

            System.out.println("Problems during file upload");
            fne.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
            if (filecontent != null) {
                filecontent.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
    }

    private String getFileName(final Part part) {
        final String partHeader = part.getHeader("content-disposition");
        System.out.println("Part Header = " + partHeader);
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }


}
