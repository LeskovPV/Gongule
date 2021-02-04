package local.gongule.tools.devices;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import local.gongule.tools.SystemInformation;

/**
 * <b>Cooling device</b>, singleton class<br>
 * Integrated device for CPU cooling<br>
 * The device consists of a fan connected via a NPN-transistor to a standard GPIO. <br>
 * The only class instance is created at the time of loading the parameters (see cfg-file): <br>
 * <em>pi.fanpin</em> - GPIO pin number of a cooling fan NPN-transistor. Cooling is disable, if this number < 0 <br>
 * <em>pi.temperature</em> - temperature value when the cooling fan turns on. Used only when cooling is on (pi.fanpin > -1) <br>
 **/
public class CoolingDevice extends RelayDevice {

    private GpioPinDigitalOutput gpioPin = null;

    private double temperature;

    public CoolingDevice(Pin pin, double temperature) {
        super(pin, false);
        this.temperature = temperature;
    }

    private Thread coolingThread = new Thread(() -> {
        while (true) {
            try {
                Thread.sleep(10000);
                double t = SystemInformation.getCPUTemperature(temperature);
                System.out.println("Temperature = " + t );
                temperature = t;
                set(t > temperature);
                if(!SystemInformation.isRaspbian) continue;
            } catch (Exception exception) {
                exception.printStackTrace();
                break;
            }
        }
    }, "Cooling thread");


    public static boolean used() {
        return SystemInformation.isRaspbian;
    }

}