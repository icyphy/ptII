/* MetroEventBuilder is a set of routines that create Metro events.

 Copyright (c) 2012-2014 The Regents of the University of California.
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

import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Status;

///////////////////////////////////////////////////////////////////
////MetroEventBuilder

/**
 * MetroEventBuilder is a set of routines that create Metro events.
 *
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public class MetroIIEventBuilder {

    /**
     * Constructs an event builder.
     */
    public MetroIIEventBuilder() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Creates a proposed Metro event.
     *
     * @param eventName
     *            The name of the event
     * @param timeValue
     *            The time value of the event
     * @param resolution
     *            The resolution associated with the time value
     * @return A Metro event with the given name and the time tag
     */
    static public Builder newProposedEvent(String eventName, long timeValue,
            double resolution) {
        Event.Builder builder = Event.newBuilder();
        builder.setName(eventName);
        builder.setStatus(Event.Status.PROPOSED);
        builder.setType(Event.Type.DEFAULT_NOTIFIED);
        Event.Time.Builder timeBuilder = Event.Time.newBuilder();
        if (timeValue < Long.MAX_VALUE) {
            double scaler = resolution / timeBuilder.getResolution();

            assert scaler > 0;
            assert Math.abs(scaler - (int) scaler) < 0.00001;

            timeValue = timeValue * ((int) scaler);
            timeBuilder.setValue(timeValue);
        } else {
            timeBuilder.setValue(Long.MAX_VALUE);
        }
        builder.setTime(timeBuilder);

        return builder;
    }

    /**
     * Creates a proposed Metro event.
     *
     * @param eventName
     *            The name of the event
     * @return A Metro event with the given name
     */
    static public Builder newProposedEvent(String eventName) {
        Event.Builder builder = Event.newBuilder();
        builder.setName(eventName);
        builder.setStatus(Event.Status.PROPOSED);
        builder.setType(Event.Type.DEFAULT_NOTIFIED);

        return builder;
    }

    /**
     * Trims the substring from the beginning to the first delimiter '.' from a
     * given string. Example: XXX.YYY.ZZZ -&gt; YYY.ZZZ
     *
     * @param name
     *            The input string
     * @return The trimmed string
     */
    static public String trimModelName(String name) {
        assert name.length() > 1;
        int pos = name.indexOf(".", 1);
        return name.substring(pos);
    }

    /**
     * Checks if at least one event is notified in the event vector.
     *
     * @param events
     *            event vector to be checked.
     * @return true if there is at least one event notified.
     */
    static public boolean atLeastOneNotified(Iterable<Event.Builder> events) {
        for (Builder event : events) {
            if (event.getStatus() == Status.NOTIFIED) {
                return true;
            }
        }
        return false;
    }

}
