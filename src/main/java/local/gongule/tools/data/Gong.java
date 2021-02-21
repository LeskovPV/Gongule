package local.gongule.tools.data;

import local.gongule.Gongule;
import local.gongule.tools.process.Sound;
import local.gongule.utils.logging.Loggible;

import java.io.Serializable;

public class Gong implements Serializable, Loggible {

    public static final Sound sound = new Sound(Gongule.getGongFile().getPath());
    public String name = "";
    public int amount = 1;

    public Gong() {
    }

    public Gong(String name) {
        this.name = name;
    }

    public Gong(String name, int amount) {
        this.name = name;
        this.amount = amount;
    }

    public void play() {
        Sound.playGong(this);
//        if (sound.isPlaying()) {
//            logger.warn("Прерываем играет");
//        }
//        new Thread(() -> {
//            for (int i = 0; i < amount; i++) {
//                logger.info("Paying № {}", i);
//                sound.play(true);
//                sound.join();
//                //sound.playSound(Gongule.getGongFile().getPath()).join();
//                //Thread.sleep(1000);
//            }
//        }).start();
    }

}
