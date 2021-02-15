package local.gongule.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * System log to console
 */

public class Log {

    private static final String dateFormat = "HH:mm:ss dd.MM.yyyy";

    private static String dateToString(Date date){
        try {
            SimpleDateFormat simpleFormat = new SimpleDateFormat();
            simpleFormat.applyPattern(dateFormat);
            return simpleFormat.format(date);
        } catch (Exception exception) {
            return date.toString();
        }
    }

    private static void print(String message){
        System.out.println(dateToString(new Date()) + " " + message);
    }

    public static void printInfo(String message) {
        print(message);
    }

    public static void printWarn(String message) {
        print("[WARN] " + message);
    }

    public static void printError(String message, Exception exception) {
        if ((message == null) && (exception == null))
            message = "";
        print("[ERROR] " + (
                (message == null) ?
                        exception.getMessage() :
                        message + (
                                (exception == null) ?
                                        "" :
                                        ": " + exception.getMessage()
                                        //"\n" + exception.getStackTrace()
                        )));
    }

    public static void printError(String message) {
        printError(message, null);
    }

    public static void printError(Exception exception) {
        printError(null, exception);
    }

}
