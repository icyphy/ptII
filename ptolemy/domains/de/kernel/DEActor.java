/* A base class for DE domain actors.

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
@ProposedRating Green (liuj@eecs.berkeley.edu)
@AcceptedRating Green (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.de.kernel;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.actor.lib.TimedActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;


//////////////////////////////////////////////////////////////////////////
//// DEActor

/**
A base class for actors specific to the DE domain.  This class
implements both SequenceActor and TimedActor.
This class returns a new DEIOPort for the newPort() method.
Although most DE specific actors extend this class,
other (domain polymorphic) actors can also be used in the
DE domain.

@author Jie Liu
@version $Id$
@since Ptolemy II 0.2
@see ptolemy.actor.Actor
*/
public abstract class DEActor extends TypedAtomicActor
    implements SequenceActor, TimedActor {

    /** Construct an actor with the specified container and name.
     *  This is protected because there is no reason to create an instance
     *  of this class, but derived classes will want to invoke the
     *  constructor of the superclass.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    protected DEActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new DEIOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *  Normally this method is not called directly by actor code.
     *  Instead, a change request should be queued with the director.
     *
     *  @param name The name for the new port.
     *  @return The new DEIOPort.
     *  @exception NameDuplicationException If this actor already has a port
     *   with the specified name.
     */
    public Port newPort(String name) throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();
            DEIOPort port = new DEIOPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "AtomicActor.newPort: Internal error: " + ex.getMessage());
        } finally {
            _workspace.doneWriting();
        }
    }

}
