package Atom.Beryllium;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static Atom.Manifest.Dates;
import static Atom.Manifest.Times;

public class TimeKeeper extends Beryllium {

    public long getMilis() {
        return System.currentTimeMillis();
    }

    public String getDates(boolean days) {
        // Initializing SimpleDateFormat
        SimpleDateFormat SDFormat = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.UK);
        // Displaying the formats
        Date date = new Date();
        String dates = SDFormat.format(date);
        String time = simpleDateFormat.format(date);
        Dates = dates;
        Times = time;
        if (days) return dates;
        return time;
    }

}
