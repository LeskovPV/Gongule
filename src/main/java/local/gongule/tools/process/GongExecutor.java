package local.gongule.tools.process;

import local.gongule.tools.ConfigFile;
import local.gongule.tools.data.Data;
import local.gongule.tools.data.Day;
import local.gongule.tools.data.Gong;
import local.gongule.utils.logging.Loggible;
import local.gongule.utils.system.SystemUtils;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GongExecutor implements Loggible {

    static private ScheduledExecutorService service = null;

    static public void init() {
        if (ConfigFile.getInstance().get("processIsPaused", false))
            pause();
        else
            run();
        runMidnightReset();
    }

    static public boolean processIsPaused(){
        return (service == null);
    }

    static public void run() {
        run(true);
    }

    synchronized static public void run(boolean withLog) {
        Data data = Data.getInstance();
        int todayIndex = data.getCurrentDayIndex();
        if (todayIndex < 0) service = Executors.newScheduledThreadPool(0); else {
            List<Day.Event> todayEvents = new ArrayList(0);
            LocalTime now = LocalTime.now();
            for (Day.Event event : data.getDay(todayIndex).events) {
                LocalTime realEventTime = event.time.minusSeconds(getAdvanceTime());
                int seconds = realEventTime.toSecondOfDay() - now.toSecondOfDay();
                if (seconds < 0) continue;
                Day.Event realEvent = new Day.Event(realEventTime, event.name, event.gongIndex);
                todayEvents.add(realEvent);
            }
            if (service != null) pause();
            service = Executors.newScheduledThreadPool(todayEvents.size());
            for (Day.Event realEvent : todayEvents) {
                Gong gong = data.getGong(realEvent.gongIndex);
                GongTask gongTask = new GongTask(gong, realEvent.name);
                int seconds = realEvent.time.toSecondOfDay() - now.toSecondOfDay();
                service.schedule(gongTask, seconds, TimeUnit.SECONDS);
            }
        }
        ConfigFile.getInstance().set("processIsPaused", false);
        if (withLog) logger.warn("Process is ran");
    }

    static public void pause() {
        pause(true);
    }

    synchronized static public void pause(boolean withLog) {
        if (service == null) return;
        GongSound.play(null, null);
        service.shutdownNow();
        service = null;
        ConfigFile.getInstance().set("processIsPaused", true);
        if (withLog) logger.warn("Process is paused");
    }
    static public void reset() {
        reset(true);
    }

    static public void reset(boolean withLog) {
        boolean isRun = (service != null);
        pause(false);
        if(isRun) run(false);
        if (withLog) logger.info("Gong schedule reset");
    }

    private static Timer midnightTimer;

    public static void runMidnightReset() {
        long period = 1000 * 60 * 60 * 24; // one day in milliseconds
        long delay = period - 1000 * (LocalTime.now().toSecondOfDay() - 1); // milliseconds to 00:00:01
        if (midnightTimer != null) midnightTimer.cancel();
        midnightTimer = new Timer();
        midnightTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                reset(false);
                logger.info("Scheduling on new day (at midnight)");
            }
        }, delay, period);
    }

    /**
     * A power turn-on advance time in seconds (for audio-amplifier)
     */
    private static int advanceTime = ConfigFile.getInstance().get("advanceTime", 20);

    public static int getAdvanceTime() {
        return SystemUtils.isRaspbian ? advanceTime : 0;
    }

    public static void setAdvanceTime(int advanceTime) {
        logger.info("Amplifier power turn-on advance time is changed from {} to {} second(s)", GongExecutor.advanceTime, advanceTime);
        GongExecutor.advanceTime = advanceTime;
        ConfigFile.getInstance().set("advanceTime", advanceTime);
        if (SystemUtils.isRaspbian) reset();
    }

}
