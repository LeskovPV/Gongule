package local.gongule.tools.process;

import local.gongule.Gongule;
import local.gongule.tools.data.Gong;
import local.gongule.utils.Sound;
import local.gongule.utils.logging.Loggible;

public class GongSound extends Thread implements Loggible {

    private Gong gong;

    private GongSound(Gong gong){
        this.gong = gong;
        start();
    }

    private void cancel() {
        if (!isAlive()) return;
        if (sound.isPlaying())
            sound.stop();
        interrupt();
        try {
            join();
        } catch (InterruptedException exception) {}
    }

    @Override
    public void run() {
        for (int i = 0; i < gong.amount; i++) {
            logger.info("Paying № {} of {} gong", i, gong.name);
            sound.play(true);
            sound.join();
            logger.info("End of paying {} gong", gong.name);
            try {
                sleep(delay * 1000);
            } catch (InterruptedException exception) {
                logger.info("Unsleeped");
                break;
            }
        }
    }

    /**
     * Delay between gongs in seconds for multiple strikes
     */
    private static int delay = 3;

    public static int getDelay() {
        return delay;
    }

    public static void setDelay(Integer delay) {
        if (delay!= null)
            GongSound.delay = delay;
    }

    private static final Sound sound = new Sound(Gongule.getGongFile().getPath());

    private static GongSound instance;

    public static void play(Gong gong) {
        if (instance != null) instance.cancel();
        if (gong != null) instance = new GongSound(gong);
    }

}
