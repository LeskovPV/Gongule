package local.gongule.tools.devices;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import local.gongule.utils.SystemInformation;

/**
 * <b>Переключатель</b> позволяет работать с реле, светодиодами и т.п.<br>
 */
public class RelayDevice{

    private GpioPinDigitalOutput gpioPin;

    private boolean value;

    public RelayDevice(Pin pin, Boolean value) {
        this.value = value;
    }

    public boolean set(boolean value) {
        Boolean acceptedValue = (this.value == value);
        if (!SystemInformation.isRaspbian) return acceptedValue;
        if (value) {
            gpioPin.high();
        } else {
            gpioPin.low();
        }
        return value;
    }

    public Boolean get() {
        return value;
    }

}