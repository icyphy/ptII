/* An IOPort for controllers and refinements in modal models.

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

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// RefinementPort
/**
A port for controllers and refinements in modal models.  This port
mirrors certain changes to it in the ports of the container of the container.
That container in turn mirrors those changes in other refinements and/or
controllers.  This class is designed to work closely with ModalPort,
since changes to the ports can be initiated from either class.

@see ModalPort
@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/

public class RefinementPort extends TypedIOPort {

    /** Construct a port in the given workspace.  
     *  @param workspace The workspace.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   TypedActor interface.
     */
    public RefinementPort(Workspace workspace) {
        super(workspace);
    }

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
    public RefinementPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the super class method to allow UNKNOWN type if this
     *  port does not have any inside links.
     *  @return True if this port does not have any inside links,
     *   or the super class method returns true.
     */
    public boolean isTypeAcceptable() {
        if (numInsideLinks() == 0) {
            return true;
        } else {
            return super.isTypeAcceptable();
        }
    }

    /** Override the base class so that if the port is being removed
     *  from the current container, then it is also removed from the
     *  controller and from each of the refinements.
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
        NamedObj oldContainer = (NamedObj)getContainer();
        if (container == oldContainer) {
            // Nothing to do.
            return;
        }
        boolean disableStatus = _mirrorDisable;
        try {
            _workspace.getWriteAccess();
            if (_mirrorDisable || getContainer() == null) {
                // Have already called the super class.
                // This time, process the request.
                super.setContainer(container);
            } else {
                _mirrorDisable = true;
                boolean success = false;
                if (oldContainer != null) {
                    Nameable modal = oldContainer.getContainer();
                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel)modal).getPort(getName());
                        if (port != null) {
                            port.setContainer(null);
                            success = true;
                        }
                    }
                }
                if (!success) super.setContainer(container);
            }
        } finally {
            _mirrorDisable = disableStatus;
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
        boolean disableStatus = _mirrorDisable;
        try {
            _workspace.getWriteAccess();
            if (_mirrorDisable || getContainer() == null) {
                // Have already called the super class.
                // This time, process the request.
                super.setInput(isInput);
            } else {
                _mirrorDisable = true;
                boolean success = false;
                Nameable container = getContainer();
                if (container != null) {
                    Nameable modal = container.getContainer();
                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel)modal).getPort(getName());
                        if (port instanceof IOPort) {
                            ((IOPort)port).setInput(isInput);
                            success = true;
                        }
                    }
                }
                if (!success) super.setInput(isInput);
            }
        } finally {
            _mirrorDisable = disableStatus;
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
        boolean disableStatus = _mirrorDisable;
        try {
            _workspace.getWriteAccess();
            if (_mirrorDisable || getContainer() == null) {
                // Have already called the super class.
                // This time, process the request.
                super.setMultiport(isMultiport);
            } else {
                _mirrorDisable = true;
                boolean success = false;
                Nameable container = getContainer();
                if (container != null) {
                    Nameable modal = container.getContainer();
                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel)modal).getPort(getName());
                        if (port instanceof IOPort) {
                            ((IOPort)port).setMultiport(isMultiport);
                            success = true;
                        }
                    }
                }
                if (!success) super.setMultiport(isMultiport);
            }
        } finally {
            _mirrorDisable = disableStatus;
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
        boolean disableStatus = _mirrorDisable;
        try {
            _workspace.getWriteAccess();
            if (_mirrorDisable || getContainer() == null) {
                // Have already called the super class.
                // This time, process the request.
                super.setName(name);
            } else {
                _mirrorDisable = true;
                boolean success = false;
                Nameable container = getContainer();
                if (container != null) {
                    Nameable modal = container.getContainer();
                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel)modal).getPort(getName());
                        if (port != null) {
                            port.setName(name);
                            success = true;
                        }
                    }
                }
                if (!success) super.setName(name);
            }
        } finally {
            _mirrorDisable = disableStatus;
            _workspace.doneWriting();
        }
    }

    /** If the argument is true, make the port an output port.
     *  If the argument is false, make the port not an output port.
     *  In addition, if the container is an instance of Refinement,
     *  and the argument is true, find the corresponding port of the
     *  controller and make it an input and not an output.  This makes
     *  it possible for the controller to see the outputs of the refinements.
     *  This method overrides the base class to make the same
     *  change on the mirror ports in the controller and state refinments.
     *  This method invalidates the schedule and resolved types of the
     *  director of the container, if there is one.
     *  It is write-synchronized on the workspace, and increments
     *  the version of the workspace.
     *  @param isOutput True to make the port an output.
     */
    public void setOutput(boolean isOutput) {
        boolean disableStatus = _mirrorDisable;
        try {
            _workspace.getWriteAccess();
            if (_mirrorDisable || getContainer() == null) {
                // Have already called the super class.
                // This time, process the request.
                super.setOutput(isOutput);
            } else {
                _mirrorDisable = true;
                boolean success = false;
                Nameable container = getContainer();
                if (container != null) {
                    Nameable modal = container.getContainer();
                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel)modal).getPort(getName());
                        if (port instanceof IOPort) {
                            ((IOPort)port).setOutput(isOutput);
                            success = true;
                        }
                    }
                }
                if (!success) super.setOutput(isOutput);
            }
        } finally {
            _mirrorDisable = disableStatus;
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // This is protected to be accessible to ModalPort.

    /** If false, then changes the port are mirrored in the container's
     *  container.
     */
    protected boolean _mirrorDisable = true;
}
