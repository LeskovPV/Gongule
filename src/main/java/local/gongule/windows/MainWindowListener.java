package local.gongule.windows;

import local.gongule.tools.Log;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class MainWindowListener implements WindowListener {

    public void windowActivated(WindowEvent event) {

    }

    public void windowClosed(WindowEvent event) {

    }

    public void windowClosing(WindowEvent event) {
        Log.printWarn("Closing window and ending GongA");
    }

    public void windowDeactivated(WindowEvent event) {

    }

    public void windowDeiconified(WindowEvent event) {

    }

    public void windowIconified(WindowEvent event) {

    }

    public void windowOpened(WindowEvent event) {

    }

}
