/* Composite actor with multiple refinements.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.hoc;

import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MultiCompositeActor

/**
 A composite actor that can have several refinements.

 @see Refinement
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (reviewmoderator)
 */
public class MultiCompositeActor extends TypedCompositeActor {
    /** Construct a composite actor in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.
     *  @param workspace The workspace that will list the actor.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public MultiCompositeActor(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a composite actor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public MultiCompositeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Mirror a set of container ports in the refinement.
     *  @param refinement The refinement in which to create ports.
     *  @param portsToMirror The ports to mirror in the refinement.
     *  @exception IllegalActionException If the port cannot be set
     *  to a multiport or to an output.
     *  @exception NameDuplicationException If a port cannot be added
     *  to the the refinement.
     */
    public static void mirrorContainerPortsInRefinement(Refinement refinement,
            Set<Port> portsToMirror) throws IllegalActionException,
            NameDuplicationException {

        for (Port port : portsToMirror) {
            try {
                refinement.setMirrorDisable(true);
                Port newPort = refinement.newPort(port.getName());
                if (newPort instanceof RefinementPort && port instanceof IOPort) {
                    try {
                        ((RefinementPort) newPort).setMirrorDisable(true);

                        if (((IOPort) port).isInput()) {
                            ((RefinementPort) newPort).setInput(true);
                        }

                        if (((IOPort) port).isOutput()) {
                            ((RefinementPort) newPort).setOutput(true);
                        }

                        if (((IOPort) port).isMultiport()) {
                            ((RefinementPort) newPort).setMultiport(true);
                        }
                    } finally {
                        ((RefinementPort) newPort).setMirrorDisable(false);
                    }
                }
            } finally {
                refinement.setMirrorDisable(false);
            }
        }
    }

    /** Create a new port with the specified name in this entity
     *  and all the refinements.  Link these ports so that
     *  if the new port is set to be an input, output, or multiport, then
     *  the change is mirrored in the other ports.  The new port will be
     *  an instance of MultiCompositePort, which extends TypedIOPort.
     *  This method is write-synchronized on the workspace, and increments
     *  its version number.
     *  @param name The name to assign to the newly created port.
     *  @return The new port.
     *  @exception NameDuplicationException If the entity already has a port
     *   with the specified name.
     */
    @Override
    public Port newPort(String name) throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            MultiCompositePort port = new MultiCompositePort(this, name);

            // Create mirror ports.
            Iterator entities = entityList(Refinement.class).iterator();
            while (entities.hasNext()) {
                Refinement entity = (Refinement) entities.next();
                if (entity.getPort(name) == null) {
                    try {
                        entity._mirrorDisable = true;
                        entity.newPort(name);
                    } finally {
                        entity._mirrorDisable = false;
                    }
                }
            }
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "MultiCompositeActor.newPort(): Internal error: "
                            + ex.getMessage());
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the model. */
    private void _init() {
        // The base class identifies the class name as TypedCompositeActor
        // irrespective of the actual class name.  We override that here.
        setClassName("ptolemy.actor.lib.hoc.MultiCompositeActor");

    }
}
