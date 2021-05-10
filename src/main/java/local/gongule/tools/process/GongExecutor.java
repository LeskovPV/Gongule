package local.gongule.tools.process;

import local.gongule.Gongule;
import local.gongule.tools.ConfigFile;
import local.gongule.tools.data.Data;
import local.gongule.tools.data.Day;
import local.gongule.tools.data.Gong;
import local.gongule.utils.Sound;
import local.gongule.utils.logging.Loggible;
import local.gongule.utils.resources.Resources;
import local.gongule.utils.system.SystemUtils;

import java.io.File;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GongExecutor implements Loggible {

    static private ScheduledExecutorService scheduledService = null;

    static private ExecutorService cachedService = null;

    static public void init() {
        getMinVolumeLevel();
        getMaxVolumeNumber();
        if (ConfigFile.getInstance().get("processIsPaused", false))
            pauseProcess();
        else
            runProcess();
        runMidnightReset();
    }

    static public boolean processIsPaused(){
        return (scheduledService == null);
    }

    static public boolean processIsRun(){
        return (scheduledService != null);
    }

    static public void runProcess() {
        runProcess(true);
    }

    synchronized static public void runProcess(boolean withLog) {
        Data data = Data.getInstance();
        int todayIndex = data.getCurrentDayIndex();
        if (todayIndex < 0)
            scheduledService = Executors.newScheduledThreadPool(0);
        else {
            List<Day.Event> todayEvents = new ArrayList(0);
            LocalTime now = LocalTime.now();
            for (Day.Event event : data.getDay(todayIndex).events) {
                LocalTime realEventTime = event.time.minusSeconds(getAdvanceTime());
                int seconds = realEventTime.toSecondOfDay() - now.toSecondOfDay();
                if (seconds < 0) continue;
                Day.Event realEvent = new Day.Event(realEventTime, event.name, event.gongIndex);
                todayEvents.add(realEvent);
            }
            if (scheduledService != null) pauseProcess();
            scheduledService = Executors.newScheduledThreadPool(todayEvents.size());
            for (Day.Event realEvent : todayEvents) {
                Gong gong = data.getGong(realEvent.gongIndex);
                GongTask gongTask = new GongTask(gong, realEvent.name);
                int seconds = realEvent.time.toSecondOfDay() - now.toSecondOfDay();
                scheduledService.schedule(gongTask, seconds, TimeUnit.SECONDS);
            }
        }
        ConfigFile.getInstance().set("processIsPaused", false);
        if (withLog) logger.warn("Process is ran");
    }

    static public void pauseProcess() {
        pauseProcess(true);
    }

    synchronized static public void pauseProcess(boolean withLog) {
        if (scheduledService == null) return;
        //GongSound.play(null, null);
        scheduledService.shutdownNow();
        scheduledService = null;
        ConfigFile.getInstance().set("processIsPaused", true);
        if (withLog) logger.warn("Process is paused");
    }

    static public void playSingleGong(Gong gong) {
        playSingleGong(gong, true);
    }



    synchronized static public void playSingleGong(Gong gong, boolean withLog) {
        cachedService = Executors.newCachedThreadPool();
        cachedService.execute(new GongTask(gong, "Manual running"));
        cachedService.shutdown();
    }

    static public void stopCurrentGong() {
        stopCurrentGong(true);
    }

    synchronized static public void stopCurrentGong(boolean withLog) {
        cachedService.shutdownNow();
    }


    static public void reset() {
        reset(true);
    }

    static public void reset(boolean withLog) {
        boolean processNeedRun = processIsRun();
        pauseProcess(false);
        if (processNeedRun) runProcess(false);
        if (withLog) logger.info("Gong schedule reset");
    }

    static private Timer midnightTimer;

    static public void runMidnightReset() {
        long period = 1000 * 60 * 60 * 24; // one day in milliseconds
        long delay = period - 1000 * (LocalTime.now().toSecondOfDay() - 1); // milliseconds to 00:00:01
        if (midnightTimer != null) midnightTimer.cancel();
        midnightTimer = new Timer();
        midnightTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                reset(false);
                logger.info("Midnight reset. Scheduling on new day");
            }
        }, delay, period);
        logger.info("Midnight reset is planed");
    }

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
        if (SystemUtils.isRaspbian) reset();
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

    static public final Sound sound = new Sound(getGongFile().getPath());

    static private File gongFile = null;

    static public File getGongFile() {
        if (gongFile == null)
            // Extract wav-file from jar-package to jar-directory
            return Resources.getAsFile("wav/gong.wav", Gongule.projectName + ".wav", true);
        else
            return gongFile;
    }

}
