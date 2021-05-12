package local.gongule.tools.process;

import local.gongule.tools.ConfigFile;
import local.gongule.tools.data.Data;
import local.gongule.tools.data.Day;
import local.gongule.tools.data.Gong;
import local.gongule.utils.logging.Loggible;

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

    static public void init() {
        GongSound.getMinVolumeLevel();
        GongSound.getMaxVolumeNumber();
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
                LocalTime realEventTime = event.time.minusSeconds(GongSound.getAdvanceTime());
                int seconds = realEventTime.toSecondOfDay() - now.toSecondOfDay();
                if (seconds < 0) continue;
                Day.Event realEvent = new Day.Event(realEventTime, event.name, event.gongIndex);
                todayEvents.add(realEvent);
            }
            if (scheduledService != null) pauseProcess();
            scheduledService = Executors.newScheduledThreadPool(todayEvents.size());
            for (Day.Event realEvent : todayEvents) {
                Gong gong = data.getGong(realEvent.gongIndex);
                GongSound gongTask = new GongSound(gong, realEvent.name);
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
        scheduledService.shutdownNow();
        scheduledService = null;
        ConfigFile.getInstance().set("processIsPaused", true);
        if (withLog) logger.warn("Process is paused");
    }

    static public void playSingleGong(Gong gong) {
        playSingleGong(gong, true);
    }

    synchronized static public void playSingleGong(Gong gong, boolean withLog) {
        new GongSound(gong, "Manual running").start();
    }

    synchronized static public void stopCurrentGong() {
        new GongSound().start();
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

}
