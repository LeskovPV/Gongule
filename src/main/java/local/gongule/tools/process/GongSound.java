package local.gongule.tools.process;

import local.gongule.Gongule;
import local.gongule.tools.ConfigFile;
import local.gongule.tools.data.Gong;
import local.gongule.tools.relays.PowerRelay;
import local.gongule.utils.Sound;
import local.gongule.utils.logging.Loggible;
import local.gongule.utils.resources.Resources;

import java.io.File;

public class GongSound extends Thread implements Loggible {

    private Gong gong;

    private String eventName;

    private boolean useAdvanceTime;

    private GongSound(Gong gong, String eventName, boolean useAdvanceTime){
        this.gong = gong;
        this.useAdvanceTime = useAdvanceTime;
        this.eventName = eventName;
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
        PowerRelay.getInstance().set(true);
        if (useAdvanceTime)
            try {
                sleep(GongExecutor.getAdvanceTime() * 1000);
            } catch (InterruptedException exception) {
                logger.info("Waked up from advance time");
            }
        logger.info("{}. {} gong is playing", eventName, gong.name);
        int maxVolumeNumber = getMaxVolumeNumber();
        int minVolumePercent = getMinVolumePercent();
        int volumeNumber = (maxVolumeNumber > gong.amount) ? gong.amount : maxVolumeNumber;
        for (int i = 0; i < gong.amount; i++) {
            double volume = 100.0;
            if ((gong.amount > 1)&&(volumeNumber > 1))
                volume = (minVolumePercent + (100.0 - minVolumePercent) * i/(volumeNumber-1))/100.0;
            sound.setVolume(volume);
            sound.play(true);
            sound.join();
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
    private static int strikesDelay = ConfigFile.getInstance().get("strikesDelay", 3);

    public static int getMinVolumePercent() {
        return ConfigFile.getInstance().get("minVolumePercent", 30, 1, 100);
    }

    public static int getMaxVolumeNumber() {
        return ConfigFile.getInstance().get("maxVolumeNumber", 5, 1, 100);
    }

    /**
     * A power turn-on advance time in seconds (for audio-amplifier)
     */
    public static int getStrikesDelay() {
        return strikesDelay;
    }

    public static void setStrikesDelay(Integer strikesDelay) {
        if (strikesDelay == null) return;
        logger.info("Delay between gong strikes is changed from {} to {} second(s)", GongSound.strikesDelay, strikesDelay);
        GongSound.strikesDelay = strikesDelay;
        ConfigFile.getInstance().set("strikesDelay", strikesDelay);
    }

    private static final Sound sound = new Sound(getGongFile().getPath());

    private static GongSound instance;

    public static void play(Gong gong, String eventName) {
        play(gong, eventName,true);
    }

    public static void play(Gong gong, String eventName, boolean useAdvanceTime) {
        if (instance != null) instance.cancel();
        if (gong != null) instance = new GongSound(gong, eventName, useAdvanceTime);
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
