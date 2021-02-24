package local.gongule.windows;

import local.gongule.utils.logging.Loggible;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class MainWindowListener implements WindowListener, Loggible {

    public void windowActivated(WindowEvent event) { }

    public void windowClosed(WindowEvent event) { }

    public void windowClosing(WindowEvent event) {
        logger.warn("Gongule closed from application window");
    }

    public void windowDeactivated(WindowEvent event) { }

    public void windowDeiconified(WindowEvent event) { }

    public void windowIconified(WindowEvent event) { }

    public void windowOpened(WindowEvent event) { }

}
