package local.gongule.tools.process;

import local.gongule.Gongule;
import local.gongule.tools.data.Gong;
import local.gongule.tools.relays.CoolingRelay;
import local.gongule.tools.relays.PowerRelay;
import local.gongule.utils.Sound;
import local.gongule.utils.logging.Loggible;
import local.gongule.utils.resources.Resources;

import java.io.File;

public class GongSound extends Thread implements Loggible {

    private Gong gong;

    private boolean useAdvanceTime;

    private GongSound(Gong gong, boolean useAdvanceTime){
        this.gong = gong;
        this.useAdvanceTime = useAdvanceTime;
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
        logger.info("Begin of {} gong", gong.name);
        if (useAdvanceTime) {
            PowerRelay.getInstance().set(true);
            try {
                sleep(GongExecutor.getAdvanceTime() * 1000);
            } catch (InterruptedException exception) {
                logger.info("Waked up from advance time");
            }
        } else {
            PowerRelay.getInstance().set(true);
        }
        for (int i = 0; i < gong.amount; i++) {
            logger.info("Paying № {} of {} gong", i, gong.name);
            sound.play(true);
            sound.join();
            logger.info("End of paying {} gong", gong.name);
            try {
                sleep(strikesDelay * 1000);
            } catch (InterruptedException exception) {
                logger.info("Waked up from strikes delay");
                break;
            }
        }
        PowerRelay.getInstance().set(false);
    }

    /**
     * Delay between gongs in seconds for multiple strikes
     */
    private static int strikesDelay = 3;

    public static int getStrikesDelay() {
        return strikesDelay;
    }

    public static void setStrikesDelay(Integer strikesDelay) {
        if (strikesDelay != null)
            GongSound.strikesDelay = strikesDelay;
    }

    private static final Sound sound = new Sound(getGongFile().getPath());

    private static GongSound instance;

    public static void play(Gong gong) {
        play(gong, true);
    }

    public static void play(Gong gong, boolean useAdvanceTime) {
        if (instance != null) instance.cancel();
        if (gong != null) instance = new GongSound(gong, useAdvanceTime);
    }

    private static File gongFile = null;

    public static File getGongFile() {
        if (gongFile == null)
            // Extract wav-file from jar-package to jar-directory
            return Resources.getAsFile("wav/gong.wav", Gongule.projectName + ".wav", false);
        else
            return gongFile;
    }

}
