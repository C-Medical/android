package iz.supereasycamera;

import android.util.Log;

import org.joda.time.DateTime;

/**
 * Created by izumi_j on 2014/12/01.
 */
public final class Utils {
    private Utils(){}

    public static void debug(String log) {
        Log.d("DEBUG", log);
    }

    public static void error(String log) {
        Log.e("ERROR", log);
    }

    public static void error(String log, Throwable t) {
        Log.e("ERROR", log, t);
    }

    public static String formatDateTime(DateTime dateTime) {
        return dateTime.toString("yyyy-MM-dd HH:mm:ss");
    }
}
