/* A base class for actors that transform an input signal into an output signal.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (xxx@eecs.berkeley.edu)
*/

package ptolemy.domains.de.lib;

import ptolemy.domains.de.kernel.*;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.lib.TimedActor;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// DETransformer
/**
This is an abstract base class for DE actors that transform
an input stream into an output stream.  It provides an input
and an output port, and manages the cloning of these ports.
These ports are instances of DEPort, and the actor extends DEActor.
Thus, all the facilities of those classes are available.

@author Edward A. Lee
@version $Id$
*/

public abstract class DETransformer extends DEActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    protected DETransformer(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input = new DEIOPort(this, "input", true, false);
        output = new DEIOPort(this, "output", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.  This base class imposes no type constraints except
     *  that the type of the input cannot be greater than the type of the
     *  output.
     */
    public DEIOPort input;

    /** The output port. By default, the type of this output is constrained
     *  to be at least that of the input.
     */
    public DEIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        DETransformer newobj = (DETransformer)super.clone(ws);
        newobj.input = (DEIOPort)newobj.getPort("input");
        newobj.output = (DEIOPort)newobj.getPort("output");
        return newobj;
    }
}
