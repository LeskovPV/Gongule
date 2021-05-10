package local.gongule.tools.relays;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import local.gongule.utils.system.SystemUtils;

public class PowerRelay extends Relay {


    static private PowerRelay instance = new PowerRelay(RaspiPin.GPIO_05);

    static public PowerRelay getInstance() {
        return instance;
    }

    private PowerRelay(Pin pin) {
        super("Power relay", pin, false);
    }

    @Override
    public boolean set(boolean value) {
        if (!SystemUtils.isRaspbian) return this.value;
        logger.info("Audio-amplifier power turn-{}", value ? "on": "off");
        return super.set(value);
    }

}
