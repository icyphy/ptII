/* An IOPort for controllers and refinements in modal models.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.domains.modal.modal;

import java.util.Set;

import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedIOPort;
import ptolemy.graph.Inequality;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
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
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (liuxj)
 */
public class RefinementPort extends ModalBasePort {
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
        _checkWhetherMirrorIsInput();
        _init();
    }

    /** Construct a port in the given workspace.
     *  @param workspace The workspace.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   TypedActor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public RefinementPort(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
        _checkWhetherMirrorIsInput();
        _init();
    }

    /** React to attribute changes.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == defaultValue) {
            IOPort mirrorPort = _getMirrorPort();
            if (mirrorPort != null) {
                mirrorPort.defaultValue.setExpression(defaultValue
                        .getExpression());
            }
        }
        super.attributeChanged(attribute);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the super class method to allow UNKNOWN type if this
     *  port does not have any inside links.
     *  @return True if this port does not have any inside links,
     *   or the super class method returns true.
     */
    @Override
    public boolean isTypeAcceptable() {
        if (numInsideLinks() == 0) {
            return true;
        } else {
            return super.isTypeAcceptable();
        }
    }

    /** Set the connected relation to a bus if this port is a multiport.
     */
    @Override
    public void link(Relation relation) throws IllegalActionException {
        super.link(relation);

        if (isMultiport()) {
            ((IORelation) relation).setWidth(IORelation.WIDTH_TO_INFER);
        }
    }

    /** Move this object down by one in the list of attributes of
     *  its container. If this object is already last, do nothing.
     *  This method overrides the base class to mirror the change
     *  in any mirror ports.
     *  Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    @Override
    public int moveDown() throws IllegalActionException {
        boolean disableStatus = _mirrorDisable;

        try {
            _workspace.getWriteAccess();

            int result = -1;

            if (_mirrorDisable || getContainer() == null) {
                result = super.moveDown();
            } else {
                _mirrorDisable = true;

                boolean success = false;
                Nameable container = getContainer();

                if (container != null) {
                    Nameable modal = container.getContainer();

                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel) modal).getPort(getName());

                        if (port instanceof IOPort) {
                            ((IOPort) port).moveDown();
                            success = true;
                        }
                    }
                }

                // The mirror port(s), if there are any,
                // will have called this method and achieved
                // the moveDown. But if there are no mirror
                // ports, we need to do it here.
                if (!success) {
                    result = super.moveDown();
                }
            }

            return result;
        } finally {
            _mirrorDisable = disableStatus;
            _workspace.doneWriting();
        }
    }

    /** Move this object to the first position in the list
     *  of attributes of the container. If  this object is already first,
     *  do nothing. Increment the version of the workspace.
     *  This method overrides the base class to mirror the change
     *  in any mirror ports.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    @Override
    public int moveToFirst() throws IllegalActionException {
        boolean disableStatus = _mirrorDisable;

        try {
            _workspace.getWriteAccess();

            int result = -1;

            if (_mirrorDisable || getContainer() == null) {
                result = super.moveToFirst();
            } else {
                _mirrorDisable = true;

                boolean success = false;
                Nameable container = getContainer();

                if (container != null) {
                    Nameable modal = container.getContainer();

                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel) modal).getPort(getName());

                        if (port instanceof IOPort) {
                            ((IOPort) port).moveToFirst();
                            success = true;
                        }
                    }
                }

                // The mirror port(s), if there are any,
                // will have called this method and achieved
                // the moveToFirst. But if there are no mirror
                // ports, we need to do it here.
                if (!success) {
                    result = super.moveToFirst();
                }
            }

            return result;
        } finally {
            _mirrorDisable = disableStatus;
            _workspace.doneWriting();
        }
    }

    /** Move this object to the specified position in the list
     *  of attributes of the container. If this object is already at the
     *  specified position, do nothing.
     *  This method overrides the base class to mirror the change
     *  in any mirror ports.
     *  Increment the version of the workspace.
     *  @param index The position to move this object to.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container or if the index is out of bounds.
     */
    @Override
    public int moveToIndex(int index) throws IllegalActionException {
        boolean disableStatus = _mirrorDisable;

        try {
            _workspace.getWriteAccess();

            int result = -1;

            if (_mirrorDisable || getContainer() == null) {
                result = super.moveToIndex(index);
            } else {
                _mirrorDisable = true;

                boolean success = false;
                Nameable container = getContainer();

                if (container != null) {
                    Nameable modal = container.getContainer();

                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel) modal).getPort(getName());

                        if (port instanceof IOPort) {
                            ((IOPort) port).moveToIndex(index);
                            success = true;
                        }
                    }
                }

                // The mirror port(s), if there are any,
                // will have called this method and achieved
                // the moveToIndex. But if there are no mirror
                // ports, we need to do it here.
                if (!success) {
                    result = super.moveToIndex(index);
                }
            }

            return result;
        } finally {
            _mirrorDisable = disableStatus;
            _workspace.doneWriting();
        }
    }

    /** Move this object to the last position in the list
     *  of attributes of the container.  If this object is already last,
     *  do nothing. This method overrides the base class to mirror the change
     *  in any mirror ports.
     *  Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    @Override
    public int moveToLast() throws IllegalActionException {
        boolean disableStatus = _mirrorDisable;

        try {
            _workspace.getWriteAccess();

            int result = -1;

            if (_mirrorDisable || getContainer() == null) {
                result = super.moveToLast();
            } else {
                _mirrorDisable = true;

                boolean success = false;
                Nameable container = getContainer();

                if (container != null) {
                    Nameable modal = container.getContainer();

                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel) modal).getPort(getName());

                        if (port instanceof IOPort) {
                            ((IOPort) port).moveToLast();
                            success = true;
                        }
                    }
                }

                if (!success) {
                    result = super.moveToLast();
                }
            }

            return result;
        } finally {
            _mirrorDisable = disableStatus;
            _workspace.doneWriting();
        }
    }

    /** Move this object up by one in the list of
     *  attributes of the container. If  this object is already first, do
     *  nothing.
     *  This method overrides the base class to mirror the change
     *  in any mirror ports.
     *  Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    @Override
    public int moveUp() throws IllegalActionException {
        boolean disableStatus = _mirrorDisable;

        try {
            _workspace.getWriteAccess();

            int result = -1;

            if (_mirrorDisable || getContainer() == null) {
                result = super.moveUp();
            } else {
                _mirrorDisable = true;

                boolean success = false;
                Nameable container = getContainer();

                if (container != null) {
                    Nameable modal = container.getContainer();

                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel) modal).getPort(getName());

                        if (port instanceof IOPort) {
                            ((IOPort) port).moveUp();
                            success = true;
                        }
                    }
                }

                // The mirror port(s), if there are any,
                // will have called this method and achieved
                // the moveUp. But if there are no mirror
                // ports, we need to do it here.
                if (!success) {
                    result = super.moveUp();
                }
            }

            return result;
        } finally {
            _mirrorDisable = disableStatus;
            _workspace.doneWriting();
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
    @Override
    public void setContainer(Entity container) throws IllegalActionException,
    NameDuplicationException {
        NamedObj oldContainer = getContainer();

        if (container == oldContainer) {
            // Nothing to do.
            return;
        }

        boolean disableStatus = _mirrorDisable;

        try {
            _workspace.getWriteAccess();

            if (_mirrorDisable || oldContainer == null) {
                // Mirroring changes from above, or there
                // is no pre-existing above.
                super.setContainer(container);
            } else {
                // Not mirroring changes from above.
                // Delegate upwards in the hierarchy.
                _mirrorDisable = true;

                if (oldContainer != null) {
                    // This port previously had a container.
                    // Remove any mirror ports from their container.
                    // This will result the container calling this
                    // setContainer() method with a null argument,
                    // thus removing this port from its container.
                    Nameable modal = oldContainer.getContainer();
                    boolean success = false;
                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel) modal).getPort(getName());

                        if (port != null) {
                            port.setContainer(null);
                            success = true;
                        }
                    }
                    if (!success) {
                        // There was no mirror port above in the hierarchy,
                        // so do this directly.
                        super.setContainer(container);
                    }
                }
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
     *  @exception IllegalActionException If changing the port status is
     *   not permitted.
     */
    @Override
    public void setInput(boolean isInput) throws IllegalActionException {
        boolean disableStatus = _mirrorDisable;

        try {
            _workspace.getWriteAccess();

            if (_mirrorDisable || getContainer() == null || _automaticallyInput) {
                // Do not mirror.
                super.setInput(isInput);
            } else {
                _mirrorDisable = true;

                boolean success = false;
                Nameable container = getContainer();

                if (container != null) {
                    Nameable modal = container.getContainer();

                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel) modal).getPort(getName());

                        if (port instanceof IOPort) {
                            ((IOPort) port).setInput(isInput);
                            success = true;
                        }
                    }
                }

                if (!success) {
                    super.setInput(isInput);
                }
            }
        } finally {
            _mirrorDisable = disableStatus;
            _workspace.doneWriting();
        }
    }

    /** Control whether any change should be mirrored in the modal
     *  model, mode controller, and refinements.
     *  This is added to allow control by the UI.
     *  @param disable True if mirroring should not occur.
     */
    public void setMirrorDisable(boolean disable) {
        _mirrorDisable = disable;
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
     *  @exception IllegalActionException If changing the port status is
     *   not permitted.
     */
    @Override
    public void setMultiport(boolean isMultiport) throws IllegalActionException {
        boolean disableStatus = _mirrorDisable;

        try {
            _workspace.getWriteAccess();

            if (_mirrorDisable || getContainer() == null) {
                // Do not mirror.
                super.setMultiport(isMultiport);
            } else {
                _mirrorDisable = true;

                boolean success = false;
                Nameable container = getContainer();

                if (container != null) {
                    Nameable modal = container.getContainer();

                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel) modal).getPort(getName());

                        if (port instanceof IOPort) {
                            ((IOPort) port).setMultiport(isMultiport);
                            success = true;
                        }
                    }
                }

                if (!success) {
                    super.setMultiport(isMultiport);
                }
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
    @Override
    public void setName(String name) throws IllegalActionException,
    NameDuplicationException {
        boolean disableStatus = _mirrorDisable;

        try {
            _workspace.getWriteAccess();

            if (_mirrorDisable || getContainer() == null) {
                // Do not mirror.
                super.setName(name);
            } else {
                _mirrorDisable = true;

                boolean success = false;
                Nameable container = getContainer();

                if (container != null) {
                    Nameable modal = container.getContainer();

                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel) modal).getPort(getName());

                        if (port != null) {
                            port.setName(name);
                            success = true;
                        }
                    }
                }

                if (!success) {
                    super.setName(name);
                }
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
     *  @exception IllegalActionException If changing the port status is
     *   not permitted.
     */
    @Override
    public void setOutput(boolean isOutput) throws IllegalActionException {
        boolean disableStatus = _mirrorDisable;

        try {
            _workspace.getWriteAccess();

            if (_mirrorDisable || getContainer() == null) {
                // Do not mirror.
                super.setOutput(isOutput);
            } else {
                _mirrorDisable = true;

                boolean success = false;
                Nameable container = getContainer();

                if (container != null) {
                    Nameable modal = container.getContainer();

                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel) modal).getPort(getName());

                        if (port instanceof IOPort) {
                            ((IOPort) port).setOutput(isOutput);
                            success = true;
                        }
                    }
                }

                if (!success) {
                    super.setOutput(isOutput);
                }
            }
        } finally {
            _mirrorDisable = disableStatus;
            _workspace.doneWriting();
        }
    }

    /** Return the type constraints of this port in the form of a
     *  set of inequalities.
     *  @return A set of inequalities.
     *  @see ptolemy.graph.Inequality
     */
    @Override
    public Set<Inequality> typeConstraints() {
        Nameable container = getContainer();
        if (container != null) {
            Nameable modal = container.getContainer();
            if (modal instanceof ModalModel) {
                Port port = ((ModalModel) modal).getPort(getName());
                if (port instanceof TypedIOPort) {
                    // Whether this port is an input or output,
                    // ensure that its type is at least that
                    // the container's port. The reason this makes sense
                    // if its an output is that the output will also
                    // be an input to the controller.
                    setTypeAtLeast((TypedIOPort) port);
                    if (isOutput()) {
                        ((TypedIOPort) port).setTypeAtLeast(this);
                    }
                }
            }
        }
        return super.typeConstraints();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to ensure that the proposed container
     *  implements the TypedActor interface (the base class ensures that
     *  the container implements the Actor interface), is null, or is
     *  an EntityLibrary.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the proposed container is not a
     *   TypedActor, or if the base class throws it.
     */
    @Override
    protected void _checkContainer(Entity container)
            throws IllegalActionException {
        // This is copied from TypedIOPort because the parent class of
        // this class (RefinementPort) is ModalBasePort, which has a
        // _checkContainer() method used in ModalPort and ModalRefinementPort.
        // This is convoluted, but avoids quite a bit of code duplication.
        _checkTypedIOPortContainer(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Indicator that the port is automatically an input port
     *  because it was set to be an output port. This automatic change
     *  is made in output ports of the ModalController so that if
     *  a refinement writes to its output port, then that data is
     *  available in scope for the guard expressions.
     */
    protected boolean _automaticallyInput = false;

    // This is protected to be accessible to ModalPort.

    /** If false, then changes the port are mirrored in the container's
     *  container. This is false by default.
     */
    protected boolean _mirrorDisable = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** If the container is a ModalModel and has a mirror port to this
     *  one that is not an input and is an output, then mark this port
     *  so that if it becomes an input, that input property is not
     *  reflected to the mirror port.
     */
    private void _checkWhetherMirrorIsInput() {
        // Need to check whether there is a containing ModalModel,
        // and whether its mirror port is also an input.
        Nameable container = getContainer();

        if (container != null) {
            Nameable modal = container.getContainer();

            if (modal instanceof ModalModel) {
                Port port = ((ModalModel) modal).getPort(getName());

                if (port instanceof IOPort) {
                    if (!((IOPort) port).isInput()
                            && ((IOPort) port).isOutput()) {
                        _automaticallyInput = true;
                    }
                }
            }
        }
    }

    private ModalPort _getMirrorPort() {
        Nameable container = getContainer();

        if (container != null) {
            Nameable modal = container.getContainer();

            if (modal instanceof ModalModel) {
                if (((ModalModel) modal).getPort(getName()) instanceof ModalPort) {
                    return (ModalPort) ((ModalModel) modal).getPort(getName());
                }
            }
        }
        return null;
    }

    private void _init() throws IllegalActionException,
            NameDuplicationException {
        // Need to check whether there is a containing ModalModel,
        // and whether its mirror port is also an input.
        Nameable container = getContainer();

        if (container != null) {
            Nameable modal = container.getContainer();

            if (modal instanceof ModalModel) {
                Port port = ((ModalModel) modal).getPort(getName());

                if (port instanceof ModalPort) {
                    defaultValue.setExpression(((ModalPort) port).defaultValue
                            .getExpression());
                }
            }
        }
    }
}
