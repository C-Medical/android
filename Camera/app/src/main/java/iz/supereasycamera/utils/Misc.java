package iz.supereasycamera.utils;

import android.util.Log;

import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * Created by izumi_j on 2014/12/01.
 */
public final class Misc {
    private Misc(){}

    private static final String LOG_TAG = "APP";

    public static void debug(String log) {
        Log.d(LOG_TAG, log);
    }

    public static void warn(String log) {
        Log.w(LOG_TAG, log);
    }

    public static void error(String log) {
        Log.e(LOG_TAG, log);
    }

    public static void error(String log, Throwable t) {
        Log.e(LOG_TAG, log, t);
    }

    public static String formatDateTime(DateTime dateTime) {
        return dateTime.toString("yyyy-MM-dd HH:mm:ss");
    }

    public static String toKB(int byteSize) {
        final BigDecimal kb = new BigDecimal(byteSize).divide(new BigDecimal(1000), 2, BigDecimal.ROUND_HALF_UP);
        return kb.toString() + "KB";
    }
}
