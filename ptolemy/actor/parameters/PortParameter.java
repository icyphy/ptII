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
import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// PortParameter
/**
A parameter that creates an associated port that can be used to update
the parameter value.  This parameter value can be set directly by the
user, as with any other parameter, or it can be set by feeding data
into the associated port.  Until the port receives data, the value
of the parameter is that set by the user.  After the port has received
data, then its value is that most recently received on the port.
The value must be obtained by calling getToken().  The
getExpression() method always returns the user-set expression,
regardless of whether an override value has been received on the
input port.  On each call to getToken(), this actor first checks
to see whether an input has arrived at the associated port
since the last setExpression(), and if so, returns a token
read from that port.  Also, any call to get() on the associated
port will result in the value of this parameter being updated.
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
            _port = new ParameterPort((ComponentEntity)container, name, this);
            _port.setTypeSameAs(this);
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
        // Use setExpression() since this is an initial value,
        // not a value read from a port.
        setExpression(token.toString());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the parameter. This overrides the base class to create
     *  a new associated port.
     *  @param workspace The workspace in which to place the cloned variable.
     *  @exception CloneNotSupportedException Not thrown in this base class.
     *  @see java.lang.Object#clone()
     *  @return The cloned variable.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        PortParameter newObject = (PortParameter)super.clone(workspace);
        // Do not create an associated port until there is a meaningful
        // container.
        newObject._port = null;
        return newObject;
    }

    /** Get a token with the current value of this parameter.
     *  First, check to see whether an input has arrived at the
     *  associated port since the last call to setExpression().
     *  If one has, then return that token.  Otherwise, return the
     *  result of evaluating the expression.
     *  @return The token contained by this variable converted to the
     *   type of this variable, or null if there is none.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    public ptolemy.data.Token getToken() throws IllegalActionException {
        if (_port != null && _port.getWidth() > 0 && _port.hasToken(0)) {
            Token newToken = _port.get(0);
        }
        return super.getToken();
    }

    /** Set the container of this parameter and its associated port.
     *  However, the clone() method of the associated PortParameter
     *  will create a new ParameterPort, so the clone of the ParameterPort
     *  should be removed.  Setting the container to null here
     *  accomplishes that.
     *  @param entity The container.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public void setContainer(Entity entity)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(entity);
        // If there is an associated port, then change its container too.
        // Otherwise, create an associated port.
        if (_port != null) {
            _port._setContainer(entity);
        } else if (entity instanceof TypedActor) {
            // If we get to here, the cast is safe.
            _port = new ParameterPort(
                    (ComponentEntity)entity, getName(), this);
            _port.setTypeSameAs(this);
        }
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

    /** Put a new token in this variable and notify the container and
     *  and value listeners, but override the base class so as not to
     *  erase the current expression.
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
    public void setToken(ptolemy.data.Token token)
            throws IllegalActionException {
        _setTokenAndNotify(token);
        setUnknown(false);
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

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The associated port.
    private ParameterPort _port;

    // Indicator that we are in the midst of setting the name.
    private boolean _settingName = false;
}
