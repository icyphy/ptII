/* An IOPort for modal models.

 Copyright (c) 1997-2001 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (liuxj@eecs.berkeley.edu)
*/

package ptolemy.vergil.ptolemy.fsm.modal;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.*;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// ModalPort
/**
A port for modal models.  This port mirrors certain changes to it in the
ports of the controller and refinements of the modal model.

@author Edward A. Lee
@version $Id$
*/

public class ModalPort extends TypedIOPort {

    /** Construct a port with a containing actor and a name
     *  that is neither an input nor an output.  The specified container
     *  must implement the TypedActor interface, or an exception will be
     *  thrown.
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   TypedActor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public ModalPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is true, make the port an input port.
     *  If the argument is false, make the port not an input port.
     *  If this is never called, and setOutput() is never called,
     *  and the port is a transparent port of a composite actor,
     *  then the input/output status will be inferred from the connection.
     *  This method invalidates the schedule and resolved types of the
     *  director of the container, if there is one.
     *  It is write-synchronized on the workspace, and increments
     *  the version of the workspace.
     *
     *  @param isInput True to make the port an input.
     */
    public void setInput(boolean isInput) {
        super.setInput(isInput);
        // Mirror the change in mirror ports.
        ModalModel container = (ModalModel)getContainer();
        Iterator entities = container.entityList().iterator();
        while (entities.hasNext()) {
            Entity entity = (Entity)entities.next();
            Port mirrorPort = entity.getPort(getName());
            if (mirrorPort instanceof IOPort) {
                ((IOPort)mirrorPort).setInput(isInput);
            }
        }
    }

    // FIXME: setOutput, setMultiport.

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to ensure that the proposed container
     *  implements the TypedActor interface (the base class ensures that
     *  the container implements the Actor interface) or null.
     *
     *  @param container The proposed container.
     *  @exception IllegalActionException If the proposed container is not a
     *   TypedActor, or if the base class throws it.
     */
    protected void _checkContainer(Entity container)
            throws IllegalActionException {
        if (!(container instanceof ModalModel) && (container != null)) {
            throw new IllegalActionException(container, this,
                    "ModalPort can only be contained by ModalModel objects.");
        }
    }
}
