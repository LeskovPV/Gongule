package local.gongule.tools.relays;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

public class PowerRelay extends Relay {

    static private PowerRelay instance = new PowerRelay(RaspiPin.GPIO_05);

    static public PowerRelay getInstance() {
        return instance;
    }

    private PowerRelay(Pin pin) {
        super("Power relay", pin, false);
    }

}
