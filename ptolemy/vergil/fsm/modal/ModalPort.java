/* An IOPort for modal models.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

package ptolemy.vergil.fsm.modal;

import java.util.Iterator;

import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ModalPort
/**
A port for modal models.  This port mirrors certain changes to it in the
ports of the controller and refinements of the modal model. It is designed
to work closely with RefinementPort, since changes to the ports can be initiated
in either class.

@see RefinementPort
@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
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

    /** Override the base class so that if the port is being removed
     *  from the current container, then it is also removed from the
     *  controller and from each of the refinements.  This method is
     *  write-synchronized on the workspace.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the proposed container is not a
     *   ComponentEntity, doesn't implement Actor, or has no name,
     *   or the port and container are not in the same workspace. Or
     *   it's not null
     *  @exception NameDuplicationException If the container already has
     *   a port with the name of this port.
     */
    public void setContainer(Entity container)
            throws IllegalActionException, NameDuplicationException {
        try {
            _workspace.getWriteAccess();
            ModalModel model = (ModalModel)getContainer();
            if (model != null && container != model) {
                // The port is being removed from the current container.
                // Remove it from the mirrored ports.
                Iterator entities = model.entityList().iterator();
                while (entities.hasNext()) {
                    Entity entity = (Entity)entities.next();
                    Port mirrorPort = entity.getPort(getName());
                    if (mirrorPort instanceof RefinementPort) {
                        RefinementPort castPort = (RefinementPort)mirrorPort;
                        boolean disableStatus = castPort._mirrorDisable;
                        try {
                            castPort._mirrorDisable = true;
                            castPort.setContainer(null);
                        } finally {
                            castPort._mirrorDisable = disableStatus;
                        }
                    }
                }
                // Remove the relation as well.
                ComponentRelation relation = (ComponentRelation)
                    model.getRelation(getName() + "Relation");
                if (relation != null) {
                    relation.setContainer(null);
                }
            }
            super.setContainer(container);
        } finally {
            _workspace.doneWriting();
        }
    }

    /** If the argument is true, make the port an input port.
     *  If the argument is false, make the port not an input port.
     *  This method overrides the base class to make the same
     *  change on the mirror ports in the controller and state refinments.
     *  This method invalidates the schedule and resolved types of the
     *  director of the container, if there is one.
     *  It is write-synchronized on the workspace, and increments
     *  the version of the workspace.
     *  @param isInput True to make the port an input.
     */
    public void setInput(boolean isInput) {
        try {
            _workspace.getWriteAccess();

            super.setInput(isInput);
            // Mirror the change in mirror ports.
            ModalModel container = (ModalModel)getContainer();
            Iterator entities = container.entityList().iterator();
            while (entities.hasNext()) {
                Entity entity = (Entity)entities.next();
                Port mirrorPort = entity.getPort(getName());
                if (mirrorPort instanceof RefinementPort) {
                    RefinementPort castPort = (RefinementPort)mirrorPort;
                    boolean disableStatus = castPort._mirrorDisable;
                    try {
                        castPort._mirrorDisable = true;
                        castPort.setInput(isInput);
                    } finally {
                        castPort._mirrorDisable = disableStatus;
                    }
                }
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** If the argument is true, make the port a multiport.
     *  If the argument is false, make the port not a multiport.
     *  This method overrides the base class to make the same
     *  change on the mirror ports in the controller and state refinments.
     *  This method invalidates the schedule and resolved types of the
     *  director of the container, if there is one.
     *  It is write-synchronized on the workspace, and increments
     *  the version of the workspace.
     *  @param isMultiport True to make the port a multiport.
     */
    public void setMultiport(boolean isMultiport) {
        try {
            _workspace.getWriteAccess();

            super.setMultiport(isMultiport);
            // Mirror the change in mirror ports.
            ModalModel container = (ModalModel)getContainer();
            Iterator entities = container.entityList().iterator();
            while (entities.hasNext()) {
                Entity entity = (Entity)entities.next();
                Port mirrorPort = entity.getPort(getName());
                if (mirrorPort instanceof RefinementPort) {
                    RefinementPort castPort = (RefinementPort)mirrorPort;
                    boolean disableStatus = castPort._mirrorDisable;
                    try {
                        castPort._mirrorDisable = true;
                        castPort.setMultiport(isMultiport);
                    } finally {
                        castPort._mirrorDisable = disableStatus;
                    }
                }
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Set the name of the port, and mirror the change in all the
     *  mirror ports.
     *  This method is write-synchronized on the workspace, and increments
     *  the version of the workspace.
     *  @exception IllegalActionException If the name has a period.
     *  @exception NameDuplicationException If there is already a port
     *   with the same name in the container.
     */
    public void setName(String name)
            throws IllegalActionException, NameDuplicationException {
        try {
            _workspace.getWriteAccess();
            String oldName = getName();
            super.setName(name);
            // Mirror the change in mirror ports.
            ModalModel container = (ModalModel)getContainer();
            // NOTE: This is called before there is even a container
            // to originally set the name.
            if (container != null) {
                Iterator entities = container.entityList().iterator();
                while (entities.hasNext()) {
                    Entity entity = (Entity)entities.next();
                    Port mirrorPort = entity.getPort(oldName);
                    if (mirrorPort instanceof RefinementPort) {
                        RefinementPort castPort = (RefinementPort)mirrorPort;
                        boolean disableStatus = castPort._mirrorDisable;
                        try {
                            castPort._mirrorDisable = true;
                            castPort.setName(name);
                        } finally {
                            castPort._mirrorDisable = disableStatus;
                        }
                    }
                }
                // Rename the corresponding relation.
                Relation relation = container.getRelation(oldName + "Relation");
                if (relation != null) {
                    relation.setName(name + "Relation");
                }
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** If the argument is true, make the port an output port.
     *  If the argument is false, make the port not an output port.
     *  This method overrides the base class to make the same
     *  change on the mirror ports in the controller and state refinments.
     *  This method invalidates the schedule and resolved types of the
     *  director of the container, if there is one.
     *  It is write-synchronized on the workspace, and increments
     *  the version of the workspace.
     *  @param isOutput True to make the port an output.
     */
    public void setOutput(boolean isOutput) {
        try {
            _workspace.getWriteAccess();

            super.setOutput(isOutput);
            // Mirror the change in mirror ports.
            ModalModel container = (ModalModel)getContainer();
            Iterator entities = container.entityList().iterator();
            while (entities.hasNext()) {
                Entity entity = (Entity)entities.next();
                Port mirrorPort = entity.getPort(getName());
                if (mirrorPort instanceof RefinementPort) {
                    RefinementPort castPort = (RefinementPort)mirrorPort;
                    boolean disableStatus = castPort._mirrorDisable;
                    try {
                        castPort._mirrorDisable = true;
                        castPort.setOutput(isOutput);
                    } finally {
                        castPort._mirrorDisable = disableStatus;
                    }
                    // If the entity is a controller, then set the
                    // port to also be an input.
                    if (entity.getName().equals("_Controller")) {
                        boolean controlPortStatus = castPort._mirrorDisable;
                        try {
                            castPort._mirrorDisable = true;
                            castPort.setInput(true);
                            // If we don't do the following,
                            // this will port will both an input
                            // and an output in the controller,
                            // which is probably what we want.
                            // castPort.setOutput(false);
                        } finally {
                            castPort._mirrorDisable = controlPortStatus;
                        }
                    }
                }
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to ensure that the proposed container
     *  is a ModalModel or null.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the proposed container is not a
     *   TypedActor, or if the base class throws it.
     */
    protected void _checkContainer(Entity container)
            throws IllegalActionException {
        if (!(container instanceof ModalModel) && (container != null)) {
            throw new IllegalActionException(container, this,
                    "ModalPort can only be contained by ModalModel objects. "
                    + "The container was: " + container);
        }
    }
}
