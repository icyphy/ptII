/* A port that updates a parameter of the container.

 Copyright (c) 2001-2003 The Regents of the University of California.
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

import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ParameterPort
/**
A specialized port for use with PortParameter.  This port is created
by an instance of PortParameter and provides values to a parameter.
The parameter's current value is updated whenever get() is
called on this port.  This port is only useful if the container is
opaque, however, this is not checked.

@see PortParameter
@author  Edward A. Lee
@version $Id$
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
        super(container, name);
        setInput(true);
        setMultiport(false);
        // Notify SDF scheduler that this port consumes one token,
        // despite not being connected on the inside.
        Parameter tokenConsumptionRate =
            new Parameter(this, "tokenConsumptionRate", new IntToken(1));
        tokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        tokenConsumptionRate.setPersistent(false);
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
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        ParameterPort newObject = (ParameterPort)super.clone(workspace);
        // Cannot establish an association with the cloned parameter until
        // that parameter is cloned and the container of both is set.
        newObject._parameter = null;
        return newObject;
    }

    /** Get a token from the specified channel as done by the superclass,
     *  but override the superclass to set the current value of
     *  of the associated parameter, if there is one.
     *  @param channelIndex The channel index.
     *  @return A token from the specified channel.
     *  @exception NoTokenException If there is no token.
     *  @exception IllegalActionException If there is no director, and hence
     *   no receivers have been created, if the port is not an input port, or
     *   if the channel index is out of range.
     */
    public Token get(int channelIndex)
            throws NoTokenException, IllegalActionException {
        Token token = super.get(channelIndex);
        if (_parameter != null) {
            _parameter.setCurrentValue(token);
        }
        return token;
    }

    /** Get an array of tokens from the specified channel
     *  as done by the superclass, but override the superclass to set the
     *  current value of the corresponding parameter to be the last token
     *  in the array.
     *  @param channelIndex The channel index.
     *  @param vectorLength The number of valid tokens to get in the
     *   returned array.
     *  @return A token array from the specified channel containing
     *   <i>vectorLength</i> valid tokens.
     *  @exception NoTokenException If there is no array of tokens.
     *  @exception IllegalActionException If there is no director, and hence
     *   no receivers have been created, if the port is not an input port, or
     *   if the channel index is out of range.
     */
    public Token[] get(int channelIndex, int vectorLength)
            throws NoTokenException, IllegalActionException {
        Token[] retArray = super.get(channelIndex, vectorLength);
        if (_parameter != null) {
            _parameter.setToken(retArray[vectorLength-1]);
        }
        return retArray;
    }

    /** Set the container of this port. If the container is different
     *  from what it was before and there is an associated parameter, then
     *  break the association.  If the new container has a parameter with the
     *  same name as this port, then establish a new association.
     *  That parameter must be an instance of PortParameter, or no association
     *  is created.
     *  @see PortParameter
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
        if (_parameter != null && entity != previousContainer) {
            _parameter._port = null;
            _parameter = null;
        }

        // Look for a parameter in the new container with the same name,
        // and establish an association.
        if (entity instanceof TypedActor) {
            // Establish association with the parameter.
            Attribute parameter = entity.getAttribute(getName());
            if (parameter instanceof PortParameter) {
                _parameter = (PortParameter)parameter;
                if (_parameter._port == null) {
                    _parameter._port = this;
                    _setTypeConstraints();
                }
            }
        }
    }
    
    /** Get the associated parameter.
     *  @return The associated parameter.
     */
    public PortParameter getParameter() {
        return _parameter;
    }

    /** Set the name, and propagate the name change to the
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
        if (_settingName || _parameter == null) {
            super.setName(name);
        } else {
            try {
                _settingName = true;
                _parameter.setName(name);
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
        _parameter.setTypeSameAs(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** Indicator that we are in the midst of setting the name. */
    protected boolean _settingName = false;

    /** The associated parameter. */
    protected PortParameter _parameter;
}
