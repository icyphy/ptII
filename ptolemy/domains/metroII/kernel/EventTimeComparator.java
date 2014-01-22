/* EventTimeComparator compares time tags of MetroII events.

 Copyright (c) 2012-2013 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.domains.metroII.kernel;

import java.util.Comparator;

import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Time;

///////////////////////////////////////////////////////////////////
//// EventTimeComparator

/**
 * EventTimeComparator compares time tags of MetroII events.
 * 
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 * 
 */
public class EventTimeComparator {

    /**
     * EventTimeComparator is a singleton.
     */
    protected EventTimeComparator() {
        // Exists only to defeat instantiation.
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
     * Compare two timetags.
     * 
     * @param time1
     *            the first timetag to be compared
     * @param time2
     *            the second timetag to be compared
     * @return a negative integer, zero, or a positive integer as the first
     *         timetag is less than, equal to, or greater than the second.
     */
    static public int compare(Time time1, Time time2) {
        if (comparator == null) {
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

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /**
     * Comparator for time tags of MetroII events.
     */
    private static Comparator<Event.Time> comparator = null;
}
