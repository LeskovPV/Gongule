package local.gongule.tools.relays;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import local.gongule.tools.ConfigFile;
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

    static private CoolingRelay instance = new CoolingRelay(RaspiPin.GPIO_04, 50, 65);

    static public CoolingRelay getInstance() {
        return instance;
    }

    private int measureDelay = 30;

    private Double maxTemperature = null;

    public double getMinTemperature() {
        return minTemperature;
    }

    private Double minTemperature = null;

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public void setTemperatures(Double min, Double max) {
        Double oldMin = minTemperature;
        Double oldMax = maxTemperature;
        if (min != null)
            minTemperature = min;
        if (max != null)
            maxTemperature = max;
        if (minTemperature > maxTemperature) {
            double temperature = maxTemperature;
            maxTemperature = minTemperature;
            minTemperature = temperature;
        }
        if (!minTemperature.equals(oldMin)) {
            ConfigFile.getInstance().set("minTemperature", String.valueOf(minTemperature));
            if (oldMin != null)
                logger.info("Fan relay turn-off temperature is changed from {} to {}", oldMin, minTemperature);
        }
        if (!maxTemperature.equals(oldMax)) {
            ConfigFile.getInstance().set("maxTemperature", String.valueOf(maxTemperature));
            if (oldMin != null)
                logger.info("Fan relay turn-on temperature is changed from {} to {}", oldMax, maxTemperature);
        }
    }

    private Thread coolingThread = new Thread(() -> {
        while (true) {
            try {
                Thread.sleep(1000 * measureDelay);
            } catch (InterruptedException exception) {
                break;
            }
            double temperature = SystemUtils.getCPUTemperature((maxTemperature + minTemperature)/2);
            if ((temperature > maxTemperature) && (value)){
                set(false);
                logger.trace("Cooling relay turn-on. T = {}", temperature);
            }
            if ((temperature < minTemperature) && (!value)) {
                set(true);
                logger.trace("Cooling relay turn-off. T = {}", temperature);
            }
            //logger.trace("Temperature = {}", relayTemperature);
        }
    }, "Cooling thread");

    /**
     * Constructor
     */
    private CoolingRelay(Pin pin, double minTemperature, double maxTemperature) {
        super("Cooling relay", pin, true);
        minTemperature = Double.parseDouble(ConfigFile.getInstance().get("minTemperature", String.valueOf(minTemperature)));
        maxTemperature = Double.parseDouble(ConfigFile.getInstance().get("maxTemperature", String.valueOf(maxTemperature)));
        setTemperatures(minTemperature, maxTemperature);
        if (SystemUtils.isRaspbian)
            coolingThread.start();
    }

}