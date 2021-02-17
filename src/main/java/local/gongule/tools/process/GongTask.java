package local.gongule.tools.process;

import local.gongule.Gongule;
import local.gongule.tools.data.Gong;
import local.gongule.utils.logging.Loggible;

import java.util.TimerTask;

public class GongTask extends TimerTask implements Loggible {

    Gong gong;

    public GongTask(Gong gong) {
        this.gong = gong;
    }

    @Override
    public void run() {
        logger.info("Play {} gong", gong.name);
        //for(int i=0; i<gong.amount; i++) {
            Sound.playSound(Gongule.getGongFile().getAbsolutePath());
            try {
                Thread.sleep(1000 * 7);
            } catch (Exception exception) {

            }
        //}
    }

}
