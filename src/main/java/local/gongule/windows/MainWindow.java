package local.gongule.windows;

import java.awt.*;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JButton;
import local.gongule.utils.logging.Loggible;
import local.gongule.utils.resources.Resources;
import local.gongule.webserver.WebServer;

/*
 * The main application window,
 * Contains only the "Management" button to go to the web interface,
 * Closing window terminates the entire application
 */
public class MainWindow extends JFrame implements Loggible {

    /*
     * The single instance of the class
     */
    private static MainWindow instance = null;

    public static MainWindow getInstance() {
        return instance;
    }

    /*
     * Initializing a single instance of a class - {@code mainWindow}
     */
    synchronized public static void open(String windowTilte) {
        if (instance != null)
            return;
        instance = new MainWindow(windowTilte);
        logger.info("Open window");
    }

    /*
     * Constructor,
     * contains designing and launching the main application window in a separate thread
     */
    private MainWindow(String tilte) {
        super(tilte);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(new MainWindowListener());
        setIconImage(Resources.getAsImage("png/icon.png"));
        Container container = getContentPane();
        container.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 25));
        JButton button = new JButton("Management");
        button.setToolTipText("Management by browser");

        button.addActionListener(e -> {
            try {
                openBrowse(WebServer.getLocalURL());
            } catch (Exception exception) {
                logger.error("Unpossible open browser: {}", exception);
            }
        });
        container.add(button);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = 250;
        int height = 100;
        int locationX = (screenSize.width - width) / 2;
        int locationY = (screenSize.height - height) / 2;
        setBounds(locationX, locationY, width, height);
        setResizable(false);
        setSize(width, height);
        setVisible(true); // Open window
        //button.doClick(); // Run browser for management
    }

    private void openBrowse(URL url) throws Exception{
        if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
            Desktop.getDesktop().browse(url.toURI());
        else {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("xdg-open " + url);
        }
    }

}
