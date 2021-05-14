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

import java.io.File;
import java.io.IOException;
import java.net.*;

public class WebServer implements Loggible {

    ////////////////////////////////////////////////////////////////
    static private int httpPort = 80;

    static public int getHttpPort() {
        return httpPort;
    }

    static public void setHttpPort(Integer value) {
        if (value == null) return;
        httpPort = value;
    }

    ////////////////////////////////////////////////////////////////
    static private int httpsPort = 443;

    static public void setHttpsPort(Integer value) {
        if (value == null) return;
        httpsPort = value;
    }

    ////////////////////////////////////////////////////////////////
    static private boolean useHttp = true;

    static public boolean getUseHttp() {
        return useHttp;
    }

    static public void setUseHttp(Boolean value) {
        if (value == null) return;
        useHttp = value;
    }

    ////////////////////////////////////////////////////////////////
    static private String keyStoreFile = "keystore"; // in jar-package
    static private String keyStorePath = null; // full file name in work dir


    static public void setKeyStoreFile(String value) {
        keyStoreFile = value;
        keyStorePath = Resources.getAsFile(keyStoreFile, Gongule.projectName + ".key", false).getPath();
    }

    static public void updateKeyStoreFile() {
        logger.info("Update keystore file from jar-package: {}", Resources.getJarDirName() + Gongule.projectName + ".key");
        keyStorePath = Resources.getAsFile(keyStoreFile, Gongule.projectName + ".key", true).getPath();
    }

    ////////////////////////////////////////////////////////////////
    static private String keyStorePassword = "13213455";

    static public void setKeyStorePassword(String value) {
        if (value == null) return;
        keyStorePassword = value;
    }

    ////////////////////////////////////////////////////////////////
    static private String keyManagerPassword = "13213455";

    static public void setKeyManagerPassword(String value) {
        if (value == null) return;
        keyManagerPassword = value;
    }

    ////////////////////////////////////////////////////////////////
    static private int fontIndex = ConfigFile.getInstance().get("FontIndex", 2);

    static public int getFontIndex() {
        return fontIndex;
    }

    static public String getFontFamily() {
        return FontFamily.values.get(fontIndex);
    }

    static public void setFontIndex(int index) {
        ConfigFile.getInstance().set("FontIndex", String.valueOf(index));
        fontIndex = index;
    }

    ////////////////////////////////////////////////////////////////
    static private void checkPort(int port) {
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

    static private ServerConnector getHttpConnector() {
        checkPort(httpPort);
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(httpPort);
        return connector;
    }

    static private ServerConnector getHttpsConnector() {
        checkPort(httpsPort);
        //String keyStorePath = Resources.getAsFile(keyStoreFile, Gongule.projectName + ".key", true).getPath();
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

    static private Connector[] getConnectors() {
        return useHttp ?
               new Connector[] {getHttpsConnector(), getHttpConnector()} :
               new Connector[] {getHttpsConnector()};
    }

    static public URL getLocalURL(){
        try {
            return getUseHttp() ?
                new URL("http://localhost:" + httpPort) :
                new URL("https://localhost:" + httpsPort);
        } catch (MalformedURLException exception) {
            return null;
        }
    }

    static private Server server = null;

    static public void start() {
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

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        ServletHolder mainServletHolder = new ServletHolder(new MainServlet());
        context.addServlet(mainServletHolder,"");
        context.addServlet(mainServletHolder,"/ui");
        context.addServlet(new ServletHolder(new ResourceServlet()),"/resource");
        context.addServlet(new ServletHolder(new DownloadServlet()),"/download");

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
