package local.gongule.tools.process;

import local.gongule.Gongule;
import local.gongule.tools.Log;
import local.gongule.tools.data.Gong;
import java.util.TimerTask;

public class GongTask extends TimerTask {

    Gong gong;

    public GongTask(Gong gong) {
        this.gong = gong;
    }

    @Override
    public void run() {
        Log.printInfo("Play " + gong.name + " gong");
        //for(int i=0; i<gong.amount; i++) {
            Sound.playSound(Gongule.getGongFile().getAbsolutePath());
            try {
                Thread.sleep(1000 * 7);
            } catch (Exception exception) {

            }
        //}
    }

}
