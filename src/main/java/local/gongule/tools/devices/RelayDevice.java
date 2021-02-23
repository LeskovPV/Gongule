package local.gongule.tools.devices;

import com.pi4j.io.gpio.*;
import local.gongule.utils.system.SystemUtils;

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
        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // provision gpio pin #01 as an output pin and turn on
        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MyLED", PinState.HIGH);

        // set shutdown state for this pin
        pin.setShutdownOptions(true, PinState.LOW);
        // turn off gpio pin #01
        pin.low();
        // toggle the current state of gpio pin #01 (should turn on)
        pin.toggle();
        // toggle the current state of gpio pin #01  (should turn off)
        pin.toggle();
        pin.pulse(1000, true); // set second argument to 'true' use a blocking call
        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        gpio.shutdown();

        Boolean acceptedValue = (this.value == value);
        if (!SystemUtils.isRaspbian) return acceptedValue;
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