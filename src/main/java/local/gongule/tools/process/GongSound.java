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
        int minVolumePercent = getMinVolumeLevel();
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
    private static int strikesDelay = ConfigFile.getInstance().get("strikesDelay", 0);

    public static int getStrikesDelay() {
        return strikesDelay;
    }

    public static boolean setStrikesDelay(Integer delay) {
        if (delay == null) return false;
        if (strikesDelay == delay) return false;
        logger.info("Delay between gong strikes is changed from {} to {} second(s)", strikesDelay, delay);
        strikesDelay = delay;
        ConfigFile.getInstance().set("strikesDelay", strikesDelay);
        return true;
    }

    private static int minVolumeLevel = ConfigFile.getInstance().get("minVolumeLevel", 50);

    public static int getMinVolumeLevel() {
        return minVolumeLevel;
    }

    public static boolean setMinVolumeLevel(Integer volumeLevel) {
        if (volumeLevel == null) return false;
        if (minVolumeLevel == volumeLevel) return false;
        logger.info("Volume level percent of first gong is changed from {} to {} second(s)", minVolumeLevel, volumeLevel);
        minVolumeLevel = volumeLevel;
        ConfigFile.getInstance().set("minVolumeLevel", minVolumeLevel);
        return true;
    }

    private static int maxVolumeNumber = ConfigFile.getInstance().get("maxVolumeNumber", 7);

    public static int getMaxVolumeNumber() {
        return maxVolumeNumber;
    }

    public static boolean setMaxVolumeNumber(Integer volumeNumber) {
        if (volumeNumber == null) return false;
        if (maxVolumeNumber == volumeNumber) return false;
        logger.info("Maximum volume gong number is changed from {} to {} second(s)", maxVolumeNumber, volumeNumber);
        maxVolumeNumber = volumeNumber;
        ConfigFile.getInstance().set("maxVolumeNumber", maxVolumeNumber);
        return true;
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
            return Resources.getAsFile("wav/gong.wav", Gongule.projectName + ".wav", true);
        else
            return gongFile;
    }

}
