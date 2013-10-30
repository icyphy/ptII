package ptolemy.domains.metroII.kernel;

import java.util.Comparator;

import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Time;

public class EventTimeComparator {
    
    protected EventTimeComparator() {
        // Exists only to defeat instantiation.
    }

    /**
     * Convert a time value from one resolution to another resolution. Note that
     * this method may result in a loss of precision.
     * 
     * @param timeValue
     *            input time value in type 'long'
     * @param fromResolution
     *            the resolution associated with timeValue
     * @param toResolution
     *            the resolution it's converting to
     * @return the new time value.
     */
    static public long convert(long timeValue, double fromResolution,
            double toResolution) {
        if (timeValue == Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }

        double scaler = fromResolution / toResolution;

        assert scaler > 0;

        if (scaler > 1) {
            assert Math.abs(scaler - (int) scaler) < 0.00001;
            timeValue = timeValue * ((int) scaler);
        } else {
            double iScaler = 1 / scaler;
            assert Math.abs(iScaler - (int) iScaler) < 0.00001;
            timeValue = timeValue / ((int) iScaler);
        }

        return timeValue;
    }

    /**
     * Compare two event time.
     */
    static public int compare(Time time1, Time time2) {
        if (comparator== null) {
            comparator = new Comparator<Event.Time>() {
                public int compare(Time time1, Time time2) {
                    long greater = time1.getValue()
                            - convert(time2.getValue(), time2.getResolution(),
                                    time1.getResolution());
                    if (greater > 0) {
                        return 1;
                    } else if (greater == 0) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            };
        }
        
        return comparator.compare(time1, time2); 
    }
    
    private static Comparator<Event.Time> comparator = null; 
}
