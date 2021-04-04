package local.gongule.utils.formatter;

import java.time.LocalTime;

public class TimeFunctions {

    static public boolean isBeforeMidnight(LocalTime time, int minutes) {
        return (time.toSecondOfDay() >= (24 * 60 - minutes) * 60);
    }

    static public boolean isAfterMidnight(LocalTime time, int minutes) {
        return (time.toSecondOfDay() <= minutes * 60);
    }

    static public boolean isNearMidnight(LocalTime time, int minutes) {
        return isBeforeMidnight(time, minutes) || isAfterMidnight(time, minutes);
    }

    static public boolean isNear(LocalTime time1, LocalTime time2, int minutes) {
        if (isNearMidnight(time1, minutes))
            return (isNearMidnight(time2, minutes));
        if (isNearMidnight(time2, minutes))
            return (isNearMidnight(time1, minutes));
        return  time1.isAfter(time2.minusMinutes(minutes)) &&
                time1.isBefore(time2.plusMinutes(minutes));
    }

}
