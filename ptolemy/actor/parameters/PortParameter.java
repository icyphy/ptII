/* A parameter that has an associated port.

 Copyright (c) 2002-2014 The Regents of the University of California.
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

import ptolemy.actor.Initializable;
import ptolemy.actor.TypedActor;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.AbstractInitializableParameter;
import ptolemy.data.type.Type;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// PortParameter

/**
 <p>This parameter creates an associated port that can be used to update
 the current value of the parameter. This parameter has two values,
 which may not be equal, a <i>current value</i> and a <i>persistent value</i>.
 The persistent value is returned by
 getExpression() and is set by any of three different mechanisms:</p>
 <ul>
 <li> calling setExpression();</li>
 <li> calling setToken(); and </li>
 <li> specifying a value as a constructor argument.</li>
 </ul>
 <p>
 All three of these will also set the current value, which is then
 equal to the persistent value.
 The current value is returned by get getToken()
 and is set by any of two different mechanisms:</p>
 <ul>
 <li> calling setCurrentValue();</li>
 <li> calling update() sets the current value if there is an associated
 port, and that port has a token to consume; and</li>
 </ul>
 These two techniques do not change the persistent value, so after
 these are used, the persistent value and current value may be different.
 <p>
 When the container for this parameter is initialized, the current
 value of the parameter is reset to match the persistent value.
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
 mechanism, see Ramp.</p>
 <p>
 If this actor is placed in a container that does not implement
 the TypedActor interface, then no associated port is created,
 and it functions as an ordinary parameter.  This is useful,
 for example, if this is put in a library, where one would not
 want the associated port to appear.</p>

 <p>There are a few situations where PortParameter might not do what
 you expect:</p>

 <ol>
 <li> If it is used in a transparent composite actor, then a token provided
 to a PortParameter will never be read.  A transparent composite actor
 is one without a director.
 <br>Workaround: Put a director in the composite.<br>
 </li>

 <li> Certain actors (such as the Integrator in CT) read parameter
 values only during initialization.  During initialization, a
 PortParameter can only have a value set via the parameter (it
 can't have yet received a token).  So if the initial value of the
 Integrator is set to the value of the PortParameter, then it will
 see only the parameter value, never the value provided via the
 port.
 <br>Workaround: Use a RunCompositeActor to contain the model with the
 Integrator.
 </li>

 </ol>

 @see ptolemy.actor.lib.Ramp
 @see ParameterPort
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Yellow (neuendor)
 */
public class PortParameter extends AbstractInitializableParameter implements
Initializable {
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
        // If we get to here, we know the container is a ComponentEntity,
        // so the cast is safe.
        if (container instanceof TypedActor) {
            _port = new ParameterPort((ComponentEntity) container, name);
        }
    }

    /** Construct a parameter with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will create
     *  an associated port in the same container.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @param initializeParameterPort True if the parameterPort should
     *   be initialized here. Some derived classes might want to initialize
     *   the port themselves (e.g. MirrorPortParameter).
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public PortParameter(NamedObj container, String name,
            boolean initializeParameterPort) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        // If we get to here, we know the container is a ComponentEntity,
        // so the cast is safe.
        if (initializeParameterPort && container instanceof TypedActor) {
            _port = new ParameterPort((ComponentEntity) container, name);
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
    public PortParameter(NamedObj container, String name,
            ptolemy.data.Token token) throws IllegalActionException,
            NameDuplicationException {
        this(container, name);
        setToken(token);
        if (token != null) {
            if (isStringMode() && token instanceof StringToken) {
                _persistentExpression = ((StringToken) token).stringValue();
            } else {
                _persistentExpression = token.toString();
            }
        } else {
            _persistentExpression = "";
        }
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
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute instanceof Locatable) {
            Locatable location = (Locatable) attribute;

            if (_port != null) {
                Attribute portAttribute = _port.getAttribute("_location");
                Locatable portLocation = null;

                if (portAttribute instanceof Locatable) {
                    portLocation = (Locatable) portAttribute;
                } else {
                    try {
                        portLocation = new Location(_port, "_location");
                        ((NamedObj) portLocation).propagateExistence();
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
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PortParameter newObject = (PortParameter) super.clone(workspace);
        // Cannot establish an association with the cloned port until
        // that port is cloned and the container of both is set.
        newObject._port = null;
        return newObject;
    }

    /** Get the persistent expression.
     *  @return The expression used by this variable.
     *  @see #setExpression(String)
     */
    @Override
    public String getExpression() {
        if (_persistentExpression == null) {
            return "";
        }
        return _persistentExpression;
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

    /** Reset the current value to match the persistent value.
     *  @exception IllegalActionException If thrown by a subclass.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        super.setExpression(_persistentExpression);
        validate();
    }

    /** Reset the current value to match the persistent value.
     *  @exception IllegalActionException If thrown by a subclass.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        super.setExpression(_persistentExpression);
        validate();
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
     *  depend on its value.</p>
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

        super.setToken(token);
        setUnknown(false);
    }

    /** Set the display name, and propagate the name change to the
     *  associated port.
     *  Increment the version of the workspace.
     *  This method is write-synchronized on the workspace.
     *  @param name The new display name.
     */
    @Override
    public void setDisplayName(String name) {
        if (_settingName) {
            return;
        }
        super.setDisplayName(name);
        if (_port != null) {
            try {
                _settingName = true;
                _port._settingName = true;
                _port.setDisplayName(name);
            } finally {
                _settingName = false;
                _port._settingName = false;
            }
        }
    }

    /** Override the base class to record the persistent expression.
     *  @param expression The expression for this variable.
     *  @see #getExpression()
     */
    @Override
    public void setExpression(String expression) {
        _persistentExpression = expression;
        super.setExpression(expression);
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
    @Override
    public void setName(String name) throws IllegalActionException,
    NameDuplicationException {
        if (_settingName) {
            return;
        }

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

    /** Override the base class to record the persistent expression
     *  to be the string representation of the specified token.
     *  @param newValue The new persistent value.
     *  @exception IllegalActionException If the token type is not
     *   compatible with specified constraints, or if you are attempting
     *   to set to null a variable that has value dependents, or if the
     *   container rejects the change.
     */
    @Override
    public void setToken(Token newValue) throws IllegalActionException {
        if (newValue != null) {
            if (isStringMode() && newValue instanceof StringToken) {
                _persistentExpression = ((StringToken) newValue).stringValue();
            } else {
                _persistentExpression = newValue.toString();
            }
        } else {
            _persistentExpression = "";
        }
        super.setToken(newValue);
    }

    /** Declare the type of this parameter to by equal the specified value.
     *  In addition, if a port is associated, declare its type to be equal
     *  to this value.
     *  To undo, call this method with the argument BaseType.UNKNOWN.
     *  @see ParameterPort
     *  @param type A Type.
     *  @exception IllegalActionException If the currently contained
     *   token cannot be converted losslessly to the specified type.
     */
    @Override
    public void setTypeEquals(Type type) throws IllegalActionException {
        super.setTypeEquals(type);
        if (_port != null) {
            _port.setTypeEquals(type);
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

        if (port != null && port.isOutsideConnected() && port.hasToken(0)) {
            Token token = port.get(0);
            setCurrentValue(token);
            // Have to validate so that containers of dependent
            // variables get attributeChanged() called.
            validate();

            if (_debugging) {
                _debug("Updated parameter value to: " + token);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to also propagate the associated port.
     *  @param container Object to contain the new object.
     *  @exception IllegalActionException If the object
     *   cannot be cloned.
     *  @return A new object of the same class and name
     *   as this one.
     */
    @Override
    protected NamedObj _propagateExistence(NamedObj container)
            throws IllegalActionException {
        NamedObj result = super._propagateExistence(container);

        // Since we have created an associated port in the
        // constructor, and since that port is not contained by
        // this parameter, it will not automatically be propagated.
        // If this parameter is contained by class definition
        // somewhere above in the hierarchy, then not propagating
        // the associated port is an error.
        if (_port != null) {
            _port.propagateExistence();
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The associated port. */
    protected ParameterPort _port;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The persistent expression. */
    private String _persistentExpression;

    /** Indicator that we are in the midst of setting the name. */
    private boolean _settingName = false;
}
