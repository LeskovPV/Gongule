package local.gongule.webserver;

import local.gongule.tools.RuntimeConfiguration;
import local.gongule.webserver.servlets.LogServlet;
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
    private static String keyStore = "keystore";

    public static void setKeyStore(String value) {
        if (value == null) return;
        keyStore = value;
    }

    ////////////////////////////////////////////////////////////////
    private static String storePassword = "staffbots";

    public static void setStorePassword(String value) {
        if (value == null) return;
        storePassword = value;
    }

    ////////////////////////////////////////////////////////////////
    private static String managerPassword = "staffbots";

    public static void setManagerPassword(String value) {
        if (value == null) return;
        managerPassword = value;
    }

    ////////////////////////////////////////////////////////////////
    //private static ColorSchema сolorSchema = new ColorSchema(RuntimeConfiguration.getInstance().get("BaseColor"));

    public static void setBaseColor(String baseColor) {
        RuntimeConfiguration.getInstance().set("BaseColor", baseColor);
        ColorSchema.getInstance().setBaseColor(baseColor);
    }

    ////////////////////////////////////////////////////////////////
    private static int fontIndex = getFontIndex();

    public static int getFontIndex(boolean byRuntimeConfiguration) {
        return byRuntimeConfiguration ? getFontIndex() : fontIndex;
    }

    public static int getFontIndex() {
        try {
            fontIndex = Integer.valueOf(RuntimeConfiguration.getInstance().get("FontIndex"));
        } catch (Exception exception) {
            fontIndex = 0;
        }
        return fontIndex;
    }

    public static String getFontFamily() {
        return FontFamily.values.get(fontIndex);
    }

    public static void setFontIndex(int index) {
        RuntimeConfiguration.getInstance().set("FontIndex", String.valueOf(index));
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
        String keyStorePath = Resources.getAsFile(keyStore, Gongule.projectName + ".key").getPath();
        // HTTPS configuration
        HttpConfiguration https = new HttpConfiguration();
        https.addCustomizer(new SecureRequestCustomizer());            // Configuring SSL
        SslContextFactory sslContextFactory = new SslContextFactory();
        //SslContextFactory sslContextFactory = new JettySslContextFactory(configuration.getSslProviders());
        // Defining keystore path and passwords
        sslContextFactory.setKeyStorePath(keyStorePath);
        sslContextFactory.setKeyStorePassword(storePassword);
        sslContextFactory.setKeyManagerPassword(managerPassword);
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
        ServletHolder MainServletHolder = new ServletHolder(new MainServlet());
        context.addServlet(MainServletHolder,"");
        context.addServlet(MainServletHolder,"/ui");
        context.addServlet(new ServletHolder(new ResourceServlet()),"/resource");
        context.addServlet(new ServletHolder(new LogServlet()),"/log");
        HandlerList handlers = new HandlerList();
        handlers.addHandler(context);
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
