package local.gongule.tools.relays;

import com.pi4j.io.gpio.*;
import local.gongule.utils.logging.Loggible;
import local.gongule.utils.system.SystemUtils;

/**
 * <b>Переключатель</b> позволяет работать с реле, светодиодами и т.п.<br>
 */
abstract public class Relay implements Loggible {

    static protected final GpioController gpio = SystemUtils.isRaspbian ? GpioFactory.getInstance() : null;

    protected GpioPinDigitalOutput gpioPin;

    protected boolean value;

    public Relay(String name, Pin pin, Boolean value) {
        if (SystemUtils.isRaspbian) {
            gpioPin = gpio.provisionDigitalOutputPin(pin, name, PinState.HIGH);
            gpioPin.setShutdownOptions(true, PinState.LOW);
            this.value = !value;
            set(value);
        }
        this.value = value;
    }

    public boolean set(boolean value) {
        if (this.value == value) return value;
        if (!SystemUtils.isRaspbian) return this.value;
        //logger.trace("{} = {}", gpioPin.getName(), value);
        if (value)
            gpioPin.high();
        else
            gpioPin.low();
        this.value = value;
        return value;
    }

    public Boolean get() {
        return value;
    }

}