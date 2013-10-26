/* This actor implements a Network Bus.

@Copyright (c) 2010-2013 The Regents of the University of California.
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

package ptolemy.actor;


/** This interface defines a listener for communication aspects.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public interface CommunicationAspectListener {

    /** The event that is sent by the communication aspect and processed
     *  by the listener.
     *  @param communicationAspect The communication aspect that sent the event.
     *  @param source The source actor that caused the event in the
     *      communication aspect.
     *  @param messageId The ID of the message that caused the event in
     *      the communication aspect.
     *  @param messageCnt The amount of messages currently being processed
     *      by the communication aspect.
     *  @param eventType The type of the event.
     *  @param time The timestamp of the event.
     */
    public void event(CommunicationAspect communicationAspect, Actor source,
            int messageId, int messageCnt, double time, EventType eventType);

    /** Type of the event. */
    public static enum EventType {
        /** Token was received. */
        RECEIVED,
        /** Token was sent. */
        SENT
    }

}
