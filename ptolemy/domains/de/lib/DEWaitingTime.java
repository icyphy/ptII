/* Measure the time that events at one input have to wait for events at another.

 Copyright (c) 1998 The Regents of the University of California.
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
events at another.  It outputs that time at the time of the arrival of
the waited for event.

@author Edward A. Lee
@version $Id$
*/
public class DEWaitingTime extends DEActor {

    /** Constructor.
     *  @param container The composite actor that this actor belongs too.
     *  @param name The name of this actor.
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
        waiter = new DEIOPort(this, "waiter", true, false);
        waitee = new DEIOPort(this, "waitee", true, false);
        // if a waiter and a waitee arrive at the same time, we don't want
        // the waiter to have to wait.
        waiter.before(waitee);
        waitee.triggers(output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If one or more events have arrived at waitee, then output the
     *  waiting time for each prior event arrival at waiter since the
     *  last arrival of waitee.
     *  @exception IllegalActionException If get or broadcast throws it.
     */
    public void fire() throws IllegalActionException {

        boolean godot = false;
        for (int i=0; i<waitee.getWidth(); i++) {
            if (waitee.hasToken(i)) {
                waitee.get(i);
                godot = true;
            }
        }
        // FIXME: Should be DEDirector, not DECQDirector
        double currentTime = ((DECQDirector)getDirector()).getCurrentTime();
        for (int i=0; i<waitee.getWidth(); i++) {
            if (waiter.hasToken(i)) {
                waiter.get(i);
                _waiting.addElement(new Double(currentTime));
            }
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

    /** Do nothing.  Derived classes override this method to define their
     *  initialization code, which gets executed exactly once prior to
     *  any other action methods. This method typically initializes
     *  internal members of an actor and produces initial output data.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
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
