/* A variable that can share state across multiple instances and
 * is used in the sequence domain.

 Copyright (c) 2009-2014 The Regents of the University of California.
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

package ptolemy.domains.sequence.lib;

import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// SequencedVariable

/** A variable that can share state across multiple instances and
 *  is used in the sequence domain.
 *
 *  @author Elizabeth Latronico
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (beth)
 *  @Pt.AcceptedRating Red (beth)
 */
public class SequencedVariable extends SequencedSharedMemoryActor {

    /** Create a new SequencedVariable actor with the given name and
     *  container.
     *
     * @param container The container that will contain the actor.
     * @param name The name of the actor.
     * @exception NameDuplicationException Thrown if there is already an actor
     *  in the container with the same name.
     * @exception IllegalActionException Thrown if the actor cannot be
     *  instantiated.
     */
    public SequencedVariable(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Messages have global scope.  (This is the default, but wanted to make this point explicit.)
        _scope = Scope.GLOBAL;

        // A parameter used by the Message icon to be able to
        // The icon cannot use the variable expression directly because
        // the variable is declared as a StringAttribute which icons do not support
        // Ideally the icon functionality should be expanded and this can be removed
        copyVariableName = new Parameter(this, "Copy_of_VariableName");
        copyVariableName.setVisibility(Settable.NONE);

        // Set the icon only if the parameter has a value.  (An exception is thrown for parameters
        // without values.)  Otherwise, display "UNDEFINED"
        setIconValue();

        // The initial value for the output.  Must be named output_InitialValue.
        // Set in preinitialize.
        outputInitialValue = new Parameter(this, "output_InitialValue");
        outputInitialValue.setVisibility(Settable.NOT_EDITABLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Inherits the modified variable name and initial variable name.
     *
     *  An additional parameter is used for the icon ( FIXME remove later!)
     */

    /** A parameter to hold the output initial value.  This parameter just copies
     *  the value in the initial value parameter.  It is needed because its name depends
     *  on the output port.
     *  FIXME:  In the future, upgrade to an interface with a function to do this.
     */
    public Parameter outputInitialValue;

    /** Delete this later when icon is figured out!! **/
    public Parameter copyVariableName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire the actor.  If the input is not connected, do not try to read a
     *  value from it.  It is OK not to read any value, since messages always
     *  have an initial value.
     *
     *  @exception IllegalActionException If the actor cannot be fired.
     */
    @Override
    public void fire() throws IllegalActionException {
        if (!input.connectedPortList().isEmpty()) {
            // If input is connected, use the function from
            // the superclass to read a value from the input,
            // store it to the variable, and send to the output
            // (if the output is connected)
            super.fire();
        }

        else {
            // Otherwise, just send the stored value
            // to the output.
            // Beth 09/14/09 Changed this to !output.isOutsideConnected() to be consistent
            // with superclass
            // if (output.getWidth() > 0) {
            if (output.isOutsideConnected()) {
                output.send(0, getVariable().getToken());
            }
        }
    }

    /** In addition to the superclass preinitialize(), set the actor icon and
     *  its outputInitialValue.
     *
     *  @exception IllegalActionException If the actor cannot be preinitialized.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        // Set the expression in the copyVariableName for display in the icon
        setIconValue();

        Variable initialVar = getInitialVariable();
        if (initialVar != null && initialVar.getToken() != null) {
            // Set the token equal to the initial variable's token
            outputInitialValue.setToken(initialVar.getToken());
            outputInitialValue.validate();

        } else {
            throw new IllegalActionException(
                    this,
                    "Message actor "
                            + getName()
                            + " does not have a proper initial value.  Please set the initial value parameter.");
        }
    }

    /** When the actor name is changed, update the referenced variables
     *  to point to the new parameters and check to see if these parameters
     *  exist.  This is also called when the actor is created, BEFORE the variables have
     *  been created in the constructor.  So, we need to check for null variables here.
     *
     *  @param name The new name for the actor.
     *  @exception IllegalActionException Thrown if there is a problem setting
     *   the actor name.
     *  @exception NameDuplicationException Thrown if the new name conflicts with
     *   the name of an existing actor in the container.
     */
    @Override
    public void setName(String name) throws IllegalActionException,
    NameDuplicationException {
        super.setName(name);

        // Set the expression in the copyVariableName for display in the icon
        if (copyVariableName != null) {
            setIconValue();
        }

        // Set output initial value so that user can see it
        Variable initialVar = getInitialVariable();
        if (initialVar != null && initialVar.getToken() != null) {
            // Set the token equal to the initial variable's token
            outputInitialValue.setToken(initialVar.getToken());
            outputInitialValue.validate();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Supplies a default value for the variable, in the case that there
     *  is no initial value.
     *
     *  @return  A token containing the default value
     *  @exception IllegalActionException  Subclasses should throw an exception if
     *   an explicit initial value is required.
     */
    @Override
    protected Token _getDefaultValue() throws IllegalActionException {
        // Check the type constraints on the variable.  Default to 0 for numeric
        // and true for boolean.  Otherwise, throw an exception.  Messages
        // may not hold strings or non-scalar types.
        // As long as the type on any input from any sharing message is set, it's fine
        // since there is a type constraint between the input and the variable
        Variable var = getVariable();

        if (var != null && var.getToken() != null) {
            if (var.getType().equals(BaseType.INT)) {
                return new IntToken(0);
            } else if (var.getType().equals(BaseType.DOUBLE)) {
                return new DoubleToken(0.0);
            } else if (var.getType().equals(BaseType.BOOLEAN)) {
                return new BooleanToken("true");
            } else {
                throw new IllegalActionException(
                        this,
                        "The default value for actor "
                                + getName()
                                + " cannot be determined because the type on the input port is unknown or unsupported.  Please use an integer, double, or boolean.");
            }
        } else {
            throw new IllegalActionException(
                    this,
                    "Actor "
                            + getName()
                            + " does not have a variable to store its state and/or initial value in.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Determine the text to display in the icon (copyVariableName).
     *  If the variable is present and defined, return the variableName
     *  expression.  This will display the value of the variable in the icon.
     *  Otherwise, display the string "UNDEFINED".
     *
     *  @exception IllegalActionException Thrown if the icon cannot be set.
     */
    private void setIconValue() throws IllegalActionException {
        // Default to "UNDEFINED"
        copyVariableName.setExpression("\"UNDEFINED\"");

        if (variableName.getExpression() != null
                && !variableName.getExpression().equals("")) {
            try {
                Variable var = getVariable();
                if (var != null && var.getToken() != null) {
                    copyVariableName
                    .setExpression(variableName.getExpression());
                }
            }
            // OK if this throws an exception.  Want default of UNDEFINED which was already set.
            catch (IllegalActionException e) {
            }
        }

        // Propagate any changes.  Catch any exception since this code only deals with the icon display
        // and not the functionality.  If it doesn't work
        copyVariableName.validate();
    }
}
