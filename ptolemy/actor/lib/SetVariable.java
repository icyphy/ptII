/* Set the value of a variable contained by the container.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.ExplicitChangeContext;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.MessageHandler;

import java.util.List;
import java.util.ArrayList;

//////////////////////////////////////////////////////////////////////////
//// SetVariable
/**
Set the value of a variable contained by the container.
The change to the value of the variable is implemented in a
change request, and consequently will not take hold until the
end of the current iteration.  This helps ensure that users
of value of the variable will see changes to the value
deterministically (independent of the schedule of execution
of the actors).
<p>
Note that the variable name is observed during preinitialize().
If it is changed after that, the change will not take effect
until the next time the model is executed.
<p>
The variable can be either any attribute that implements
the Settable interface. If it is in addition an instance of
Variable, then the input token is used directly to set the
value, and the type of the variable is constrained to be
the same as the type of the input. Otherwise, then input
token is converted to a string and the setExpression() method
on the variable is used to set the value.

@author Edward A. Lee, Steve Neuendorffer
@version $Id$
*/

public class SetVariable extends TypedAtomicActor implements ChangeListener, ExplicitChangeContext {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SetVariable(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);

        variableName = new StringAttribute(this, "variableName");
        variableName.setExpression("parameter");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port. */
    public TypedIOPort input;

    /** The name of the variable in the container to set. */
    public StringAttribute variableName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do nothing.
     *  @param change The change that executed. 
     */
    public void changeExecuted(ChangeRequest change) {
    }

    /** React to the fact that a change failed by setting a flag
     *  that causes an exception to be thrown in next call to prefire()
     *  or wrapup().
     *  @param change The change request.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(
            ChangeRequest change,
            java.lang.Exception exception) {
        MessageHandler.error("Failed to set variable.", exception);
    }
    
    /** 
     * Return the change context being made explicit.  In this case,
     * the change context returned is this actor.
     */
    public Entity getContext() {
        return this;
    }

    /** Return the variable modified by this actor.  This method requires 
     *  write access on the workspace.
     *  @exception IllegalActionException If the variable cannot be found.
     */
    public Attribute getModifiedVariable() throws IllegalActionException {
        NamedObj container = (NamedObj) getContainer();
        if (container == null) {
            throw new IllegalActionException(this, "No container.");
        }
        String variableNameValue = variableName.getExpression();
        Variable variable =
            (Variable) container.getAttribute(
                    variableNameValue);
        if (variable == null) {
            try {
                variable = new Variable(this, variableNameValue);
            } catch (NameDuplicationException ex) {
                throw new IllegalActionException(
                    this, ex,
                    "Existing attribute that is not a Variable " + 
                    "with specified name: "
                    + variableNameValue
                    + ". It is: "
                    + container.getAttribute(variableNameValue));
            }
        }   
        return variable;
    }

    /** Return a list of variables that this entity modifies.  The
     * variables are assumed to have a change context of the given
     * entity.
     * @return A list of variables.
     * @exception IllegalActionException If the list of modified
     * variables cannot be returned.
     */
    public List getModifiedVariables() throws IllegalActionException {
        Attribute attribute = getModifiedVariable();
        List list = new ArrayList(1);
        if(attribute instanceof Variable) {
            list.add(attribute);
        }
        return list;
    }

    /** Read at most one token from the input port and issue a change
     *  request to update variables as indicated by the input.
     *  @exception IllegalActionException If thrown reading the input.
     */
    public boolean postfire() throws IllegalActionException {
        if (input.hasToken(0)) {
            final Token value = input.get(0);
            ChangeRequest request =
                new ChangeRequest(this, "SetVariable change request") {
                    protected void _execute() throws IllegalActionException {
                        Attribute variable = getModifiedVariable();
                        if (variable instanceof Variable) {
                            ((Variable)variable).setToken(value);
                            // NOTE: If we don't call validate(), then the
                            // change will not propagate to dependents.
                            ((Variable)variable).validate();
                        } else if (variable instanceof Settable) {
                            ((Settable)variable).setExpression(value.toString());
                            // NOTE: If we don't call validate(), then the
                            // change will not propagate to dependents.
                            ((Settable)variable).validate();
                        } else {
                            throw new IllegalActionException(SetVariable.this,
                                    "Cannot set the value of the variable named: "
                                    + variableName.getExpression());
                        }
                    }
                };
            
            // To prevent prompting for saving the model, mark this
            // change as non-persistent.
            request.setPersistent(false);
            request.addChangeListener(this);
            requestChange(request);
        }
        return true;
    }

    /** If there is no variable with the specified name, then create one.
     *  This is done in preinitialize() so that we can set up a type
     *  constraint that ensures that the variable and the input port
     *  have the same type.
     *  @exception IllegalActionException If the superclass throws it,
     *   or if there is no container.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        Attribute attribute = getModifiedVariable();
        if (attribute instanceof Variable) {
            ((Variable)attribute).setTypeSameAs(input);
        }
    }
}
