/* Measure the time that events at one input have to wait for events at another.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.domains.de.lib;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// DESampler
/**
This actor measures the time that events at one input have to wait for
events at another.  Specifically, there will be one output event for
each <i>waiter</i> input event.  But the output event is delayed until the
next arrival of an event at <i>waitee</i>.  When one or more events arrive
at <i>waitee</i>, then all events that have arrived at <i>waiter</i> since
the last <i>waitee</i> (or since the start of the execution) trigger an
output.  The value of each output is the time that the <i>waiter</i> event
waited for <i>waitee</i>.  The inputs can be of any type.  The output
is always a DoubleToken.

@author Lukito Muliadi, Edward A Lee
@version $Id$
*/
public class DEWaitingTime extends DEActor {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEWaitingTime(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // create the ports
        output = new DEIOPort(this, "output", false, true);
        output.setDeclaredType(DoubleToken.class);
        waiter = new DEIOPort(this, "waiter", true, false);
        waiter.setDeclaredType(Token.class);
        waitee = new DEIOPort(this, "waitee", true, false);
        waitee.setDeclaredType(Token.class);
        // Ensure that waiters are seen before simultaneous waitees.
        waiter.before(waitee);
        waitee.triggers(output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If this firing is triggered by an event at waitee, then output
     *  the waiting time for each prior event arrival at waiter since the
     *  last arrival of waitee.  If there is no event at waitee, then record
     *  the time of arrival of the events at waiter, and produce no output.
     *  @exception IllegalActionException If get() or broadcast() throws it.
     */
    public void fire() throws IllegalActionException {

        double currentTime = ((DEDirector)getDirector()).getCurrentTime();
        while(waiter.hasToken(0)) {
            waiter.get(0);
            _waiting.addElement(new Double(currentTime));
        }
        boolean godot = false;
        while(waitee.hasToken(0)) {
            waitee.get(0);
            godot = true;
        }
        if(godot) {
            for (int i=0; i<_waiting.size(); i++) {
                double previousTime =
                        ((Double)_waiting.elementAt(i)).doubleValue();
                DoubleToken outToken =
                        new DoubleToken(currentTime-previousTime);
                output.broadcast(outToken);
            }
            _waiting.removeAllElements();
        }
    }

    /** Clear the list of waiters.
     */
    public void initialize() {
        try{
            super.initialize();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex.getMessage());
        }
        _waiting.removeAllElements();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // the ports.
    public DEIOPort output;
    public DEIOPort waiter;
    public DEIOPort waitee;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Vector _waiting = new Vector();
}
