/* A base class for shared memory actors in the sequence domain.

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

import ptolemy.actor.CompositeActor;
import ptolemy.actor.lib.SetVariable;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// SequencedSharedMemoryActor

/** A base class for shared memory actors in the sequence domain.
 *  This class should not itself be instantiated, so it is an abstract class.
 *
 *  This class builds on SetVariable in the following ways:
 *   - Supports an additional parameter to store the initial value
 *   - Calculates the name of the variable, and the initial value
 *  parameter, according to the name of the actor
 *   - Additional features for creating the variables in the proper scope
 *
 *  @author Elizabeth Latronico
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (beth)
 *  @Pt.AcceptedRating Red (beth)
 */
public abstract class SequencedSharedMemoryActor extends SetVariable {

    /** Create a new SequencedSharedMemoryActor with the given name and
     *  container.
     * @param container The container for the new actor.
     * @param name The name of the new actor.
     * @exception NameDuplicationException Thrown if the chosen name matches the
     *  name of an already existing actor in the model.
     * @exception IllegalActionException Thrown if there is a problem instantiating
     *  the actor.
     */
    public SequencedSharedMemoryActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Set delayed to false (changes take place immediately)
        // Set delayed to not editable
        delayed.setExpression("false");
        delayed.setVisibility(Settable.NONE);

        // The variable name is initialized to the same as the actor name
        // without the _graphicOID number (for importing from ASCET)
        // Note that currently if the user changes the name of the actor,
        // the name of the parameter is not updated - this must be done
        // manually.
        // Calculate and store this shared name

        // Beth added 08/24/09
        // Fixed this to ensure that the underscore must be followed by all numbers
        // Otherwise, this part of the name should not be removed
        // lastIndexOf returns -1 if not found

        int underscore = name.lastIndexOf('_');

        if (underscore > 0 && name.substring(underscore).matches("_\\d+")) {
            _sharedName = name.substring(0, underscore);
        }

        else {
            _sharedName = name;
        }

        // Beth added 10/24/08
        // Append "_parameter" to the name
        // This is consistent with system constants
        // The expression stored here is not editable by the user
        // A parameter for this is created in the superclass constructor
        // variableName is a StringAttribute

        variableName.setExpression(_sharedName + "_parameter");
        variableName.setVisibility(Settable.NOT_EDITABLE);

        // Shared memory actors also have a shared initial value parameter
        // This is used by the Sequence and Process directors
        // The expression stored here is not editable by the user

        initialVariableName = new StringAttribute(this, "_initialValue");
        initialVariableName.setExpression(_sharedName + "_initialValue");
        initialVariableName.setVisibility(Settable.NOT_EDITABLE);

        // Create parameters for these variables, if they do not already exist
        // Note:  A parameter might be declared in the Ptolemy moml file after the message is created
        // If so, the new value will overwrite the old value, so there is no problem.
        // Want to check in the constructor, so that they will be created if needed so that
        // the user can enter values for the variables.
        try {
            checkForAttributes();
        } catch (IllegalActionException e) {
        }
        ;

        // Beth added 01/21/09
        // Set up Ptolemy type constraints
        //((Variable) getModifiedVariable(variableName.getExpression())).setTypeAtLeast(input);
        //((Variable) getModifiedVariable(variableName.getExpression())).setTypeAtLeast( (Variable) getModifiedVariable(initialVariableName.getExpression()));

        // Type constraints are set in preinitialize()

        // Set up type constraint for the output port.  The type of the output port will be
        // the same as the type of token stored in the variable
        // output.setTypeAtLeast(((Variable) getModifiedVariable(variableName.getExpression())));

        // FIXME:  Implement this in the future.  Not used currently.
        // numInstancesParameter = new SharedParameter(this, _sharedName + "_numInstances", Message.class);
        // numInstancesParameter.setVisibility(Settable.NONE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The name of the initial value variable in the container to set. */
    public StringAttribute initialVariableName;

    /** An enumeration that represents the scope of the memory element.
     *  The are three options:  Local, Imported, and Exported.
     *  This information can be read from the ASCET model.
     *
     *  In Ptolemy, the scope is determined by the location in the
     *  hierarchy.
     *
     *  In Ptolemy, Imported and Exported will both treated as global
     *  scope - parameter will be created at the top level.
     *
     *  This enumeration also has a generic GLOBAL option, which can be
     *  used for actors created in Ptolemy where Imported/Exported have
     *  no meaning.
     */
    enum Scope {
        /** Local scope. */
        LOCAL,

        /** Global scope. */
        GLOBAL,

        /** Imported scope (treated as global). */
        IMPORTED,

        /** Exported scope (treated as global). */
        EXPORTED
    };

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check to see if the parameters exist in the workspace.
     *  Similar to SetVariable getAttribute().
     *  If not, create them, and assign them a null token.  This is needed for (for example)
     *  when the actor is dragged and dropped from the menu, since the parameters will be created
     *  but do not have any user defined values.
     *  Subclasses can override this method, or can use checkForVariables(Token)
     *
     *  FIXME Any way to get these to display to the user?
     *
     *  @exception IllegalActionException If there is a problem with the attributes.
     */
    public void checkForAttributes() throws IllegalActionException {
        checkForAttributes(null, null);
    }

    /** Check to see if both the variable and initial value variable exist.
     *  If not, create them, and set their tokens to the arguments.
     *
     * @param value1  The value to assign to the variable.
     * @param value2  The value to assign to the initial value variable.
     * @exception IllegalActionException If the attributes cannot be created.
     */
    public void checkForAttributes(Token value1, Token value2)
            throws IllegalActionException {

        // Look for variables.  If not present, create them.
        NamedObj container = getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "No container.");
        }

        // FIXME:  Check for null or invalid names here?  Throw exception?

        // Look for the variable holding the value
        // If a model is imported from ASCET2Ptolemy, this parameter will be created already,
        // and will hold the appropriate value
        _checkSingleAttribute(variableName.getExpression(), container, value1);

        // Look for the variable holding the initial value
        // Beth added 10/24/08 - Pass in value of the variable as a token
        // here, so that we set the initial value to the value of the parameter
        // if a new initial value variable was created
        // Note, this assumes that the variable exists - which it should since
        // the previous function call should have created it
        // (except if the name was null or invalid)
        _checkSingleAttribute(initialVariableName.getExpression(), container,
                value2);
    }

    /** Fire the SequencedSharedMemoryActor.
     *  If the input is not connected, do not try to read a value
     *  from it.
     *
     *  It is OK not to read any value, since SequencedSharedMemoryActors always have
     *  an initial value.
     *
     *  @exception IllegalActionException If the actor cannot be fired.
     */
    @Override
    public void fire() throws IllegalActionException {
        if (input.isOutsideConnected()) {
            // If input is connected, use the function from
            // the superclass to read a value from the input,
            // store it to the variable, and send to the output
            // (if the output is connected)
            super.fire();
        }

        else {
            // Otherwise, just send the stored value
            // to the output.  Attribute a should always be
            // a variable.
            // Beth 09/14/09 Changed this to !output.isOutsideConnected() to be consistent
            // with superclass
            // if (output.getWidth() > 0) {
            if (output.isOutsideConnected()) {
                output.send(0, getVariable().getToken());
            }
        }
    }

    /** Return the initial variable.  Create it if it's not present.
     *
     *  @return  The initial variable.  Returns null if the variable name is not assigned
     *   or is the empty string.
     *  @exception IllegalActionException If the variable cannot be found.
     */
    public Variable getInitialVariable() throws IllegalActionException {
        if (initialVariableName != null
                && !initialVariableName.getExpression().equals("")) {
            return getModifiedVariable(initialVariableName.getExpression());
        } else {
            return null;
        }
    }

    /** Returns the initial variable's name.
     *  @return The initial variable name.
     */
    public String getInitialVariableName() {
        return initialVariableName.getExpression();
    }

    /** From superclass. Overridden to return checkSingleVariable for the variable
     *  (i.e. not the initial value variable). This will create the variable if
     *  not present.
     *
     *  @return The attribute that was found or created.
     *  @exception IllegalActionException If the container is null, or the name is invalid (from checkSingleAttribute).
     */
    @Override
    public Attribute getModifiedVariable() throws IllegalActionException {

        // Look for variable.  If not present, create it.
        NamedObj container = getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "No container.");
        }

        // Look for the variable holding the value
        // If a model is imported from ASCET2Ptolemy, this parameter will be created already,
        // and will hold the appropriate value
        return _checkSingleAttribute(variableName.getExpression(), container,
                new Token());
    }

    /** Similar to superclass getModifiedVariable(), but takes a
     *  name as an argument and does not used cached variables, and
     *  checks only in the proper scope.
     *  Also, it returns a variable (not an attribute).  This makes more sense but is
     *  inconsistent with superclass function.  Subclasses should use this function.
     *  Also, uses the default value if the variable is created.
     *
     *  @param name The name of the variable to get.
     *  @return The attribute that was found or created
     *  @exception IllegalActionException   If the container is null, or the name is invalid (from checkSingleAttribute)
     */
    public Variable getModifiedVariable(String name)
            throws IllegalActionException {

        // Look for variable.  If not present, create it.
        NamedObj container = getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "No container.");
        }

        // Beth 11/18/09 - Changed to create a null token here, instead of new Token()
        // which will assign the value "present"
        Attribute a = _checkSingleAttribute(name, container, null);
        if (a instanceof Variable) {
            return (Variable) a;
        } else {
            throw new IllegalActionException(
                    this,
                    "Actor "
                            + getName()
                            + " does not have a variable to store its state and/or initial value in.");
        }
    }

    /** Return the sharedName.
     *  @return Name shared by set of messages
     */
    public String getSharedName() {
        return _sharedName;
    }

    /** Return the modified variable.  Create it if it's not present.
     *  Different from getModifiedVariable, in that it returns a Variable, not an attribute.
     *
     *  @return  The modified variable.  Returns null if the variable name is not assigned
     *   or is the empty string.
     *  @exception IllegalActionException If there is a problem with the variable
     *   referenced by this actor.
     */
    public Variable getVariable() throws IllegalActionException {
        if (variableName.getExpression() != null
                && !variableName.getExpression().equals("")) {
            return getModifiedVariable(variableName.getExpression());
        } else {
            return null;
        }
    }

    /** Returns the variable's name.
     *  @return  The variable name.
     */
    public String getVariableName() {
        return variableName.getExpression();
    }

    /** Set up the type constraint between the output and the variable
     *  and the output and the initial value variable.
     *  @exception IllegalActionException If the actor cannot be preinitialized.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        // Check that parameter names have been entered.
        if (variableName.getExpression() == null
                || variableName.getExpression().equals("")
                || initialVariableName.getExpression() == null
                || initialVariableName.getExpression().equals("")) {
            throw new IllegalActionException(
                    this,
                    "Actor "
                            + getName()
                            + " has a null or empty string for its parameter or its inital value parameter.  Please enter a valid name.");
        }

        // Add a new type constraint relating the output to the modified variable
        // Preinitialize from the super class already adds the
        // constraint that the modified variable is at least
        // equal to the input
        // These functions will create the variable if not already present (as long as each expression contains
        // a valid value, which they do, since we just checked for null and empty names above).

        Variable var = getVariable();
        Variable initialVar = getInitialVariable();

        // If initial variable does not have a value, then assign a default value
        // Subclasses should override getDefaultValue()
        if (initialVar == null) {
            // FindBugs wants us to avoid dereferencing initialVar if it is null.
            _setValue(initialVar, _getDefaultValue());
        } else if (initialVar.getToken() == null
                || initialVar.getToken().isNil()) {
            _setValue(initialVar, _getDefaultValue());
        }

        // Set the variable type to at least the input (if connected)
        // Set the variable type to at least the initial value type
        // Set the output type to at least the variable
        if (input.isOutsideConnected()) {
            var.setTypeAtLeast(input);
        }
        // FindBugs says that initialVar could be null.
        if (initialVar != null) {
            var.setTypeAtLeast(initialVar);
        }
        output.setTypeAtLeast(var);

        // FindBugs was reportingthat initialVar could be null.
        if (initialVar == null) {
            throw new InternalErrorException(this, null, "initialVar is null?");
        } else {
            // Set value of parameter equal to the initial value
            // Note that if a default value has been assigned to the initial value, it will be overwritten
            _setValue(var, initialVar.getToken());
        }
    }

    /** When the actor name is changed, update the referenced variables
     * to point to the new parameters and check to see if these parameters
     * exist. This is also called when the actor is created, so don't need it in constructor.
     * Previously, the code for the ASCETArray would allow direct changes in the
     * variable name, in attributeChanged().  This is no longer allowed.
     * The user should change the name of the actor; then, the variable name change
     * will follow.
     *
     * @param name  The new name of the actor
     * @exception IllegalActionException If the name cannot be changed.
     * @exception NameDuplicationException If there is another actor with the same
     *  name in the actor's container
     */
    @Override
    public void setName(String name) throws IllegalActionException,
            NameDuplicationException {
        super.setName(name);

        // Set up strings for moml change requests
        // FIXME:  In the future, use this to manage parameter deletion for parameters
        // that are no longer needed when actors are deleted
        //ArrayList<String> momlChangeRequests = new ArrayList<String>();

        // Don't run this code the first time - the constructor has
        // its own code that needs to be executed before super.setName(name)
        // FIXME:  Refactor same code in constructor into a function

        if (variableName != null) {
            // Beth added 08/24/09
            // Fixed this to ensure that the underscore must be followed by all numbers
            // Otherwise, this part of the name should not be removed
            // lastIndexOf returns -1 if not found

            int underscore = name.lastIndexOf('_');

            if (underscore > 0 && name.substring(underscore).matches("_\\d+")) {
                _sharedName = name.substring(0, underscore);
            }

            else {
                _sharedName = name;
            }

            String compareName = _sharedName + "_parameter";
            String compareInitialName = _sharedName + "_initialValue";

            // If the new shared name is equal to the old shared name,
            // and the initial value is also equal, then no changes are needed
            if (variableName.getExpression() != null
                    && compareName.equals(variableName.getExpression())
                    && initialVariableName.getExpression() != null
                    && compareInitialName.equals(initialVariableName
                            .getExpression())) {
                return;
            }

            // Otherwise, the name is different
            // Get and save the current value, initial value, and number of instances
            // for the old parameter
            Variable value = getVariable();
            Variable initialValue = getInitialVariable();

            // This should always be true
            // FIXME throw an exception if it's not?
            // Possible if something else that's not a variable
            // already exists in container when message is created

            // Next, store the new names and check to see if these
            // variables already exist
            // If so, checkForVariables will use old values
            // Otherwise, create new variables that use the values (tokens)
            // of the current variables
            // numInstances is set to zero by checkForVariables
            // Then, increment numInstances

            variableName.setExpression(_sharedName + "_parameter");
            initialVariableName.setExpression(_sharedName + "_initialValue");

            // This should always be true
            // Beth 10/24/08 - Changed to always set the initial value to the same as the parameter value
            // when new parameters are created
            checkForAttributes(value.getToken(), initialValue.getToken());

            // If there are parameters to delete, then issue a change request to delete them
            // They should be in the top level container.
            // FIXME:  Future enhancement:  Track number of actors and issue a change
            // request to delete the variables if no actor uses them any more

            /*
            if (!momlChangeRequests.isEmpty())
            {
                            NamedObj container = getContainer();
                            while (container.getContainer() != null)
                            {
                                    container = container.getContainer();
                            }

                            for (int i = 0; i < momlChangeRequests.size(); i++)
                            {
                                    container.addChangeListener(this);
                                    MoMLChangeRequest request = new MoMLChangeRequest(this, container, momlChangeRequests.get(i));
                                    request.setUndoable(true);
                                    container.requestChange(request);
                            }

            }
             */

        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Supplies a default value for the variable, in the case that there
     *  is no initial value.
     *
     *  Subclasses should override getDefaultValue() to return an appropriate
     *  token.  If there is no appropriate token, the actor should
     *  throw an exception stating that a default value is required.
     *
     *  @return  A token containing the default value
     *  @exception IllegalActionException  Subclasses should throw an exception if
     *   an explicit initial value is required.
     */
    protected Token _getDefaultValue() throws IllegalActionException {
        return Token.NIL;
    }

    /** Set the value of the associated container's variable.
     *  Added a name here, to set the variable with this name.
     *
     *  Override base class function, since the base class function
     *  does not set the value if the tokens are equal.  This causes
     *  problems for the token solver.
     *
     *  @param variable The variable whose value will be set.
     *  @param value The new value as a string.
     *  @exception IllegalActionException If the variable cannot be set.
     */
    protected void _setValue(Attribute variable, String value)
            throws IllegalActionException {

        //Attribute variable = getModifiedVariable(name);

        if (variable instanceof Variable) {
            ((Settable) variable).setExpression(value);

            // NOTE: If we don't call validate(), then the
            // change will not propagate to dependents.
            ((Settable) variable).validate();
        }

        else {
            throw new IllegalActionException(this,
                    "Cannot set the value of the variable named: "
                            + variableName.getExpression());
        }

        // FIXME:  ALternatively - call superclass _setValue function?
        // Instead of throwing the exception?
    }

    /** Set the value of the associated container's variable.
     *  Added a name here, to set the variable with this name.
     *
     *  Override base class function, since the base class function
     *  does not set the value if the tokens are equal.  This causes
     *  problems for the token solver.
     *
     *  @param variable The variable whose value will be set.
     *  @param value The new value as a token.
     *  @exception IllegalActionException If the variable cannot be set.
     */
    protected void _setValue(Attribute variable, Token value)
            throws IllegalActionException {

        if (variable instanceof Variable) {
            ((Variable) variable).setToken(value);

            // NOTE: If we don't call validate(), then the
            // change will not propagate to dependents.
            ((Settable) variable).validate();
        }

        else {
            throw new IllegalActionException(this,
                    "Cannot set the value of the variable named: "
                            + variableName.getExpression());
        }

        // FIXME:  ALternatively - call superclass _setValue function?
        // Instead of throwing the exception?
    }

    /*
     *     protected void _setValue(Token value) throws IllegalActionException {
       Attribute variable = getModifiedVariable();

       if (variable instanceof Variable) {
           Token oldToken = ((Variable) variable).getToken();

           if (oldToken == null || !oldToken.equals(value)) {
               ((Variable) variable).setToken(value);

            // Beth 09/23/09 validate() used to be here
           }
           // Beth 09/23/09
           // Moved this statement outside of the if (!oldToken.equals(value))
           // Need to call validate() to notify token solvers of a change
           // so that they will record the correct value in the case
           // where the old token equals the new (otherwise token solvers don't see it)
           // NOTE: If we don't call validate(), then the
           // change will not propagate to dependents.
           ((Variable) variable).validate();
       } else if (variable instanceof Settable) {
           ((Settable) variable).setExpression(value.toString());

           // NOTE: If we don't call validate(), then the
           // change will not propagate to dependents.
           ((Settable) variable).validate();
       } else {
           throw new IllegalActionException(SetVariable.this,
                   "Cannot set the value of the variable " + "named: "
                           + variableName.getExpression());
       }
    }
     */

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The name shared by a set of messages.
        This is not directly settable by the user; rather, it is derived from
        the actor name. */
    protected String _sharedName;

    /** The scope of the actor.  See enumeration above.  Global by default. */
    protected Scope _scope = Scope.GLOBAL;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Execute check for a single variable and create that variable if it does not exist
     *  within the scope of the actor that is looking
     *  Returns the attribute
     *
     * @param name  The name of the variable to look for (and create if not found)
     * @param container  The original container of the actor that is looking for the variable
     * @param value  The token to assign to the variable if it is created
     * @return   True if the variable is created; false otherwise
     * @exception IllegalActionException If the attribute cannot be created.
     */
    private Attribute _checkSingleAttribute(String name, NamedObj container,
            Token value) throws IllegalActionException {

        Attribute a = null;
        NamedObj lastContainer = null;
        NamedObj curContainer = container;

        // Ensure that a valid name was given.
        // FIXME:  Throw exception otherwise?

        if (name != null && !name.equals("")) {

            // Look for an attribute with this name
            // If the scope is global (including imported or exported), look anywhere in
            // the hierarchy
            // If the scope is local, stop at the boundary of an opaque composite actor
            while (a == null && curContainer != null) {
                // Keep track of the last (top-level) container
                lastContainer = curContainer;
                a = curContainer.getAttribute(name);

                // For local scope, if the container is opaque (i.e. has a local director), stop the search
                if (_scope == Scope.LOCAL
                        && curContainer instanceof CompositeActor
                        && ((CompositeActor) curContainer).isOpaque()) {
                    break;
                }

                curContainer = curContainer.getContainer();
            }

            if (a == null) {

                try {
                    workspace().getWriteAccess();

                    // container might be null, so create the variable
                    // in the container of this actor.
                    // The search above ensures that lastContainer will be the correct
                    // location we want to create this variable in (local for local
                    // scope, top-most level for global scope)

                    a = new Parameter(lastContainer, name, value);
                    // wasCreated = true;

                } catch (NameDuplicationException ex) {
                    throw new InternalErrorException(ex);
                } finally {
                    workspace().doneWriting();
                }
            }
        }

        // Throw an exception if the name is null or the empty string.
        // Note this should not happen, if the user cannot set this directly, since
        // the actor's name is checked and must be valid, and the variable names are
        // based on the actor's name
        else {
            throw new IllegalActionException(
                    this,
                    "Actor "
                            + getName()
                            + " has an empty of null string for the parameter that holds its state or initial value.  Please enter a non-empty string.");
        }

        return a;
    }
}
