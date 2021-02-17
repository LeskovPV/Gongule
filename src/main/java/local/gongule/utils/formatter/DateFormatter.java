package local.gongule.utils.formatter;

import java.time.format.DateTimeFormatter;

public class DateFormatter{

    static public final String pattern = "dd.MM.yyyy";

    static public DateTimeFormatter get() {
        return DateTimeFormatter.ofPattern(pattern);
    }

    static public int getSize() {
        return pattern.length();
    }

}
