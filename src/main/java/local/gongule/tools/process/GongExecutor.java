package local.gongule.tools.process;

import local.gongule.Gongule;
import local.gongule.tools.data.Day;
import local.gongule.tools.data.Gong;
import local.gongule.utils.logging.Loggible;

import java.time.LocalTime;
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
        Day today = Gongule.getData().getDay(todayIndex);
        if (service != null) pause();
        service = Executors.newScheduledThreadPool(today.events.size());
        for (Day.Event event: Gongule.getData().getDay(todayIndex).events) {
            int seconds = event.time.toSecondOfDay() - LocalTime.now().toSecondOfDay();
            if (seconds < 0) continue;
            Gong gong = Gongule.getData().getGong(event.gongIndex);
            GongTask gongTask = new GongTask(gong);
            service.schedule(gongTask, seconds, TimeUnit.SECONDS);
        }
        logger.warn("Process ran");
    }

    static public void pause() {
        if (service == null) return;
        service.shutdownNow();
        logger.warn("Process paused");
        service = null;
    }

}
