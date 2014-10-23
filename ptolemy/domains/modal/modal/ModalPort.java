/* An IOPort for modal models.

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

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ModalPort

/**
 * A port for modal models. This port mirrors certain changes to it in the ports
 * of the controller and refinements of the modal model. It is designed to work
 * closely with RefinementPort, since changes to the ports can be initiated in
 * either class.
 *
 * @see RefinementPort
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (eal)
 * @Pt.AcceptedRating Red (liuxj)
 */
public class ModalPort extends ModalBasePort {
    /**
     * Construct a port in the given workspace.
     *
     * @param workspace
     *            The workspace.
     * @exception IllegalActionException
     *                If the port is not of an acceptable class for the
     *                container, or if the container does not implement the
     *                TypedActor interface.
     */
    public ModalPort(Workspace workspace) throws IllegalActionException {
        super(workspace);
    }

    /**
     * Construct a port with a containing actor and a name that is neither an
     * input nor an output. The specified container must implement the
     * TypedActor interface, or an exception will be thrown.
     *
     * @param container
     *            The container actor.
     * @param name
     *            The name of the port.
     * @exception IllegalActionException
     *                If the port is not of an acceptable class for the
     *                container, or if the container does not implement the
     *                TypedActor interface.
     * @exception NameDuplicationException
     *                If the name coincides with a port already in the
     *                container.
     */
    public ModalPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * React to attribute changes.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == defaultValue) {
            if (_getMirrorPort() != null) {
                _getMirrorPort().defaultValue.setExpression(defaultValue
                        .getExpression());
            }
        }
        super.attributeChanged(attribute);
    }

    /**
     * Move this object down by one in the list of attributes of its container.
     * If this object is already last, do nothing. This method overrides the
     * base class to mirror the change in any mirror ports. Increment the
     * version of the workspace.
     *
     * @return The index of the specified object prior to moving it, or -1 if it
     *         is not moved.
     * @exception IllegalActionException
     *                If this object has no container.
     */
    @Override
    public int moveDown() throws IllegalActionException {
        return _moveDown();
    }

    /**
     * Move this object to the first position in the list of attributes of the
     * container. If this object is already first, do nothing. Increment the
     * version of the workspace. This method overrides the base class to mirror
     * the change in any mirror ports.
     *
     * @return The index of the specified object prior to moving it, or -1 if it
     *         is not moved.
     * @exception IllegalActionException
     *                If this object has no container.
     */
    @Override
    public int moveToFirst() throws IllegalActionException {
        return _moveToFirst();
    }

    /**
     * Move this object to the specified position in the list of attributes of
     * the container. If this object is already at the specified position, do
     * nothing. This method overrides the base class to mirror the change in any
     * mirror ports. Increment the version of the workspace.
     *
     * @param index
     *            The position to move this object to.
     * @return The index of the specified object prior to moving it, or -1 if it
     *         is not moved.
     * @exception IllegalActionException
     *                If this object has no container or if the index is out of
     *                bounds.
     */
    @Override
    public int moveToIndex(int index) throws IllegalActionException {
        return _moveToIndex(index);
    }

    /**
     * Move this object to the last position in the list of attributes of the
     * container. If this object is already last, do nothing. This method
     * overrides the base class to mirror the change in any mirror ports.
     * Increment the version of the workspace.
     *
     * @return The index of the specified object prior to moving it, or -1 if it
     *         is not moved.
     * @exception IllegalActionException
     *                If this object has no container.
     */
    @Override
    public int moveToLast() throws IllegalActionException {
        return _moveToLast();
    }

    /**
     * Move this object up by one in the list of attributes of the container. If
     * this object is already first, do nothing. This method overrides the base
     * class to mirror the change in any mirror ports. Increment the version of
     * the workspace.
     *
     * @return The index of the specified object prior to moving it, or -1 if it
     *         is not moved.
     * @exception IllegalActionException
     *                If this object has no container.
     */
    @Override
    public int moveUp() throws IllegalActionException {
        return _moveUp();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Override the base class so that if the port is being removed from the
     * current container, then it is also removed from the controller and from
     * each of the refinements. This method is write-synchronized on the
     * workspace.
     *
     * @param container
     *            The proposed container.
     * @exception IllegalActionException
     *                If the proposed container is not a ComponentEntity,
     *                doesn't implement Actor, or has no name, or the port and
     *                container are not in the same workspace. Or it's not null
     * @exception NameDuplicationException
     *                If the container already has a port with the name of this
     *                port.
     */
    @Override
    public void setContainer(Entity container) throws IllegalActionException,
    NameDuplicationException {
        // See also ModelRefinementPort.setContainer() for a similar method.
        try {
            _workspace.getWriteAccess();

            CompositeEntity model = (CompositeEntity) getContainer();

            if (model != null && container != model) {
                // The port is being removed from the current container.
                // Remove it from the mirrored ports.
                _removePort(model);
            }

            super.setContainer(container);
        } finally {
            _workspace.doneWriting();
        }
    }

    /**
     * If the argument is true, make the port an input port. If the argument is
     * false, make the port not an input port. This method overrides the base
     * class to make the same change on the mirror ports in the controller and
     * state refinments. This method invalidates the schedule and resolved types
     * of the director of the container, if there is one. It is
     * write-synchronized on the workspace, and increments the version of the
     * workspace.
     *
     * @param isInput
     *            True to make the port an input.
     * @exception IllegalActionException
     *                If changing the port status is not permitted.
     */
    @Override
    public void setInput(boolean isInput) throws IllegalActionException {
        _setInput(isInput);
    }

    /**
     * If the argument is true, make the port a multiport. If the argument is
     * false, make the port not a multiport. This method overrides the base
     * class to make the same change on the mirror ports in the controller and
     * state refinments. This method invalidates the schedule and resolved types
     * of the director of the container, if there is one. It is
     * write-synchronized on the workspace, and increments the version of the
     * workspace.
     *
     * @param isMultiport
     *            True to make the port a multiport.
     * @exception IllegalActionException
     *                If changing the port status is not permitted.
     */
    @Override
    public void setMultiport(boolean isMultiport) throws IllegalActionException {
        _setMultiport(isMultiport);
    }

    /**
     * Set the name of the port, and mirror the change in all the mirror ports.
     * This method is write-synchronized on the workspace, and increments the
     * version of the workspace.
     *
     * @exception IllegalActionException
     *                If the name has a period.
     * @exception NameDuplicationException
     *                If there is already a port with the same name in the
     *                container.
     */
    @Override
    public void setName(String name) throws IllegalActionException,
    NameDuplicationException {
        _setName(name);
    }

    /**
     * If the argument is true, make the port an output port. If the argument is
     * false, make the port not an output port. This method overrides the base
     * class to make the same change on the mirror ports in the controller and
     * state refinments. This method invalidates the schedule and resolved types
     * of the director of the container, if there is one. It is
     * write-synchronized on the workspace, and increments the version of the
     * workspace.
     *
     * @param isOutput
     *            True to make the port an output.
     * @exception IllegalActionException
     *                If changing the port status is not permitted.
     */
    @Override
    public void setOutput(boolean isOutput) throws IllegalActionException {
        _setOutput(isOutput);
    }

    private RefinementPort _getMirrorPort() {
        if (getContainer() != null
                && getContainer() instanceof ModalModel
                && ((ModalModel) getContainer())._controller != null
                && ((ModalModel) getContainer())._controller.getPort(this
                        .getName()) != null) {
            if (((ModalModel) getContainer())._controller.getPort(this
                    .getName()) instanceof RefinementPort) {
                return (RefinementPort) ((ModalModel) getContainer())._controller
                        .getPort(this.getName());
            }
        }
        return null;
    }
}
