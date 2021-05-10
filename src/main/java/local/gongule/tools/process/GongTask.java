package local.gongule.tools.process;

import local.gongule.tools.data.Gong;
import local.gongule.tools.relays.PowerRelay;
import local.gongule.utils.logging.Loggible;
import local.gongule.utils.system.SystemUtils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;

public class GongTask implements Runnable, Loggible {

    static private volatile Thread currentTask = null;

    static private volatile Lock lock = new ReentrantLock();

    private Gong gong;

    private String eventName;

    public GongTask(Gong gong, String eventName) {
        this.gong = gong;
        this.eventName = eventName;
    }

    @Override
    public void run() {
        try {
            GongExecutor.sound.stop();
            if (currentTask != null)
                currentTask.interrupt();
                //currentTask.join();
            lock.lock();
            currentTask = Thread.currentThread();
            String threadName = "SoundThread" + Thread.currentThread().getId();
            Thread.currentThread().setName(threadName);
            if (SystemUtils.isRaspbian) {
                PowerRelay.getInstance().set(true);
                sleep(GongExecutor.getAdvanceTime() * 1000);
            }
            logger.info("{}. {} gong is started", eventName, gong.name);
            int maxVolumeNumber = GongExecutor.getMaxVolumeNumber();
            int minVolumePercent = GongExecutor.getMinVolumeLevel();
            int volumeNumber = (maxVolumeNumber > gong.amount) ? gong.amount : maxVolumeNumber;
            for (int i = 0; i < gong.amount; i++) {
                double volume = 1.0;
                if ((gong.amount > 1)&&(volumeNumber > 1))
                    volume = (minVolumePercent + (100.0 - minVolumePercent) * i/(volumeNumber-1))/100.0;
                logger.trace("{} | Strike №{} of {} gong - volume is {}", threadName, i + 1, gong.name, String.format("%1.2f", volume));
                GongExecutor.sound.setVolume(volume);
                GongExecutor.sound.play(true);
                GongExecutor.sound.join();
                if (i < gong.amount - 1) // if iteration don't the last
                    {
                        logger.trace("{} | Sleep to strikes delay", threadName);
                        sleep(GongExecutor.getStrikesDelay() * 1000);
                        logger.trace("{} | Waked from strikes delay", threadName);
                    }
            }
            logger.info("{}. {} gong is finished", eventName, gong.name);
            if (SystemUtils.isRaspbian)
                PowerRelay.getInstance().set(false);
            currentTask = null;
            lock.unlock();
        } catch (InterruptedException exception) {
            logger.trace("{} | Interrupted from advance time", "SoundThread" + Thread.currentThread().getId());
        }
    }


}
