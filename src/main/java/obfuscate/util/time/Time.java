package obfuscate.util.time;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Time
{
    public static boolean elapsed(long t1, long delay)
    {
        return System.currentTimeMillis() > t1 + delay;
    }

    public static String tFormat(int ticks)
    {
        int seconds = (ticks / 20);
        return sFormat(seconds);
    }

    public static String sFormat(int seconds) {
        if(seconds < 0)
            return "-";

        int minutes = seconds / 60;
        seconds = seconds % 60;
        NumberFormat nf = new DecimalFormat("00");
        return nf.format(minutes) + ":" + nf.format(seconds);
    }

    public static String sFormatVerbose(int seconds) {
        if(seconds < 0)
            return "-";

        if (seconds > 60) {
            var minutes = seconds / 60f;
            return String.format("%.1f", minutes) + " Minutes";
        }

        return seconds + " Seconds";
    }

    public static Long now() {
        return System.currentTimeMillis();
    }
}
