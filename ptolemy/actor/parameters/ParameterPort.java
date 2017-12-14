/* A port that updates a parameter of the container.

 Copyright (c) 2001-2015 The Regents of the University of California.
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
package ptolemy.actor.parameters;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ParameterPort

/**
 A specialized port for use with PortParameter.  This port is created
 by an instance of PortParameter and provides values to a parameter.
 Data should not be read directly from this port.  Instead, the update
 method of the corresponding PortParameter should be invoked.  This
 port is only useful if the container is opaque, however, this is not
 checked.

 @see PortParameter
 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Yellow (neuendor)
 */
public class ParameterPort extends TypedIOPort {
    /** Construct a new input port in the specified container with the
     *  specified name. The specified container
     *  must implement the Actor interface, or an exception will be thrown.
     *  @param container The container.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public ParameterPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, null);
    }

    /** Construct a new input port in the specified container with the
     *  specified name. The specified container
     *  must implement the Actor interface, or an exception will be thrown.
     *  @param container The container.
     *  @param name The name of the port.
     *  @param parameter The associate PortParameter, or null to create one.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    protected ParameterPort(ComponentEntity container, String name,
            PortParameter parameter)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setInput(true);
        setMultiport(false);
        _parameter = parameter;

        if (_parameter == null) {
            // This is apparently being created directly, e.g. in MoML, rather than
            // the preferred way of creating a PortParameter. Create or capture
            // the corresponding PortParameter.
            Attribute existingParameter = container.getAttribute(name);
            if (existingParameter instanceof PortParameter) {
                _parameter = (PortParameter) existingParameter;
                _parameter._port = this;
            } else {
                _parameter = new PortParameter(container, name, this);
            }
        }
        _setTypeConstraints();

        // Declare to the SDF scheduler that this port consumes one
        // token, despite not being connected on the inside.
        Parameter tokenConsumptionRate = new Parameter(this,
                "tokenConsumptionRate", new IntToken(1));
        tokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        tokenConsumptionRate.setPersistent(false);

        Parameter notDraggable = new Parameter(this, "_notDraggable");
        notDraggable.setPersistent(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the port. This overrides the base class to remove
     *  the current association with a parameter.  It is assumed that the
     *  parameter will also be cloned, and when the containers are set of
     *  this port and that parameter, whichever one is set second
     *  will result in re-establishment of the association.
     *  @param workspace The workspace in which to place the cloned port.
     *  @exception CloneNotSupportedException Not thrown in this base class.
     *  @see java.lang.Object#clone()
     *  @return The cloned port.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ParameterPort newObject = (ParameterPort) super.clone(workspace);

        // Cannot establish an association with the cloned parameter until
        // that parameter is cloned and the container of both is set.
        newObject._parameter = null;
        return newObject;
    }

    /** Get the associated parameter.
     *  @return The associated parameter.
     */
    public PortParameter getParameter() {
        if (_parameter == null) {
            // Attempt to find the parameter.
            NamedObj container = getContainer();
            if (container != null) {
                Attribute candidate = container.getAttribute(getName());
                if (candidate instanceof PortParameter) {
                    _parameter = (PortParameter) candidate;
                    _setTypeConstraints();
                }
            }
        }
        return _parameter;
    }

    /** Set the container of this port. If the container is different
     *  from what it was before and there is an associated parameter, then
     *  also change the container of the parameter.
     *  @see PortParameter
     *  @param entity The new container.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    @Override
    public void setContainer(Entity entity)
            throws IllegalActionException, NameDuplicationException {
        if (_settingContainer) {
            // Recursive call through the parameter.
            return;
        }
        Entity previousContainer = (Entity) getContainer();
        if (previousContainer == entity) {
            // No change.
            return;
        }
        super.setContainer(entity);

        // If there is an associated parameter, and the container has changed,
        // change that container too.
        if (_parameter != null) {
            try {
                _settingContainer = true;
                _parameter.setContainer(entity);
            } catch (KernelException ex) {
                super.setContainer(previousContainer);
                throw ex;
            } finally {
                _settingContainer = false;
            }
        }
        if (previousContainer == null) {
            // This may be part of cloning operation, in which case we need to
            // establish type constraints that are present in the original.
            // Since ports are normally cloned after parameters, the container should
            // have the relevant parameter by now.
            if (getParameter() != null) {
                _setTypeConstraints();
            }
        }
    }

    /** Set the display name, and propagate the name change to the
     *  associated parameter.
     *  Increment the version of the workspace.
     *  This method is write-synchronized on the workspace.
     *  @param name The new display name..
     */
    @Override
    public void setDisplayName(String name) {
        if (_settingName) {
            return;
        }
        super.setDisplayName(name);
        PortParameter parameter = getParameter();
        if (parameter != null) {
            try {
                _settingName = true;
                parameter.setDisplayName(name);
            } finally {
                _settingName = false;
            }
        }
    }

    /** Set the name, and propagate the name change to the
     *  associated parameter.  If a null argument is given, then the
     *  name is set to an empty string.
     *  Increment the version of the workspace.
     *  This method is write-synchronized on the workspace.
     *  @param name The new name.
     *  @exception IllegalActionException If the name contains a period.
     *  @exception NameDuplicationException If the container already
     *   contains an attribute with the proposed name.
     */
    @Override
    public void setName(String name)
            throws IllegalActionException, NameDuplicationException {
        if (_settingName) {
            return;
        }
        super.setName(name);
        PortParameter parameter = getParameter();
        if (parameter != null) {
            String oldName = getName();
            try {
                _settingName = true;
                parameter.setName(name);
            } catch (KernelException ex) {
                super.setName(oldName);
                throw ex;
            } finally {
                _settingName = false;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set the type constraints between the protected member _parameter
     *  and this port.  This is a protected method so that subclasses
     *  can define different type constraints.  It is assured that when
     *  this is called, _parameter is non-null.  However, use caution,
     *  since this method may be called during construction of this
     *  port, and hence the port may not be fully constructed.
     */
    protected void _setTypeConstraints() {
        PortParameter parameter = getParameter();
        if (parameter != null) {
            parameter.setTypeSameAs(this);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected members                   ////

    /** The associated parameter. */
    protected PortParameter _parameter;

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Indicator that we are in the midst of setting the container. */
    private boolean _settingContainer = false;

    /** Indicator that we are in the midst of setting the name. */
    private boolean _settingName = false;
}
