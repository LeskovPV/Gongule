package local.gongule.tools.devices;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import local.gongule.utils.system.SystemUtils;

/**
 * <b>Cooling device</b>, singleton class<br>
 * Integrated device for CPU cooling<br>
 * The device consists of a fan connected via a NPN-transistor to a standard GPIO. <br>
 * The only class instance is created at the time of loading the parameters (see cfg-file): <br>
 * <em>pi.fanpin</em> - GPIO pin number of a cooling fan NPN-transistor. Cooling is disable, if this number < 0 <br>
 * <em>pi.temperature</em> - temperature value when the cooling fan turns on. Used only when cooling is on (pi.fanpin > -1) <br>
 **/
public class CoolingRelay extends Relay {

    static private CoolingRelay instance = new CoolingRelay(RaspiPin.GPIO_04, 50);

    static public CoolingRelay getInstance() {
        return instance;
    }

    private int measureDelay = 30;

    private double relayTemperature;

    public double getRelayTemperature() {
        return relayTemperature;
    }

    public void setRelayTemperature(Double relayTemperature) {
        if (relayTemperature != null)
            this.relayTemperature = relayTemperature;
    }

    private Thread coolingThread = new Thread(() -> {
        while (true) {
            try {
                Thread.sleep(1000 * measureDelay);
            } catch (InterruptedException exception) {
                break;
            }
            double temperature = SystemUtils.getCPUTemperature(relayTemperature);
            logger.trace("Temperature = {}", temperature );
            set(temperature > relayTemperature);
            //logger.trace("Temperature = {}", relayTemperature);
        }
    }, "Cooling thread");

    private CoolingRelay(Pin pin, double relayTemperature) {
        super("Cooling relay", pin, false);
        this.relayTemperature = relayTemperature;
        if (SystemUtils.isRaspbian)
            coolingThread.start();
    }

}