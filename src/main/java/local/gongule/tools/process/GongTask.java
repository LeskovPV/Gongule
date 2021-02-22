package local.gongule.tools.process;

import local.gongule.Gongule;
import local.gongule.tools.data.Gong;
import local.gongule.utils.Sound;
import local.gongule.utils.logging.Loggible;

import java.util.TimerTask;

public class GongTask extends TimerTask implements Loggible {

    Gong gong;

    public GongTask(Gong gong) {
        this.gong = gong;
    }

    @Override
    public void run() {
        GongSound.play(gong);
    }

}
