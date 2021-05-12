package local.gongule.tools.process;

import local.gongule.Gongule;
import local.gongule.tools.ConfigFile;
import local.gongule.tools.data.Gong;
import local.gongule.tools.relays.PowerRelay;
import local.gongule.utils.Sound;
import local.gongule.utils.logging.Loggible;
import local.gongule.utils.resources.Resources;
import local.gongule.utils.system.SystemUtils;

import java.io.File;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GongSound extends Thread implements Loggible {

    private Gong gong;

    private String eventName;

    public GongSound() {
        this.gong = null;
        this.eventName = null;
    }

    public GongSound(Gong gong, String eventName) {
        this.gong = gong;
        this.eventName = eventName;
    }

    @Override
    public void run() {
        String threadName = "SoundThread" + Thread.currentThread().getId();
        Thread.currentThread().setName(threadName);
        keepPowerRelay = false; // default value
        if (currentGongSoundThread != null) {
            keepPowerRelay = (gong != null); // keep PowerRelay as ON after the end of currentGongSoundThread
            logger.trace("{} | Try interrupt thread id={}", threadName, currentGongSoundThread.getId());
            currentGongSoundThread.interrupt();
        }
        if (sound.isPlaying()) {
            rearSemaphore = false;
            logger.trace("{} | Try sound stop", threadName);
            sound.stop();
        }
        if (gong == null) return;
        lock.lock();
        currentGongSoundThread = Thread.currentThread();
        try {
            rearSemaphore = true;
            if (SystemUtils.isRaspbian)
                if (!powerRelay.get()) {
                    powerRelay.set(true);
                    sleep(getAdvanceTime() * 1000);
                }
            logger.info("{}. {} gong is started", eventName, gong.name);
            int maxVolumeNumber = getMaxVolumeNumber();
            int minVolumePercent = getMinVolumeLevel();
            int volumeNumber = (maxVolumeNumber > gong.amount) ? gong.amount : maxVolumeNumber;
            for (int i = 0; i < gong.amount; i++) {
                double volume = 1.0;
                if ((gong.amount > 1)&&(volumeNumber > 1))
                    volume = (minVolumePercent + (100.0 - minVolumePercent) * i/(volumeNumber-1))/100.0;
                logger.trace("{} | Strike №{} of {} gong - volume is {}", threadName, i + 1, gong.name, String.format("%1.2f", volume));
                sound.setVolume(volume);
                sound.play(true);
                sound.join();
                if (!rearSemaphore) {
                    logger.trace("{} | Break by rear semaphore", threadName);
                    break;
                }
                if (i < gong.amount - 1) // if iteration don't the last
                    {
                        logger.trace("{} | Sleep to strikes delay", threadName);
                        sleep(getStrikesDelay() * 1000);
                        logger.trace("{} | Waked from strikes delay", threadName);
                    }
            }
        } catch (InterruptedException exception) {
            logger.trace("{} | Interrupted from advance time", "SoundThread" + Thread.currentThread().getId());
        }
        logger.info("{}. {} gong is finished", eventName, gong.name);
        if (SystemUtils.isRaspbian)
            if (!keepPowerRelay)
                powerRelay.getInstance().set(false);
        currentGongSoundThread = null;
        lock.unlock();
    }


    static private final PowerRelay powerRelay = PowerRelay.getInstance();

    static public final Sound sound = new Sound(getGongFile().getPath());

    static private volatile Thread currentGongSoundThread = null;

    static private volatile boolean rearSemaphore = true;

    static private volatile boolean keepPowerRelay = false;

    static private volatile Lock lock = new ReentrantLock();

    /**
     * A power turn-on advance time in seconds (for audio-amplifier)
     */
    static private int advanceTime = ConfigFile.getInstance().get("advanceTime", 20);

    static public int getAdvanceTime() {
        return SystemUtils.isRaspbian ? advanceTime : 0;
    }

    static public boolean setAdvanceTime(int time) {
        if (advanceTime == time) return false;
        logger.info("Amplifier power turn-on advance time is changed from {} to {} second(s)", advanceTime, time);
        advanceTime = time;
        ConfigFile.getInstance().set("advanceTime", advanceTime);
        if (SystemUtils.isRaspbian) GongExecutor.reset();
        return true;
    }

    /**
     * Delay between gongs in seconds for multiple strikes
     */
    static private int strikesDelay = ConfigFile.getInstance().get("strikesDelay", 0);

    static public int getStrikesDelay() {
        return strikesDelay;
    }

    static public boolean setStrikesDelay(Integer delay) {
        if (delay == null) return false;
        if (strikesDelay == delay) return false;
        logger.info("Delay between gong strikes is changed from {} to {} second(s)", strikesDelay, delay);
        strikesDelay = delay;
        ConfigFile.getInstance().set("strikesDelay", strikesDelay);
        return true;
    }

    static private int minVolumeLevel = ConfigFile.getInstance().get("minVolumeLevel", 50);

    static public int getMinVolumeLevel() {
        return minVolumeLevel;
    }

    static public boolean setMinVolumeLevel(Integer volumeLevel) {
        if (volumeLevel == null) return false;
        if (minVolumeLevel == volumeLevel) return false;
        logger.info("Volume level percent of first gong is changed from {} to {} second(s)", minVolumeLevel, volumeLevel);
        minVolumeLevel = volumeLevel;
        ConfigFile.getInstance().set("minVolumeLevel", minVolumeLevel);
        return true;
    }

    static private int maxVolumeNumber = ConfigFile.getInstance().get("maxVolumeNumber", 7);

    static public int getMaxVolumeNumber() {
        return maxVolumeNumber;
    }

    static public boolean setMaxVolumeNumber(Integer volumeNumber) {
        if (volumeNumber == null) return false;
        if (maxVolumeNumber == volumeNumber) return false;
        logger.info("Maximum volume gong number is changed from {} to {} second(s)", maxVolumeNumber, volumeNumber);
        maxVolumeNumber = volumeNumber;
        ConfigFile.getInstance().set("maxVolumeNumber", maxVolumeNumber);
        return true;
    }

    static private File gongFile = null;

    static public File getGongFile() {
        if (gongFile == null)
            // Extract wav-file from jar-package to jar-directory
            return Resources.getAsFile("wav/gong.wav", Gongule.projectName + ".wav", true);
        else
            return gongFile;
    }

}
