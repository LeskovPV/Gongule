package local.gongule.utils.formatter;

import java.time.format.DateTimeFormatter;

public class TimeFormatter{

    static public String pattern = "HH:mm";

    static public String fullPattern = "HH:mm:ss";

    static public DateTimeFormatter get() {
        return get(false);
    }

    static public DateTimeFormatter get(boolean fullPattern) {
        return DateTimeFormatter.ofPattern(fullPattern ? TimeFormatter.fullPattern : pattern);
    }

    static public int getSize() {
        return getSize(false);
    }

    static public int getSize(boolean fullPattern) {
        return fullPattern ? TimeFormatter.fullPattern.length() : pattern.length();
    }

}
