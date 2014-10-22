/* A dummy ScheduleListener for testing.

 Copyright (c) 2002-2014 The Regents of the University of California.
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
package ptolemy.domains.tm.kernel.test;

import ptolemy.domains.tm.kernel.ScheduleListener;

///////////////////////////////////////////////////////////////////
//// TestTypeListener

/**
 This dummy schedule listener implements the ScheduleListener interface.
 Each time event() is called, a line is appended to an internal
 buffer.  When getMessage() is called, the internal buffer is returned
 and the internal buffer is reset.

 @author Christopher Hylands, based on TestScheduleListener by Yuhong Xiong
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class TestScheduleListener implements ScheduleListener {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return strings describing the events seen by this listener.
     *  If no events have been seen, then the empty string is returned.
     *  @return A String including the type change information.
     */
    public String getEvents() {
        String results = _events.toString();
        _events = new StringBuffer("");
        return results;
    }

    /** React to the given scheduling event.
     */
    @Override
    public void event(String actorName, double time, int scheduleEvent) {
        _events.append(actorName + "\t" + time + "\t" + scheduleEvent + "\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private StringBuffer _events = new StringBuffer("");
}
