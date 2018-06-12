/* This class extends a TimedEvent to integrate HLA specific information.

@Copyright (c) 2013-2018 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package org.hlacerti.lib;

import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;

///////////////////////////////////////////////////////////////////
//// HlaTimedEvent

/**
 * <p>This class extends the TimedEvent class to add specific information
 * relative to HLA "events" received from HLA/CERTI.
 * The <i>objectInstanceID</i> parameter give information on who
 * is responsible of an UAV (Update Attribute Value). An UAV is an update
 * value of a HLA attribute which belongs to a HLA class instance.
 *
 * See the {@link HlaSubscriber} for its usage and more information.
 *
 *  @author Gilles Lasnier based on OriginatedEvent.java by David Come
 *  @version $Id: HlaTimedEvent.java 151 2018-03-06 14:43:59Z gl.lasnier $
 *  @since Ptolemy II 11.0
 *
 *  @Pt.ProposedRating Yellow (glasnier)
 *  @Pt.AcceptedRating Red (glasnier)
 */
public class HlaTimedEvent extends TimedEvent {

    /** Constructs a HLA event with the specified time stamp, contents
     *  and HLA object instance ID.
     *  @param time The time stamp.
     *  @param obj The contents.
     *  @param hlaObjectInstanceId The HLA object instance ID.
     */
    public HlaTimedEvent(Time time, Object obj, int hlaObjectInstanceId) {
        super(time, obj);
        _hlaObjectInstanceId = hlaObjectInstanceId;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the HLA object instance ID value.
     *  @return The HLA object instance ID as int.
     */
    public int getHlaObjectInstanceId() {
        return _hlaObjectInstanceId;
    }

    /** Check if two HlaTimedEvent objects are equals.
     *  @param hlaTimedEvent A HlaTimedEvent object.
     *  @return true if the two objects are equals, false otherwise.
     */
    @Override
    public boolean equals(Object hlaTimedEvent) {
        if (!super.equals(hlaTimedEvent)) {
            return false;
        } else {
            // At this point 'this' and hlaTimedvent are known the same class
            // thanks to the check done from the super class (super.equals()).
            HlaTimedEvent event = (HlaTimedEvent) hlaTimedEvent;

            if (_hlaObjectInstanceId == event.getHlaObjectInstanceId()) {
                return true;
            } else {
                return false;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The HLA object instance ID. */
    private int _hlaObjectInstanceId;
}
