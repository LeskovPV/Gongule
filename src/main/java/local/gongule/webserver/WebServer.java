package local.gongule.webserver;

import local.gongule.tools.ConfigFile;
import local.gongule.webserver.servlets.DownloadServlet;
import local.gongule.utils.logging.Loggible;
import local.gongule.utils.FontFamily;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import local.gongule.Gongule;
import local.gongule.utils.resources.Resources;
import local.gongule.utils.colors.ColorSchema;
import local.gongule.webserver.servlets.MainServlet;
import local.gongule.webserver.servlets.ResourceServlet;

import java.io.IOException;
import java.net.*;

public class WebServer implements Loggible {

    ////////////////////////////////////////////////////////////////
    private static int httpPort = 80;

    public static int getHttpPort() {
        return httpPort;
    }

    public static void setHttpPort(Integer value) {
        if (value == null) return;
        httpPort = value;
    }

    ////////////////////////////////////////////////////////////////
    private static int httpsPort = 443;

    public static void setHttpsPort(Integer value) {
        if (value == null) return;
        httpsPort = value;
    }

    ////////////////////////////////////////////////////////////////
    private static boolean useHttp = true;

    public static boolean getUseHttp() {
        return useHttp;
    }

    public static void setUseHttp(Boolean value) {
        if (value == null) return;
        useHttp = value;
    }

    ////////////////////////////////////////////////////////////////
    private static String keyStoreFile = "keystore";

    public static void setKeyStoreFile(String value) {
        if (value == null) return;
        keyStoreFile = value;
    }

    ////////////////////////////////////////////////////////////////
    private static String keyStorePassword = "13213455";

    public static void setKeyStorePassword(String value) {
        if (value == null) return;
        keyStorePassword = value;
    }

    ////////////////////////////////////////////////////////////////
    private static String keyManagerPassword = "13213455";

    public static void setKeyManagerPassword(String value) {
        if (value == null) return;
        keyManagerPassword = value;
    }

    ////////////////////////////////////////////////////////////////
    //private static ColorSchema сolorSchema = new ColorSchema(RuntimeConfiguration.getInstance().get("BaseColor"));

    public static void setBaseColor(String baseColor) {
        ConfigFile.getInstance().set("BaseColor", baseColor);
        ColorSchema.getInstance().setBaseColor(baseColor);
    }

    ////////////////////////////////////////////////////////////////
    private static int fontIndex = ConfigFile.getInstance().get("FontIndex", 2);

    public static int getFontIndex() {
        return fontIndex;
    }

    public static String getFontFamily() {
        return FontFamily.values.get(fontIndex);
    }

    public static void setFontIndex(int index) {
        ConfigFile.getInstance().set("FontIndex", String.valueOf(index));
        fontIndex = index;
    }

    ////////////////////////////////////////////////////////////////
    private static void checkPort(int port) {
        try {
            Socket socket = new Socket("localhost", port);
            if (socket.isConnected()) {
                logger.error("Port {} is busy", port);
                System.exit(0);
            }
        } catch (IOException e) {
            // No actions
        }
    }

    private static ServerConnector getHttpConnector() {
        checkPort(httpPort);
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(httpPort);
        return connector;
    }

    private static ServerConnector getHttpsConnector() {
        checkPort(httpsPort);
        String keyStorePath = Resources.getAsFile(keyStoreFile, Gongule.projectName + ".key", true).getPath();
        // HTTPS configuration
        HttpConfiguration https = new HttpConfiguration();
        https.addCustomizer(new SecureRequestCustomizer());            // Configuring SSL
        SslContextFactory sslContextFactory = new SslContextFactory();
        //SslContextFactory sslContextFactory = new JettySslContextFactory(configuration.getSslProviders());
        // Defining keystore path and passwords
        sslContextFactory.setKeyStorePath(keyStorePath);
        sslContextFactory.setKeyStorePassword(keyStorePassword);
        sslContextFactory.setKeyManagerPassword(keyManagerPassword);
        // Configuring the connector
        ServerConnector connector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, "http/1.1"),
                new HttpConnectionFactory(https));
        connector.setPort(httpsPort);
        return connector;
    }

    private static Connector[] getConnectors() {
        return useHttp ?
               new Connector[] {getHttpsConnector(), getHttpConnector()} :
               new Connector[] {getHttpsConnector()};
    }

    public static URL getLocalURL(){
        try {
            return getUseHttp() ?
                new URL("http://localhost:" + httpPort) :
                new URL("https://localhost:" + httpsPort);
        } catch (MalformedURLException exception) {
            return null;
        }
    }

    private static Server server = null;

    public static void start() {
        if (server != null) { // If server already run
            try {
                server.stop();
                server.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        server = new Server();
        server.setConnectors(getConnectors());
        //AccountService accountService = new AccountService();
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        ServletHolder mainServletHolder = new ServletHolder(new MainServlet());
        context.addServlet(mainServletHolder,"");
        context.addServlet(mainServletHolder,"/ui");
        context.addServlet(new ServletHolder(new ResourceServlet()),"/resource");
        context.addServlet(new ServletHolder(new DownloadServlet()),"/download");

//        ServletHolder uploadServletHolder = new ServletHolder(new UploadServlet());
//        context.addServlet(uploadServletHolder,"/upload");

        HandlerList handlers = new HandlerList();
        handlers.addHandler(context);

//        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
//        File locationDir = new File(tmpDir, "jetty-fileupload");
//        if (!locationDir.exists()) locationDir.mkdirs();
//        String location = locationDir.getAbsolutePath();
//        long maxFileSize = 1024 * 1024 * 50;
//        long maxRequestSize = -1L;
//        int fileSizeThreshold = 1024 * 1024;
//        MultipartConfigElement multipartConfig = new MultipartConfigElement(location, maxFileSize, maxRequestSize, fileSizeThreshold);
//        uploadServletHolder.getRegistration().setMultipartConfig(multipartConfig);

        server.setHandler(handlers);
        try {
            server.start();
            logger.info("Web-server is start");
        } catch (Exception exception){
            logger.error("Unpossible start web-server: {}", exception.getMessage());
            System.exit(0);
        }
    }

}
