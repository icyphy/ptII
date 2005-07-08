/* An actor that implements a queue of events.

 Copyright (c) 1998-2005 The Regents of the University of California.
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

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.FIFOQueue;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Queue

/**
 This actor implements an event queue.  When a token is received on the
 <i>input</i> port, it is stored in the queue.
 When the <i>trigger</i> port receives a token, the oldest element in the
 queue is output.  If there is no element in the queue when a
 token is received on the <i>trigger</i> port, then no output is
 produced.  The inputs can be of any token type, and the output
 is constrained to be of a type at least that of the input. There is no
 constraint on the size of queue.
 <p>

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (eal)
 */
public class Queue extends DETransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Queue(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        output.setTypeAtLeast(input);
        trigger = new TypedIOPort(this, "trigger", true, false);

        // Leave trigger type undeclared.
        // Put it at the bottom of the icon by default.
        StringAttribute cardinality = new StringAttribute(trigger, "_cardinal");
        cardinality.setExpression("SOUTH");

        _queue = new FIFOQueue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The trigger port, which has undeclared type. If this port
     *  receives a token, then the oldest token in the queue
     *  will be emitted on the <i>output</i> port.
     */
    public TypedIOPort trigger;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the ports.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Queue newObject = (Queue) super.clone(workspace);
        newObject._queue = new FIFOQueue();
        newObject.output.setTypeAtLeast(newObject.input);
        return newObject;
    }

    /** If there is a token in the <i>trigger</i> port,
     *  emit the most recent token from the <i>input</i> port. If there
     *  has been no input token, or there is no token on the <i>trigger</i>
     *  port, emit nothing.
     *  @exception IllegalActionException If getting tokens from input and
     *  trigger ports or sending token to output throws it.
     */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            _queue.put(input.get(0));
        }

        if (trigger.hasToken(0)) {
            // Consume the trigger token.
            trigger.get(0);

            if (_queue.size() > 0) {
                output.send(0, (Token) _queue.take());
            }
        }
    }

    /** Clear the cached input tokens.
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        _queue.clear();
        super.initialize();
    }

    /** If there is no input on the <i>trigger</i> port, return
     *  false, indicating that this actor does not want to fire.
     *  This has the effect of leaving input values in the input
     *  ports, if there are any.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        // If the trigger input is not connected, never fire.
        boolean hasInput = false;
        boolean hasTrigger = false;

        if (input.getWidth() > 0) {
            hasInput = (input.hasToken(0));
        }

        if (trigger.getWidth() > 0) {
            hasTrigger = (trigger.hasToken(0));
        }

        return hasInput || hasTrigger;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The FIFOQueue. */
    protected FIFOQueue _queue;
}
