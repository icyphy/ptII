/* A CSP actor that creates an output only after timed deadlock
has been reached by all other CSP actors in the containing
composite actor.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.csp.demo.BusContention;

// Ptolemy imports.
import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.actor.process.*;
import ptolemy.domains.csp.lib.*;
import ptolemy.domains.csp.kernel.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;

// Java imports.
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// ContentionAlarm
/**
A ContentionAlarm is a CSP actor that creates an output only after
timed deadlock has been reached by all other CSP actors in the
containing composite actor. A ContentionAlarm has one input and
one output port as well as three "informal" states. During the
fire() method, the actor is enabled to cycle through each of the
three states.

In state one, the ContentionAlarm attempts to receive a token through
its input port. Once a token has been received, the actor enters state
two and calls the _waitForDeadlock() method that it inherits from
CSPActor. This method means that the actor becomes time delayed and
will not continue until all other actors in the composite actor are
either blocked or time delayed. Once _waitForDeadlock() returns, the
actor enters state three and sends a token through its output port.

A useful application of ContentionAlarm is to notify other actors
if there are multiple contenders for a given resource at a given
time. Because of the semantics of _waitForDeadlock(), ContentionAlarm
will not "wake up" until all contenders have attempted to access a
particular resource.

In addition to the time delay oriented semantics of ContentionAlarm,
it can also notify an ExecEventListener as this actor jumps between
its three states. Such notification is enabled by adding an
ExecEventListener to this actor's listener list via the addListeners()
method. Listeners can be removed via the removeListeners() method.
ExecEventListeners are currently implemented to serve as conduits
between Ptolemy II and the Diva graphical user interface.

@author John S. Davis II
@version $Id$
@see ptolemy.actor.gui.ExecEvent
@see ptolemy.actor.gui.ExecEventListener
*/

public class ContentionAlarm extends CSPActor {

    /** Construct a ContentionAlarm actor with the specified container
     *  and name. Set the type of the input and ouput ports to
     *  BaseType.GENERAL.
     * @param cont The container of this actor.
     * @param name The name of this actor.
     * @exception IllegalActionException If the actor cannot be
     *  contained by the proposed container.
     * @exception NameDuplicationException If the container
     *  already has an actor with this name.
     */
    public ContentionAlarm(TypedCompositeActor cont, String name)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);

        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);

        input.setTypeEquals(BaseType.GENERAL);
        output.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.  The type of this port is BaseType.GENERAL.
     */
    public TypedIOPort input;

    /** The output port.  The type of this port is BaseType.GENERAL.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute this actor by cycling through its three states
     *  continually. Each state can potentially block due to
     *  the blocking semantics of CSP communication. Upon entry
     *  into each state, generate an ExecEvent with the
     *  corresponding state value.
     * @exception IllegalActionException If communication through
     *  the input or output ports throws an IllegalActionException.
     */
    public void fire() throws IllegalActionException {

        while(true) {
            // State 1
            _debug( new ExecEvent( this, ExecEvent.WAITING ) );
            input.get(0);

            // State 2
            _debug( new ExecEvent( this, ExecEvent.WAITING ) );
            _waitForDeadlock();

            // State 3
            _debug( new ExecEvent( this, ExecEvent.ACCESSING ) );
            output.send(0, new Token());
        }
    }
}
