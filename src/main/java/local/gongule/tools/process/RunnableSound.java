package local.gongule.tools.process;

import local.gongule.tools.data.Gong;
import local.gongule.utils.logging.Loggible;

class RunnableSound extends Thread implements Loggible {

    Sound sound;
    Gong gong;
    public Boolean stop = false;

    public RunnableSound(Sound sound, Gong gong){
        this.sound = sound;
        this.gong = gong;
    }

    @Override
    public void run(){
        for (int i = 0; i < gong.amount; i++) {
            if (stop) {
                logger.trace("Interrupted thread");
                break;
            }
            logger.info("Paying № {} of {} gong", i, gong.name);
            sound.play(true);
            sound.join();
        }
    }
}
