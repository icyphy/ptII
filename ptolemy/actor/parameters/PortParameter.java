/* A parameter that has an associated port.

 Copyright (c) 2002-2003 The Regents of the University of California.
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
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.parameters;

import ptolemy.actor.TypedActor;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// PortParameter
/**
This parameter creates an associated port that can be used to update
the current value of the parameter. This parameter has two values,
which may not be equal, a <i>current value</i> and a <i>persistent value</i>.
The persistent value is returned by
getExpression() and is set by any of three different mechanisms:
<ul>
<li> calling setExpression();
<li> calling setToken(); and
<li> specifying a value as a constructor argument.
</ul>
All three of these will also set the current value, which is then
equal to the persistent value.
The current value is returned by get getToken()
and is set by any of three different mechanisms:
<ul>
<li> calling setCurrentValue();
<li> calling update() sets the current value if there is an associated
     port, and that port has a token to consume; and
</ul>
These three techniques do not change the persistent value, so after
these are used, the persistent value and current value may be different.
<p>
When using this parameter in an actor, care must be exercised
to call update() exactly once per firing prior to calling getToken().
Each time update() is called, a new token will be consumed from
the associated port (if the port is connected and has a token).
If this is called multiple times in an iteration, it may result in
consuming tokens that were intended for subsequent iterations.
Thus, for example, update() should not be called in fire() and then
again in postfire().  Moreover, in some domains (such as DE),
it is essential that if a token is provided on a port, that it
is consumed.  In DE, the actor will be repeatedly fired until
the token is consumed.  Thus, it is an error to not call update()
once per iteration.  For an example of an actor that uses this
mechanism, see Ramp.
<p>
If this actor is placed in a container that does not implement
the TypedActor interface, then no associated port is created,
and it functions as an ordinary parameter.  This is useful,
for example, if this is put in a library, where one would not
want the associated port to appear.

@see ptolemy.actor.lib.Ramp
@see ParameterPort
@author Edward A. Lee
@version $Id$
*/
public class PortParameter extends Parameter {

    /** Construct a parameter with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will create
     *  an associated port in the same container.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public PortParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        if (container instanceof TypedActor) {
            // If we get to here, we know the container is a ComponentEntity,
            // so the cast is safe.
            _port = new ParameterPort((ComponentEntity)container, name);
        }
    }

    /** Construct a Parameter with the given container, name, and Token.
     *  The token defines the initial persistent and current values.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  @param container The container.
     *  @param name The name.
     *  @param token The Token contained by this Parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an parameter already in the container.
     */
    public PortParameter(
            NamedObj container, String name, ptolemy.data.Token token)
            throws IllegalActionException, NameDuplicationException {
        this(container, name);
        setToken(token);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  In this class,
     *  if the attribute is an instance of Location, then the location
     *  of the associated port is set as well.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute instanceof Locatable) {
            Locatable location = (Locatable)attribute;
            if (_port != null) {
                Attribute portAttribute = _port.getAttribute("_location");
                Locatable portLocation = null;
                if (portAttribute instanceof Locatable) {
                    portLocation = (Locatable)portAttribute;
                } else {
                    try {
                        portLocation = new Location(_port, "_location");
                    } catch (KernelException ex) {
                        throw new InternalErrorException(ex);
                    }
                }
                double[] locationValues = location.getLocation();
                double[] portLocationValues = new double[2];
                portLocationValues[0] = locationValues[0] - 20.0;
                portLocationValues[1] = locationValues[1] - 5.0;
                portLocation.setLocation(portLocationValues);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    /** Return the associated port.  Normally, there always is one,
     *  but if setContainer() is called to change the container, then
     *  this might return null. Also, during cloning, there is a
     *  transient during which this may return null.
     *  @return The associated port.
     */
    public ParameterPort getPort() {
        return _port;
    }

    /** Clone the parameter. This overrides the base class to remove
     *  the current association with a port.  It is assumed that the
     *  port will also be cloned, and when the containers are set of
     *  this parameter and that port, whichever one is set second
     *  will result in re-establishment of the association.
     *  @param workspace The workspace in which to place the cloned parameter.
     *  @exception CloneNotSupportedException Not thrown in this base class.
     *  @see java.lang.Object#clone()
     *  @return The cloned parameter.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        PortParameter newObject = (PortParameter)super.clone(workspace);
        // Cannot establish an association with the cloned port until
        // that port is cloned and the container of both is set.
        newObject._port = null;
        return newObject;
    }

    /** Set the container of this parameter. If the container is different
     *  from what it was before and there is an associated port, then
     *  break the association.  If the new container has a port with the
     *  same name as this parameter, then establish a new association.
     *  That port must be an instance of ParameterPort, or no association
     *  is created.
     *  @see ParameterPort
     *  @param entity The new container.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public void setContainer(Entity entity)
            throws IllegalActionException, NameDuplicationException {
        Entity previousContainer = (Entity)getContainer();
        super.setContainer(entity);
        // If there is an associated port, and the container has changed,
        // break the association.
        if (_port != null && entity != previousContainer) {
            _port._parameter = null;
            _port = null;
        }

        // Look for a port in the new container with the same name,
        // and establish an association.
        if (entity instanceof TypedActor) {
            // Establish association with the port.
            Port port = entity.getPort(getName());
            if (port instanceof ParameterPort) {
                _port = (ParameterPort)port;
                if (_port._parameter == null) {
                    _port._parameter = this;
                    _port.setTypeSameAs(this);
                }
            }
            // NOTE: Do not create an instance of the port.
            // This is called when this object is cloned, and
            // the port will be cloned too. If we create a new
            // instance here, then we will get a name collision
            // as part of the cloning.
        }
    }

    /** Set the current value of this parameter and notify the container
     *  and value listeners. This does not change the persistent value
     *  (returned by getExpression()), but does change the current value
     *  (returned by getToken()).
     *  <p>
     *  If the type of this variable has been set with
     *  setTypeEquals(), then convert the specified token into that
     *  type, if possible, or throw an exception, if not.  If
     *  setTypeAtMost() has been called, then verify that its type
     *  constraint is satisfied, and if not, throw an exception.
     *  Note that you can call this with a null argument regardless
     *  of type constraints, unless there are other variables that
     *  depend on its value.
     *  @param token The new token to be stored in this variable.
     *  @exception IllegalActionException If the token type is not
     *   compatible with specified constraints, or if you are attempting
     *   to set to null a variable that has value dependents, or if the
     *   container rejects the change.
     */
    public void setCurrentValue(ptolemy.data.Token token)
            throws IllegalActionException {
        if (_debugging) {
            _debug("setCurrentValue: " + token);
        }
        _setTokenAndNotify(token);
        setUnknown(false);
    }

    /** Set or change the name, and propagate the name change to the
     *  associated port.  If a null argument is given, then the
     *  name is set to an empty string.
     *  Increment the version of the workspace.
     *  This method is write-synchronized on the workspace.
     *  @param name The new name.
     *  @exception IllegalActionException If the name contains a period.
     *  @exception NameDuplicationException If the container already
     *   contains an attribute with the proposed name.
     */
    public void setName(String name)
            throws IllegalActionException, NameDuplicationException {
        if (_settingName) return;
        super.setName(name);
        if (_port != null) {
            String oldName = getName();
            try {
                _settingName = true;
                _port._settingName = true;
                _port.setName(name);
            } catch (IllegalActionException ex) {
                super.setName(oldName);
                throw ex;
            } catch (NameDuplicationException ex) {
                super.setName(oldName);
                throw ex;
            } finally {
                _settingName = false;
                _port._settingName = false;
            }
        }
    }

    /** Check to see whether a token has arrived at the
     *  associated port, and if so, update the current value of
     *  parameter with that token.  If there is no associated port,
     *  do nothing.
     *  @exception IllegalActionException If reading from the associated
     *   port throws it.
     */
    public void update() throws IllegalActionException {
        ParameterPort port = _port;
        if (port != null
                && port.getWidth() > 0
                && port.hasToken(0)) {
            Token token = port.get(0);
            setCurrentValue(token);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Check that the specified container is of a suitable class for
     *  this parameter.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not an
     *   instance of Entity.
     */
    protected void _checkContainer(Entity container)
            throws IllegalActionException {
        if (!(container instanceof Entity)) {
            throw new IllegalActionException(this,
                    "PortParameter can only be used in an instance of Entity.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The associated port. */
    protected ParameterPort _port;

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // Indicator that we are in the midst of setting the name.
    private boolean _settingName = false;
}
