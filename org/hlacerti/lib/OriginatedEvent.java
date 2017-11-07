/*
@Copyright (c) 2015-2016 The Regents of the University of California.
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

/**
 * Extension to add to an TimedEvent in oder to give to the HLASubscriber the
 * object's id which is reponsible for the update.
 * @author David Come
 * @version
 */
public class OriginatedEvent extends TimedEvent{


        /**
         * Constructs an OriginatedEvent object.
         *
         * @param time      a Time object
         * @param obj       an Object ...
         * @param federate  an int specifying ...
         */
        public OriginatedEvent(Time time, Object obj,int federate) {
                super(time, obj);
                objectID = federate;
        }

        /** The object id which is responsible for the UAV. */
        public int objectID;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + objectID;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            OriginatedEvent other = (OriginatedEvent) obj;
            if (objectID != other.objectID)
                return false;
            return true;
        }
}
