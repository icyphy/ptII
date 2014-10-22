/* An actor that delays the input for a certain amount of real time.

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
package ptolemy.domains.ci.lib;

import java.util.LinkedList;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ci.kernel.CIActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Queue

/**
 an push-pull FIFO queue.

 @author Yang Zhao, based on Queue by Jie Liu, Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.2
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class BackDropQueue extends CIActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public BackDropQueue(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input.setMultiport(false);
        output.setMultiport(false);
        length = new TypedIOPort(this, "length", false, true);
        length.setTypeEquals(BaseType.INT);
        dropped = new TypedIOPort(this, "dropped", false, true);
        dropped.setTypeEquals(input.getType());
        capacity = new Parameter(this, "capacity");
        capacity.setTypeEquals(BaseType.INT);
        capacity.setExpression("1");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port on which the queue size is broadcast.
     *  The default type is integer.
     */
    public TypedIOPort length;

    /** The output port on which dropped tokens are broadcast.
     *  A token is dropped if the queue size is smaller than the capacity.
     *  The default type is the same as the type of the input port.
     */
    public TypedIOPort dropped;

    /** The capacity of the queue.  The initial default is an integer
     *  of value 1.
     */
    public Parameter capacity;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  @exception IllegalActionException Not thrown in this base class */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        int c = ((IntToken) capacity.getToken()).intValue();

        if (input.hasToken(0)) {
            if (_queue.size() < c) {
                _queue.add(input.get(0));
            } else {
                Token token = input.get(0);
                dropped.broadcast(token);
            }
        } else {
            output.broadcast((Token) _queue.removeFirst());
        }

        IntToken t = new IntToken(_queue.size());
        length.broadcast(t);
    }

    /** Reset the flag that fire() checks so that fire() only sleeps once.
     *  @exception IllegalActionException If the parent class throws it.
     *  @return Whatever the superclass returns (probably true).
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        super.prefire();

        // In case this actor is removed from
        // asyncPulledActors list to _pulledActors.
        if (input.hasToken(0)) {
            if (isPulled()) {
                enableActor();
            }
        }

        return input.hasToken(0) || _queue.size() > 0;
    }

    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _queue = new LinkedList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private LinkedList _queue = null;

    // private IntToken t = null;
}
