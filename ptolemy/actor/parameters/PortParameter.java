/* A parameter that has an associated port.

 Copyright (c) 1997-2002 The Regents of the University of California.
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
@AcceptedRating Red (bart@eecs.berkeley.edu)

*/

package ptolemy.actor.parameters;

import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.Parameter;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// PortParameter
/**
A parameter that creates an associated port that can be used to update
the current value of the parameter.  The "current value" is set by the
setCurrentValue() method and read by getToken().  The current value
is distinct from the persistent value, which is set by setExpression()
or setToken(), and accessed by getExpression().  Note that getToken()
returns the current value, which is not necessarily the persistent value.
The current value can be set in the usual way for a parameter (calling
setToken() or setExpression()), or by feeding data into the associated port.
Until the port receives data, the current value of the parameter is
the same as the persistent value.  After the port has received
data, then the current value is that most recently received on the port
since the last update of the persistent value.
The getExpression() methods always return the persistent
value, regardless of whether an override value has been received on the
input port.  On each call to getToken(), this parameter first checks
to see whether an input has arrived at the associated port
since the last setExpression() or setToken(), and if so, returns a token
read from that port.  Also, any call to get() on the associated
port will result in the value of this parameter being updated.
<p>
When using this parameter in an actor, care must be exercised
to read its value exactly once per firing by calling getToken().
Each time getToken() is called, a new token will be consumed from
the associated port (if there is a token).  If this is called
multiple times in an iteration, it may result in
consuming tokens that were intended for subsequent firings.
Thus, for example, it should not be called in fire() and then
again in postfire().  Moreover, in some domains (such as DE),
it is essential that if a token is provided on a port, that it
is consumed.  In DE, the actor will be repeatedly fired until
the token is consumed.  Thus, it is an error to not call getToken()
once per iteration.
<p>
If this is actor is placed in a container that does not implement
the TypedActor interface, then no associated port is created,
and it functions as an ordinary parameter.  This is useful,
for example, if this is put in a library, where one would not
want the associated port to appear.

@author Edward A. Lee
@version $Id$
@see AttributePort
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
            // NOTE: The following two statements are not necessary, since
            // the port will discover this parameter when it's setContainer()
            // method is called.  Moreover, doing this again is not a good
            // idea, since the setTypeSameAs() method will just add an
            // extra (redundant) set of constraints.  This will cause
            // the clone tests to fail, since cloning does not produce
            // the extra set of constraints.
            // _port._parameter = this;
            // _port.setTypeSameAs(this);
        }
    }

    /** Construct a Parameter with the given container, name, and Token.
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

    /** Get a token with the current value of this parameter.
     *  First, check to see whether an input has arrived at the
     *  associated port since the last call to setExpression().
     *  If one has, then return that token.  Otherwise, return the
     *  value returned by the superclass getToken() method.
     *  <p>
     *  NOTE: It was tempting in the design of this class to provide
     *  a separate getCurrentValue() method, and to have getToken()
     *  always return the persistent value.  However, this would mean
     *  that this class would not function as a drop-in replacement
     *  for a Parameter. In particular, if some other parameter were
     *  to reference this one in an expression, it would not see the
     *  current value. It would see the persisent value.
     *  @return The token contained by this variable converted to the
     *   type of this variable, or null if there is none.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one, or if reading
     *   data from the associated port throws it.
     */
    public ptolemy.data.Token getToken() throws IllegalActionException {
        try {
            // Avoid reading more than once within a call to getToken().
            if (!_alreadyReadPort) {
                _alreadyReadPort = true;
                if (_port != null
                        && _port.getWidth() > 0
                        && _port.hasToken(0)) {
                    _port.get(0);
                }
            }
            return super.getToken();
        } finally {
            _alreadyReadPort = false;
        }
    }

    /** Set the container of this parameter and its associated port.
     *  If there is no associated port (e.g. this parameter was cloned),
     *  then check the container for a port with the same name and
     *  establish an association.  If no port is found, then leave
     *  this parameter with no associated port.
     *  @param entity The container.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public void setContainer(Entity entity)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(entity);
        // If there is an associated port, then change its container too.
        // Otherwise, look for a port with the same name, and establish
        // an association if there is one.
        if (_port != null) {
            _port._setContainer(entity);
        } else if (entity instanceof TypedActor) {
            // Establish association with port.
            Port port = entity.getPort(getName());
            if (port instanceof ParameterPort) {
                _port = (ParameterPort)port;
                if (_port._parameter == null) {
                    _port._parameter = this;
                    _port.setTypeSameAs(this);
                }
            }
        }
    }

    /** Set the current value of this parameter and notify the container and
     *  and value listeners.  Force evaluation of the value listeners.
     *  This does not erase the current expression,
     *  but does update the value that will be returned by getToken().
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
        try {
            // Need to force evaluation of all dependents now.
            // Otherwise, they will trigger another read of the associated
            // port when they get around to being evaluated.
            _forceEvaluationOfListeners = true;
            _setTokenAndNotify(token);
            setUnknown(false);
        } finally {
            _forceEvaluationOfListeners = false;
        }
    }

    /** Set the expression of this variable, and override the base class
     *  to read and discard all pending inputs at the associated port.
     *  Otherwise, the behavior is exactly like that of the base class.
     *  @param expr The expression for this variable.
     */
    public void setExpression(String expr) {
        // Clear all pending inputs.
        try {
            while (_port != null && _port.getWidth() > 0 && _port.hasToken(0)) {
                _port.get(0);
            }
        } catch (IllegalActionException ex) {
            // Ignore, since this will only occur if the port is not
            // operational yet.
        }
        super.setExpression(expr);
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

    /** Put a new token in this variable, notify the container and
     *  and value listeners, and override the base class
     *  to read and discard all pending inputs at the associated port.
     *  Otherwise, the behavior is exactly like that of the base class.
     *  @param token The new token to be stored in this variable.
     *  @exception IllegalActionException If the token type is not
     *   compatible with specified constraints, or if you are attempting
     *   to set to null a variable that has value dependents, or if the
     *   container rejects the change.
     */
    public void setToken(ptolemy.data.Token token)
            throws IllegalActionException {
        if (!_alreadyReadPort) {
            // Clear all pending inputs.
            try {
                while (_port != null
                        && _port.getWidth() > 0
                        && _port.hasToken(0)) {
                    _port.get(0);
                }
            } catch (IllegalActionException ex) {
                // Ignore, since this will only occur if the port is not
                // operational yet.
            }
        }
        super.setToken(token);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Check that the specified container is of a suitable class for
     *  this port.  In this base class, this method returns immediately
     *  without doing anything.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not of
     *   an acceptable class.  Not thrown in this base class.
     */
    protected void _checkContainer(Entity container)
            throws IllegalActionException {
        if (!(container instanceof ComponentEntity)) {
            throw new IllegalActionException(this,
            "Container is required to be an instance of ComponentEntity");
        }
    }

    /** Override the base class so that if this called as a result of
     *  of a call to setCurrentValue(), then the value listeners are
     *  all forced to be evaluated.  Otherwise, it behaves just like
     *  the base class.
     */
    protected void _notifyValueListeners() {
        if (!_forceEvaluationOfListeners) {
            super._notifyValueListeners();
        } else {
            if (_valueListeners != null) {
                Iterator listeners = _valueListeners.iterator();
                while (listeners.hasNext()) {
                    ValueListener listener = (ValueListener)listeners.next();
                    listener.valueChanged(this);
                    if (listener instanceof Variable) {
                        try {
                            ((Variable)listener).getToken();
                            // FIXME: Do we have to notify the listeners
                            // of the listeners?
                        } catch (IllegalActionException ex) {
                            throw new InternalErrorException(ex);
                        }
                    }
                }
            }
        }
    }

    /** Set the container.  This should only be called by the associated
     *  parameter.
     *  @param entity The container.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    protected void _setContainer(Entity entity)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(entity);
    }



    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The associated port. */
    protected ParameterPort _port;

    /** Flag used to prevent multiple readings of input within one
     *  call to getToken(), or when the port sets the current value
     *  and value listeners are notified.  This variable is accessed
     *  by the associated port.
     */
    protected boolean _alreadyReadPort = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // Indicator that we are in the midst of setting the name.
    private boolean _settingName = false;

    // Indicator to force evaluation of value listeners.
    private boolean _forceEvaluationOfListeners = false;
}
