/* Base class for simple source actors.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (bilung@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Source
/**
Base class for simple data sources.  This class provides an output port
and a trigger input port, both exposed as public variables.  The trigger
port is a multiport with undeclared type, meaning that you can supply it
with any data type.  The trigger port can also be left unconnected.
The purpose of the trigger input is to (optionally) supply events that
cause the actor to fire.  For some domains, such as SDF and CT, this is
entirely unnecessary, as the actor will fire whether inputs are supplied
or not.  In such domains, the trigger input will normally be left unconnected.
In this base class, the fire() method reads
at most one token from each channel of the trigger input, if any,
and then discards the token.
<p>
Some derived classes may attach additional significance to an input
on the trigger port. For example, they might fix the type and attach
some significance to the value.  Note that it is not recommend to
use getWidth() on the port to determine whether the port is connected,
since the width may be greater than zero even if there
is no actual source of data.  This can occur, for example, if a trigger port
is connected to the inside of a port of an opaque composite actor, and
there is nothing connected to the outside of that port. It is not
recommended to make the behavior of an actor dependent on a global
property such as whether there is ultimately a source of data.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 0.3
*/

public abstract class Source extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Source(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        output = new TypedIOPort(this, "output", false, true);
        trigger = new TypedIOPort(this, "trigger", true, false);
        // NOTE: It used to be that trigger was set to GENERAL, but this
        // isn't really what we want.  What we want is an undeclared type
        // that can resolve to anything.  EAL 12/31/02
        // trigger.setTypeEquals(BaseType.GENERAL);
        trigger.setMultiport(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port.  The type of this port is unspecified.
     *  Derived classes may set it.
     */
    public TypedIOPort output = null;

    /** The trigger port.  The type of this port is undeclared, meaning
     *  that it will resolve to any data type.
     */
    public TypedIOPort trigger = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read at most one input token from each channel of the trigger
     *  input and discard it.  If the trigger input is not connected
     *  or has no actual sources (it might be connected to other
     *  inputs, for example, or to an unconnected input port at
     *  a higher level in the hierarchy) then this method does
     *  nothing.  Derived classes should be
     *  sure to call super.fire(), or to consume the trigger input
     *  tokens themselves, so that they aren't left unconsumed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        // NOTE: It might seem that using trigger.numberOfSources() is
        // correct here, but it is not. It is possible for channels
        // to be connected, for example, to other output ports or
        // even back to this same trigger port, in which case higher
        // numbered channels will not have their inputs read.
        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                trigger.get(i);
            }
        }
    }
}
