/* Measure the time that events at one input have to wait for events at another.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

import java.util.Vector;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEActor;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// WaitingTime

/**
 This actor measures the time that events at one input have to wait for
 events at another.  Specifically, there will be one output event for
 each <i>waiter</i> input event.  But the output event is delayed until the
 next arrival of an event at <i>waitee</i>.  When one or more events arrive
 at <i>waitee</i>, then all events that have arrived at <i>waiter</i> since
 the last <i>waitee</i> (or since the start of the execution) trigger an
 output.  The value of each output is the time that the <i>waiter</i> event
 waited for <i>waitee</i>.  The inputs have undeclared type, so anything
 is acceptable.  The output is always a DoubleToken.

 @author Lukito Muliadi, Edward A Lee
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class WaitingTime extends DEActor {
    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public WaitingTime(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // create the ports
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);
        waiter = new TypedIOPort(this, "waiter", true, false);
        waitee = new TypedIOPort(this, "waitee", true, false);
        _waiting = new Vector();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** The output, which is always a DoubleToken representing the
     *  that a waiter waited for an input at the <i>waitee</i> input.
     */
    public TypedIOPort output;

    /** An input event here waits for the next event at the <i>waitee</i>
     *  input.  The type of this port is undeclared, so any input is acceptable.
     */
    public TypedIOPort waiter;

    /** An input event here triggers an output event for each <i>waiter</i>
     *  input that arrived since the last input here.  The type of this
     *  port is undeclared, so any input is acceptable.
     */
    public TypedIOPort waitee;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the ports.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        WaitingTime newObject = (WaitingTime) super.clone(workspace);
        newObject._waiting = new Vector();
        return newObject;
    }

    /** If this firing is triggered by an event at <i>waitee</i>, then output
     *  the waiting time for each prior event arrival at <i>waiter</i>
     *  since the last arrival of waitee.  If there is no event at
     *  <i>waitee</i>, then record the time of arrival of the events
     *  at <i>waiter</i>, and produce no output.
     *  @exception IllegalActionException If get() or send() throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Time currentTime = ((DEDirector) getDirector()).getModelTime();

        while (waiter.hasNewToken(0)) {
            waiter.get(0);
            _waiting.addElement(currentTime);
        }

        boolean godot = false;

        while (waitee.hasNewToken(0)) {
            waitee.get(0);
            godot = true;
        }

        if (godot) {
            for (int i = 0; i < _waiting.size(); i++) {
                Time previousTime = (Time) _waiting.elementAt(i);
                DoubleToken outToken = new DoubleToken(currentTime.subtract(
                        previousTime).getDoubleValue());
                output.send(0, outToken);
            }

            _waiting.removeAllElements();
        }
    }

    /** Clear the list of waiters.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _waiting.removeAllElements();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Vector _waiting;
}
