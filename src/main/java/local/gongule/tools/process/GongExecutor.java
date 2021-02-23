package local.gongule.tools.process;

import local.gongule.Gongule;
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

    static public boolean processIsPaused(){
        return (service == null);
    }

    static public void run() {
        int todayIndex = Gongule.getData().getCurrentDayIndex();
        if (todayIndex < 0) return;
        List<Day.Event> todayEvents = new ArrayList();
        LocalTime now = LocalTime.now();
        for (Day.Event event: Gongule.getData().getDay(todayIndex).events) {
            LocalTime realEventTime = event.time.minusSeconds(getAdvanceTime());
            int seconds = realEventTime.toSecondOfDay() - now.toSecondOfDay() ;
            if (seconds < 0) continue;
            Day.Event realEvent = new Day.Event(realEventTime, event.name, event.gongIndex);
            todayEvents.add(realEvent);
        }
        if (service != null) pause();
        service = Executors.newScheduledThreadPool(todayEvents.size());
        for (Day.Event realEvent: todayEvents) {
            Gong gong = Gongule.getData().getGong(realEvent.gongIndex);
            GongTask gongTask = new GongTask(gong);
            int seconds = realEvent.time.toSecondOfDay() - now.toSecondOfDay();
            service.schedule(gongTask, seconds, TimeUnit.SECONDS);
        }
        logger.warn("Process ran");
    }

    static public void pause() {
        if (service == null) return;
        GongSound.play(null);
        service.shutdownNow();
        logger.warn("Process paused");
        service = null;
    }


    static public void reset() {
        boolean isRun = (service != null);
        pause();
        if(isRun) run();
    }

    private static Timer midnightTimer;

    public static void midnightReset() {
        long period = 1000 * 60 * 60 * 24; // one day in milliseconds
        long delay = period - 1000 * (LocalTime.now().toSecondOfDay() - 1); // milliseconds to 00:00:01
        if (midnightTimer != null)
            midnightTimer.cancel();
        else
            midnightTimer = new Timer();
        midnightTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                logger.trace("Scheduling on new day (at midnight)");
                reset();
            }
        }, delay, period);
    }

    /**
     * A power turn-on advance time in seconds (for audio-amplifier)
     */
    private static int advanceTime = 20;

    public static int getAdvanceTime() {
        return SystemUtils.isRaspbian ? advanceTime : 0;
    }

    public static void setAdvanceTime(Integer advanceTime) {
        if (advanceTime != null)
            GongExecutor.advanceTime = advanceTime;
    }
}
