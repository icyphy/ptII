/* A Variable is an Attribute that contains a token and can be referenced in expressions.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.data.expr;

import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.Typeable;
import ptolemy.data.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.graph.InequalityTerm;
import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// Variable
/**
A Variable is an Attribute that contains a token and can be referenced in 
expressions.

A variable can be given a token or an expression as its value. To create 
a variable with a token, either call the appropriate constructor, or create 
the variable with the appropriate container and name, and then call 
setToken() to place the token in this variable. To create a variable from 
an expression, create the variable with the appropriate container and name, 
then call setExpression() to set its value. If a variable may be referred 
to by expressions of other variables, the name of the variable must be a
valid identifier as defined by the Ptolemy II expression language syntax.
<p>
If it is given an expression, then the token contained by this variable 
needs to be resolved via a call to evaluate(). If the expression string 
is null or empty, or if the token placed in it is null, then the token 
contained will be null. If the variable is set from an expression, PtParser 
is used to generate a parse tree from the expression which can then be 
evaluated to a token. Calling getToken() also results in evaluate() being 
called if the expression in the variable has not yet been evaluated.
<p>
The expression of a variable can only reference the variables added to the
scope of this variable. By default, all variables contained by the same
NamedObj and those contained by the NamedObj one level up in the hierarchy 
(i.e. contained by the container of the container of this variable, if it
has one) are added to the scope of this variable. When a variable gets a
new token, it will call the evaluate() method of all variables depending on
it to reflect the change.
<p>
A variable is a Typeable object. Its type is specified by a Class object
and can be set directly via a call to setType(). If its type is not set 
when the first non-null token is placed in it, then the type is set to the
class of the token. The type of a variable may also be determined by the
type resolution mechanism. In this case, a set of type constraints is 
derived from the expression of the variable. Additional type constraints
can be added via calls to the setTypeAtLeast() and setTypeEquals() methods.
<p>
After the initial determination of the type of a variable, whenever the
variable gets a new token (from a call to setToken() or evaluating its
expression), the type of the new token is checked to see if it can be 
converted to the type of the variable in a lossless manner. If it cannot 
then an exception is thrown. The type of a variable can be changed via a 
call to setType(). However the new type for the variable must be able to 
contain the currently contained token.
<p>
The type of the token returned by getToken() is the same as the type of
the variable. Thus even if a varible contains an IntToken, a DoubleToken 
will be returned if the type of the variable is DoubleToken. The actual 
token contained by the variable is obtained by calling getContainedToken(). 
The distinction between the token contained by the variable and the token 
returned by getToken() is only in the type of the token: the data value 
will always be the same.
<p>
A variable can also be reset. If the variable was originally set from a
token, then this token is placed again in the variable. If the variable
was originally given an expression, then this expression is placed again
in the variable and evaluated. Note that the type of the token resulting
from reset must be compatible with the current variable type.

@author Neil Smyth, Xiaojun Liu, Edward A. Lee
@version $Id$

@see ptolemy.kernel.util.Attribute
@see ptolemy.data.Token
@see ptolemy.data.expr.PtParser

*/

public class Variable extends Attribute implements Typeable {

    /** Construct a variable in the default workspace with an empty string
     *  as its name. The variable is added to the list of objects in the 
     *  workspace. Increment the version number of the workspace.
     */
    public Variable() {
        super();
    }

    /** Construct a variable in the specified workspace with an empty
     *  string as its name. The name can be later changed with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The variable is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the variable.
     */
    public Variable(Workspace workspace) {
        super(workspace);
    }

    /** Construct a variable with the given name as an Attribute of the 
     *  given container. The container argument must not be null, otherwise
     *  a NullPointerException will be thrown. This variable will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty 
     *  string. Increment the version number of the workspace.
     *  @param container The container.
     *  @param name The name of the variable.
     *  @exception IllegalActionException If the container does not accept
     *   a variable as its attribute.
     *  @exception NameDuplicationException If the name coincides with a
     *   variable already in the container.
     */
    public Variable(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a variable with the given container, name, and token.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown. This variable will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty 
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name.
     *  @param token The token contained by this variable.
     *  @exception IllegalActionException If the container does not accept
     *   a variable as its attribute.
     *  @exception NameDuplicationException If the name coincides with a
     *   variable already in the container.
     */
    public Variable(NamedObj container, String name, ptolemy.data.Token token)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setToken(token);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the variables contained in the argument to the scope of this 
     *  variable. If any of the variables bears the same name as one 
     *  already in the scope, then it will shadow the one in the scope.  
     *  Items in the list not of class Variable (or a derived class) are
     *  ignored.
     *  @param varList The list of variables to be added to scope.
     */
    public void addToScope(NamedList varList) {
        if (varList == null) {
            return;
        }

        Enumeration vars = varList.elements();
        while (vars.hasMoreElements()) {
            Object var = vars.nextElement();
            if (var instanceof Variable) {
                addToScope((Variable)var);
            }
        }
    }

    /** Add the variable specified by the argument to the scope of this 
     *  variable. If the variable bears the same name as one already in 
     *  the scope, then it will shadow the one in the scope.  
     *  @param var The variable to be added to scope.
     */
    public void addToScope(Variable var) {
        if ((var == null) || !_isLegalInScope(var)) {
            return;
        }
        Variable shadowed = null;
        if (_addedVars != null) {
            shadowed = (Variable)_addedVars.remove(var.getName());
        } else {
            _addedVars = new NamedList(this);
        }
        if (shadowed != null) {
            shadowed._removeScopeDependent(this);
        }
        try {
            _addedVars.prepend(var);
            var._addScopeDependent(this);
        } catch (IllegalActionException ex) {
            // This will not happen since we are prepending a Nameable
            // to _addedVars.
        } catch (NameDuplicationException ex) {
            // This will not happen since we make sure there will not
            // be name duplication.           
        }
        _scopeVersion = -1;
        _destroyParseTree();
    }

    // It is sometimes easy to get an enumeration of variables, so consider adding
    // this method for convenience.
    // public void addToScope(Enumeration vars) {}

    /** Clone the variable.
     *  The state of the cloned variable will be identical to this variable,
     *  but with no variable added to its scope and no value dependencies.
     *  @param The workspace in which to place the cloned variable.
     *  @exception CloneNotSupportedException Thrown only in derived classes.
     *  @see java.lang.Object#clone()
     *  @return The cloned variable.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Variable newvar = (Variable)super.clone(ws);
        // FIXME: deal with all private and protected fields.
        return newvar;
    }

    /** Evaluate the current expression to a token. If this variable
     *  was last set directly with a token do nothing. If this variable
     *  changes due to this evaluation, it will call this method on all 
     *  value dependents registered with it. This method also detects 
     *  dependency loops among variables. The token contained by this
     *  variable is set to null if the current expression is illegal or
     *  the type of the resulting token is not compatible with the type
     *  of this variable.
     *  <p>
     *  Part of this method is read-synchronized on the workspace.
     */
    public void evaluate() {
        if (_currentExpression == null) {
	    return;
	}
        _token = null;
	if (_dependencyLoop) {
            throw new IllegalExpressionException("Found dependency loop "
                    + "when evaluating " + getFullName() 
                    + ": " + _currentExpression);
        }
        _dependencyLoop = true;

        try {
            workspace().getReadAccess();
            _buildParseTree();
            ptolemy.data.Token newToken = _parseTree.evaluateParseTree();
            if (_varType == null) {
                // The type of this variable has not been set yet.
                _varType = newToken.getClass();
            } else {
                _checkType(newToken.getClass());
            }
	    if (_noTokenYet) {
		// This is the first token stored in this variable.
		_initialExpression = _currentExpression;
		_noTokenYet = false;
	    }
            _token = newToken;
            _castToken = null;
            _needsEvaluation = false;
            _noEvaluationYet = false;
            _notifyValueDependents();
        } finally {
	    _dependencyLoop = false;
	    workspace().doneReading();
	}
    }

    /** Get the token contained by this variable. It may be null.
     *  The token is not converted to the type of this variable.
     *  The contained token is either the token placed directly 
     *  into this variable, or the result of evaluating the current 
     *  expression. This method will call evaluate if this variable
     *  was last set from an expression and the expression has not
     *  been evaluated yet.
     *  @return The token contained by this variable.
     */
    public ptolemy.data.Token getContainedToken() {
        if (_needsEvaluation || _noEvaluationYet) {
            evaluate();
        }
        return _token;
    }

    /** Get the expression currently used by this variable. If the
     *  variable was last set directly via a token being placed in 
     *  it, then the variable does not have an expression and null 
     *  is returned.
     *  @return The expression used by this variable.
     */
    public String getExpression() {
        return _currentExpression;
    }

    /** Obtain a NamedList of the variables that the value of this
     *  variable can depend on. These include the variables added to
     *  the scope of this variable and the variables in the same 
     *  NamedObj and those one level up in the hierarchy. It catches 
     *  any exceptions thrown by NamedList because if there is a 
     *  clash in the names of the two scoping levels, the variable 
     *  from the top level is considered not to be visible in the 
     *  scope of this variable. A variable also cannot reference 
     *  itself.
     *  <p>
     *  This method is read-synchronized on the workspace.
     *  @return The variables on which this variable can depend.
     */
    public NamedList getScope() {
        if (_scopeVersion == workspace().getVersion()) {
            return _scope;
        }
        try {
            workspace().getReadAccess();
            if (_addedVars != null) {
                _scope = new NamedList(_addedVars);
            } else {
                _scope = new NamedList();
            }
            NamedObj container = (NamedObj)getContainer();
            if (container != null) {
                NamedObj containerContainer =
                    (NamedObj)container.getContainer();
                Enumeration level1 = container.getAttributes();
                Attribute var = null;
                while (level1.hasMoreElements()) {
                    // add the variables in the same NamedObj to _scope
                    // exclude this
                    var = (Attribute)level1.nextElement();
                    if ((var instanceof Variable) && (var != this)) {
                        if (!_isLegalInScope((Variable)var)) {
                            continue;
                        }
                        try {
                            _scope.append(var);
                            ((Variable)var)._addScopeDependent(this);
                        } catch (NameDuplicationException ex) {
                            // This occurs when a variable in the same NamedObj
                            // has the same name as a variable added to the
                            // scope of this variable. The variable in the same
                            // NamedObj is shadowed.
                        } catch (IllegalActionException ex) {
                            // This should not happen since we are dealing with
                            // variables which are Nameable.
                        }
                    }
                }
                if (containerContainer != null) {
                    Enumeration level2 = containerContainer.getAttributes();
                    while (level2.hasMoreElements()) {
                        var = (Attribute)level2.nextElement();
                        try {
                            if (var instanceof Variable) {
                                if (_isLegalInScope((Variable)var)) {
                                    _scope.append(var);
                                    ((Variable)var)._addScopeDependent(this);
                                }
                            }
                        } catch (NameDuplicationException ex) {
                            // Name clash between the two levels of scope, 
                            // or a variable at the upper level has the same 
                            // name as a variable added to the scope of this 
                            // variable. The upper level variable is shadowed.
                        } catch (IllegalActionException ex) {
                            // This should not happen since we are dealing with
                            // variables which are Nameable.
                        }
                    }
                }
            }
            _scopeVersion = workspace().getVersion();
            return _scope;
        } finally {
            workspace().doneReading();
        }
    }

    /** Get the token contained by this variable converted to the type of
     *  this variable. It may be null. This means that if the variable
     *  is of type DoubleToken, and the token currently contained by the
     *  variable is of type IntToken, then the IntToken is converted to
     *  a DoubleToken and returned. This method will call evaluate if this 
     *  variable was last set from an expression and the expression has not
     *  been evaluated yet.
     *  @return The token contained by this variable converted to the
     *   type of this variable.
     */
    public ptolemy.data.Token getToken() {
        if (_needsEvaluation || _noEvaluationYet) {
            evaluate();
        }
        if (_token == null) {
            return null;
        }
        if (_castToken != null) {
            return _castToken;
        }

        if (_convertMethod == null) {
            try {
                Class[] arg = new Class[1];
                arg[0] = Token.class;
                _convertMethod = _varType.getMethod("convert", arg);
            } catch (NoSuchMethodException ex) {
                // Cannot happen if _varType is a subclass of 
                // ptolemy.data.Token
                throw new InternalErrorException("Variable type "
                        + "is not a subclass of ptolemy.data.Token: "
                        + ex.getMessage());
            }
        }

        // Need to convert the token contained by this variable to
        // the type of this variable.
        try {
            Object[] arg = new Object[1];
            arg[0] = _token;
            Object t = _convertMethod.invoke(null, arg);
            _castToken = (ptolemy.data.Token)t;
        } catch (IllegalAccessException ex) {
            // This should not happen since convert() is a public static
            // method of ptolemy.data.Token class.
            throw new InternalErrorException("Unable to access the "
                    + "convert() method of " + _varType.getName());
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new InternalErrorException("Convert method failed: " 
                    + e.getTargetException().getMessage() + " "
                    + e.getTargetException().getClass().getName());
        } 

        return _castToken;
    }

    /** Get the type of this variable. It is null if the type is not set.
     *  @return The type of this variable.
     */
    public Class getType() {
        return _varType;
    }

    /** Return an InequalityTerm whose value is the type of this variable.
     *  @return An InequalityTerm.
     */
    public InequalityTerm getTypeTerm() {
        // FIXME: finish this.
        return null;
    }

    /** Remove the argument from the scope of this variable.
     *  @param The variable to be removed from scope.
     */
    public void removeFromScope(Variable var) {
        if (_addedVars != null) {
            _addedVars.remove(var);
        }
        _scopeVersion = -1;
        _destroyParseTree();
    }

    /** Reset the current value of this variable to the first seen
     *  token or expression. If the variable was initially given a
     *  token, set the current token to that token. Otherwise evaluate
     *  the original expression given to the variable.
     */
    // FIXME: exception.
    // FIXME: shall we also discard currently set type?
    public void reset() {
        if (_noTokenYet) return;
        if (_origToken != null) {
            setToken(_origToken);
        } else {
            // must have an initial expression
            setExpression(_initialExpression);
            evaluate();
        }
    }

    /** Specify the container NamedObj, add this variable to the list 
     *  of attributes in the container. If this variable already has a
     *  container, remove this variable from the attribute list of the 
     *  current container first. Otherwise, remove it from the directory 
     *  of the workspace, if it is there. If the specified container is 
     *  null, remove this variable from the list of attributes of the
     *  current container. If the specified container already contains 
     *  an attribute with the same name, then throw an exception and do 
     *  not make any changes. Similarly, if the container is not in the 
     *  same workspace as this variable, throw an exception. If this 
     *  variable is already contained by the specified container, do 
     *  nothing. Otherwise, remove this variable from the scope of any
     *  scope dependent of this variable.
     *  <p>
     *  This method is write-synchronized on the workspace and increments 
     *  its version number.
     *  @param container The proposed container of this variable.
     *  @exception IllegalActionException If the container will not accept
     *   a variable as its attribute, or this variable and the container 
     *   are not in the same workspace, or the proposed container would 
     *   result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attriubte with the name of this variable.
     */
    public void setContainer(NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        Nameable cont = getContainer();
        super.setContainer(container);
        if (container != cont) {
            // This variable changed container, clear all dependencies
            // involving this variable.
            if (_scopeDependents != null) {
                Enumeration vars = _scopeDependents.elements();
                while (vars.hasMoreElements()) {
                    Variable var = (Variable)vars.nextElement();
                    var.removeFromScope(this);
                }
                _scopeDependents.clear();
            }

            _destroyParseTree();
            if (_scope != null) {
                Enumeration vars = _scope.elements();
                while (vars.hasMoreElements()) {
                    Variable var = (Variable)vars.nextElement();
                    var._removeScopeDependent(this);
                }
            }
            if (_addedVars != null) {
                _addedVars.removeAll();
            }
        }
    }

    /** Set the expression of this variable. If the expression string 
     *  is null, the token contained by this variable is set to null. 
     *  If it is not null, the expression is stored to be evaluated at
     *  a later stage. To evaluate the expression now, invoke the 
     *  method evaluate() on this variable. Value dependencies on
     *  other variables built for any previous expression are cleared.
     *  @param expr The expression for this variable.
     */
    public void setExpression(String expr) {
        if (expr == null) {
            setToken(null);
            _noEvaluationYet = false;
        } else {
            _noEvaluationYet = true;
        }
        _currentExpression = expr;
        _destroyParseTree();
    }

    /** Put a new token in this variable. This is the way to give the
     *  variable a new simple value. If the previous token in the 
     *  variable was the result from evaluating an expression, the 
     *  dependencies registered with other variables for that expression
     *  are cleared.
     *  @param token The new token to be stored in this variable.
     *  @exception IllegalArgumentException If the type of token is not
     *   compatible with the type of this variable.
     */
    public void setToken(ptolemy.data.Token token)
            throws IllegalArgumentException {
        if (token == null) {
            _token = null;
        } else {
            // Only change the token stored in this variable if 
            // the type of the new token is compatible with the 
            // type of this variable.
            _checkType(token.getClass());
            _token = token;
            if (_noTokenYet) {
                _origToken = _token;
                _noTokenYet = false;
                setType(_token.getClass());
            }
        }
        _castToken = null;

        if (_currentExpression != null) {
            _currentExpression = null;
            _destroyParseTree();
        }
        
        _notifyValueDependents();
    }

    /** Set the type of this variable to the specified value.
     *  It must be possible to losslessly convert the currently
     *  contained token to the new type, otherwise an exception 
     *  will be thrown. If so, the state of the variable is not
     *  changed.
     *  @param type The desired type for this variable.
     *  @exception IllegalArgumentException If the current token
     *   in this variable cannot be losslessly converted to the
     *   desired type, or this type is not an instantiable type
     *   in the type lattice.
     */
    public void setType(Class type) {
        if ((type == null) || !TypeLattice.isInstantiableType(type)) {
            throw new IllegalArgumentException("Variable.setType(): "
                    + "the argument " + type
                    + " is not an instantiable type in the type lattice.");
        }
        if (_token != null) {
            int typeInfo = TypeLattice.compare(_token.getClass(), type);
            if ((typeInfo == CPO.HIGHER) || (typeInfo == CPO.INCOMPARABLE)) {
                // FIXME: IllegalActionException?
                throw new IllegalArgumentException("Variable.setType(): "
                        + "the currently contained token " + _token.toString()
                        + " cannot be losslessly converted to the desired type "
                        + type.toString());
            }
        }
        _varType = type;
        _convertMethod = null;
        _castToken = null;
        _typeIsSet = true;
    }

    /** Constraint that the type of this variable is equal to or 
     *  greater than the type of the specified typeable object.
     */
    public void setTypeAtLeast(Typeable lesser) {}

    /** Constraint that the type of this variable is the same as the
     *  type of the specified typeable object.
     */
    public void setTypeEquals(Typeable equal) {}

    /** Return a string representation of the current variable value.
     *  @return A string representing the class and the current token.
     */
    public String toString() {
        return super.toString() + " " + getToken();
    }

    /** Return the type constraints of this variable.
     *  The constraints are an enumeration of inequalities.
     *  @return an enumeration of Inequality.
     *  @see ptolemy.graph.Inequality
     */
    public Enumeration typeConstraints() {
        // FIXME: finish this.
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add the argument as a scope dependent of this variable. This
     *  is called when this variable is added to the scope of the 
     *  argument.
     *  @param var The variable having this variable in its scope.
     */
    protected void _addScopeDependent(Variable var) {
        if (_scopeDependents == null) {
            _scopeDependents = new LinkedList();
        }
        if (_scopeDependents.includes(var)) {
            return;
        }
        _scopeDependents.insertFirst(var);
    }

    /** Add the argument as a value dependent of this variable. This
     *  is called when PtParser finds that this variable is referenced
     *  by the expression of the argument.
     *  @param var The variable whose expression references this variable.
     */
    protected void _addValueDependent(Variable var) {
        if (_valueDependents == null) {
            _valueDependents = new LinkedList();
        }
        if (_valueDependents.includes(var)) {
            return;
        }
        _valueDependents.insertFirst(var);
    }

    /** Build a parse tree for the current expression using PtParser. 
     *  Do nothing if a parse tree already exists.
     */
    protected void _buildParseTree() {
        if (_parseTree != null) {
            return;
        }
        if (_parser == null) {
            _parser = new PtParser(this);
        }
        _parseTree = _parser.generateParseTree(
                _currentExpression, getScope());
        return;
    }

    /** Return a description of this variable.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A string describing this variable.
     */
    // FIXME: this is just a prototype.
    protected String _description(int detail, int indent, int bracket) {
        try {
            workspace().getReadAccess();
            String result = _getIndentPrefix(indent);
            if ((bracket == 1) || (bracket == 2)) {
                result += "{";
            }
            result += toString();
            if (bracket == 2) {
                result += "}";
            }
            return result;
        } finally {
            workspace().doneReading();
        }
    }

    /** Destroy the current parse tree.
     */
    protected void _destroyParseTree() {
        if (_parseTree != null) {
            _clearDependencies(_parseTree);
            _parseTree = null;
        }
        if (_currentExpression != null) {
            _needsEvaluation = true;
        }
        return;
    }

    /** Return true if the argument is legal to be added to the scope
     *  of this variable. In this base class, this method only checks
     *  that the argument is in the same workspace as this variable.
     *  @param var The variable to be checked.
     *  @return True if the argument is legal.
     */
    protected boolean _isLegalInScope(Variable var) {
        return (var.workspace() == this.workspace());
    }

    /** Notify the value dependents of this variable that this variable
     *  changed.
     */
    protected void _notifyValueDependents() {
        if (_valueDependents == null) {
            return;
        }
        Enumeration vars = _valueDependents.elements();
        while (vars.hasMoreElements()) {
            Variable var = (Variable)vars.nextElement();
            // var._update();
            var.evaluate();
        }
    }        

    /** Remove the argument from the list of scope dependents of this
     *  variable.
     *  @param var The variable whose scope no longer includes this
     *   variable.
     */
    protected void _removeScopeDependent(Variable var) {
        _scopeDependents.exclude(var);
    }

    /** Remove the argument from the list of value dependents of this
     *  variable.
     *  @param var The variable whose value no longer depends on this
     *   variable.
     */
    protected void _removeValueDependent(Variable var) {
        _valueDependents.exclude(var);
    }

    /** Update the value of this variable.
     */
    /* protected void _update() {
        evaluate();
    } */

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // Flags that the variable was last set with an expression and that
    // the expression has not yet been evaluated.
    protected boolean _noEvaluationYet = false;

    // Stores the variables whose expression references this variable.
    protected LinkedList _valueDependents = null; 

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  Check whether the token type specified by the argument is compatible
     *  with the current variable type. If not an exception is thrown.
     *  @param newType The Class objet representing the type of a token
     *   which must be compatible with the type of this variable.
     *  @exception IllegalArgumentException If the token type is not 
     *   compatible with the type of this variable.
     */
    private void _checkType(Class tokenType) {
        if (_varType == null) {
            return;
        }
        int typeInfo = TypeLattice.compare(_varType, tokenType);
        if ((typeInfo == CPO.HIGHER) || (typeInfo == CPO.SAME)) {
            return;
        }
        // Incompatible type!
        throw new IllegalArgumentException("Cannot store a token of type "
                + tokenType.getName() + " in " + getFullName() + " of class "
                + this.getClass().getName() + " with type " 
                + _varType.getName());
    }

    /*  Clear the value dependencies this variable has registered
     *  with other variables. If this is not done a phantom web of
     *  dependencies may exist which could lead to false dependency 
     *  loops being detected. Normally this method is called on the 
     *  root node of the parse tree and recursively calls itself to 
     *  visit the whole tree.
     *  @param node The node in the parse tree below which all 
     *   dependencies are cleared.
     */
    private void _clearDependencies(Node node) {
        int children = node.jjtGetNumChildren();
        if (children > 0) {
            for (int i = 0; i < children; i++) {
                _clearDependencies(node.jjtGetChild(i));
            }
            return;
        }
        if (!(node instanceof ASTPtLeafNode)) {
            throw new InternalErrorException("If a node has no children,"
                    + " it must be a leaf node.");
        }
        ASTPtLeafNode leaf = (ASTPtLeafNode)node;
        if (leaf._var != null) {
            leaf._var._removeValueDependent(this);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Stores the variables added to the scope of this variable.
    private NamedList _addedVars = null;

    // Caches the cast value of the currently contained token to the 
    // variable type.
    private Token _castToken = null;

    // Stores the object representing the convert method of the token
    // class representing the type of this variable.
    private java.lang.reflect.Method _convertMethod = null;

    // Stores the expression used to set this variable. It is null if
    // the variable was set from a token.
    private String _currentExpression = null;

    // Used to check for dependency loops among variables.
    private boolean _dependencyLoop = false;

    // Stores the expression used to initialize this variable. It is null if
    // the first token placed in the variable is not the result of evaluating
    // an expression.
    private String _initialExpression;

    // Flags that the expression needs to be evaluated when the value of this
    // variable is queried.
    private boolean _needsEvaluation = false;

    // Flags whether the variable has not yet contained a token.
    private boolean _noTokenYet = true;

    // Stores the first token placed in this variable. It is null if the
    // first token contained by this variable was the result of evaluating
    // an expression.
    private ptolemy.data.Token _origToken;

    // Stores the variables whose scope contains this variable.
    private LinkedList _scopeDependents = null;

    // The variables this variable may reference in its expression.
    // The list is cached.
    private NamedList _scope = null;
    private long _scopeVersion = -1;

    // Stores the Class object which represents the type of this variable.
    private Class _varType;

    // The parser used by this variable to parse expressions.
    private PtParser _parser;

    // If the variable was last set from an expression, this stores
    // the parse tree for that expression.
    private ASTPtRootNode _parseTree;

    // The token contained by this parameter.
    private ptolemy.data.Token _token;

    // Flags that the type of this variable is determined from a call to
    // setType() or by the first token placed in this variable via a call
    // to setToken().
    private boolean _typeIsSet = false;

}
