package local.gongule.tools.process;

import local.gongule.tools.data.Gong;
import local.gongule.utils.logging.Loggible;

import java.util.TimerTask;

public class GongTask extends TimerTask implements Loggible {

    Gong gong;

    String eventName;

    public GongTask(Gong gong, String eventName) {
        this.gong = gong;
        this.eventName = eventName;
    }

    @Override
    public void run() {
        GongSound.play(gong, eventName);
    }

}
