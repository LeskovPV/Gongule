package local.gongule.utils.formatter;

import java.time.format.DateTimeFormatter;

public class TimeFormatter{

    static public String pattern = "HH:mm";

    static public DateTimeFormatter get() {
        return DateTimeFormatter.ofPattern(pattern);
    }

    static public int getSize() {
        return pattern.length();
    }

}
