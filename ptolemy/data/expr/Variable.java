/* A variable is an attribute that contains a token and can be referenced
in expressions.

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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)

*/

package ptolemy.data.expr;

import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.type.*;
import ptolemy.graph.Inequality; // For javadoc
import ptolemy.graph.*;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.io.Writer;
import java.io.IOException;

//////////////////////////////////////////////////////////////////////////
//// Variable
/**
A variable is an attribute that contains a token, and can be set by an
expression that can refer to other variables.
<p>
A variable can be given a token or an expression as its value. To create
a variable with a token, either call the appropriate constructor, or create
the variable with the appropriate container and name, and then call
setToken(). To set the value from an expression, call setExpression().
The expression is not actually evaluated until you call getToken() or
getType(). If the expression string is null or empty, or if no value
has been specified, then getToken() will return null.
<p>
Consider for example the sequence:
<pre>
   Variable v3 = new Variable(container,"v3");
   Variable v2 = new Variable(container,"v2");
   Variable v1 = new Variable(container,"v1");
   v3.setExpression("v1 + v2");
   v2.setExpression("1.0");
   v1.setExpression("2.0");
   v3.getToken();
</pre>
Notice that the expression for <code>v3</code> cannot be evaluated
when it is set because <code>v2</code> and <code>v1</code> do not
yet have values.  But there is no problem because the expression
is not evaluated until getToken() is called.
<p>
The expression can only reference variables that are
added to the scope of this variable using addToScope()
before the expression is evaluated
(i.e., before getToken() is called). Otherwise, getToken() will throw
an exception.  By default, all variables
contained by the same container, and those contained by the container's
container, are in the scope of this variable. Thus, in the above,
all three variables are in each other's scope because they belong
to the same container. If there are variables in the scope with the
same name, then those lower in the hierarchy shadow those that are higher.
<p>
If a variable is referred
to by expressions of other variables, then the name of the variable must be a
valid identifier as defined by the Ptolemy II expression language syntax.
<p>
A variable is a Typeable object. Constraints on its type can be
specified relative to other Typeable objects (as inequalities on the types),
or relative to specific types.  The former are called <i>dynamic type
constraints</i>, and the latter are called <i>static type constraints</i>.
Static type constraints are specified by the methods:
<ul>
<li> setTypeEquals()
<li> setTypeAtMost()
</ul>
whereas dynamic type constraints are given by
<ul>
<li> setTypeAtLeast()
<li> setTypeSameAs()
</ul>
Static type constraints are enforced in this class, meaning that:
<ul>
<li> if the variable already has a value (set by setToken() or
     setExpression()) when you set the static type constraint, then
     the value must satisfy the type constraint; and
<li> if after setting a static type constraint you call give the token
     a value, then the value must satisfy the constraints.
</ul>
A violation will cause an exception (either when setToken() is called
or when the expression is evaluated).
<p>
The dynamic type constraints are not enforced in this class, but merely
reported by the typeConstraintList() method.  They must be enforced at a
higher level (by a type system) since they involve a network of variables
and other typeable objects.  In fact, if the variable does not yet have
a value, then a type system may use these constraints to infer what the
type of the variable needs to be, and then call setTypeEquals().
<p>
The token returned by getToken() is always an instance of the class given
by the getType() method.  This is not necessarily the same as the class
of the token that was inserted via setToken().  It might be a distinct
type if the token given by setToken() can be converted losslessly into one
of the type given by setTypeEquals().
<p>
A variable can also be reset. If the variable was originally set from a
token, then this token is placed again in the variable, and the type of the
variable is set to equal that of the token. If the variable
was originally given an expression, then this expression is placed again
in the variable (but not evaluated), and the type is reset to BaseType.NAT.
The type will be determined when the expression is evaluated or when
type resolution is done.
<p>
A variable has no MoML description (MoML is an XML modeling markup
language).  Thus, a variable contained by a named object is not
persistent, in that if the object is exported to a MoML file, the
variable will not be represented.  If you prefer that the variable
be represented, then you should use the derived class Parameter instead.

@author Neil Smyth, Xiaojun Liu, Edward A. Lee, Yuhong Xiong
@version $Id$

@see ptolemy.data.Token
@see ptolemy.data.expr.PtParser
@see ptolemy.data.expr.Parameter

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

    /** Construct a variable with the given name as an attribute of the
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
        // Notification is important here so that the attributeChanged()
        // method of the container is called.
        _setToken(token);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the variables enumerated by the argument to the scope of this
     *  variable. If any of the variables bears the same name as one
     *  already in the scope, then it will shadow the one in the scope.
     *  Items in the list that are not instances of the class Variable (or a
     *  derived class) are ignored.  By default, variable of the container
     *  and of the container's container are in scope.
     *  @param variables An enumeration of variables to be added to the scope.
     */
    public void addToScope(Enumeration variables) {
        while (variables.hasMoreElements()) {
            Object var = variables.nextElement();
            if (var instanceof Variable) {
                addToScope((Variable)var);
            }
        }
    }

    /** Add the variable specified by the argument to the scope of this
     *  variable. If the variable bears the same name as one already in
     *  the scope, then it will shadow the one in the scope.
     *  @param var The variable to be added to the scope.
     */
    public void addToScope(Variable var) {
        if ((var == null) || !_isLegalInScope(var)) {
            return;
        }
        Variable shadowed = null;
        if (_scopeVariables != null) {
            shadowed = (Variable)_scopeVariables.remove(var.getName());
        } else {
            _scopeVariables = new NamedList(this);
        }
        if (shadowed != null) {
            shadowed._removeScopeDependent(this);
        }
        try {
            _scopeVariables.prepend(var);
            var._addScopeDependent(this);
        } catch (IllegalActionException ex) {
            // This will not happen since we are prepending a Nameable
            // to _scopeVariables.
            throw new InternalErrorException(ex.getMessage());
        } catch (NameDuplicationException ex) {
            // This will not happen since we make sure there will not
            // be name duplication.
            throw new InternalErrorException(ex.getMessage());
        }
        _scopeVersion = -1;
        _destroyParseTree();
    }

    /** Add a listener to be notified when the value of this variable changes.
     *  @param listener The listener to add.
     */
    public void addValueListener(ValueListener listener) {
        if (_valueListeners == null) {
            _valueListeners = new LinkedList();
        }
        _valueListeners.add(listener);
    }

    /** Clone the variable.  This creates a new variable containing the
     *  same token (if the value was set with setToken()) or the same
     *  (unevaluated) expression, if the expression was set with
     *  setExpression().  The clone also recalls the same initial
     *  expression or initial token, so that reset() on the clone behaves
     *  as reset() on the original. The list of variables added to the scope
     *  is not cloned; i.e., the clone has an empty scope.
     *  The clone has the same static type constraints (those given by
     *  setTypeEquals() and setTypeAtMost()), but none of the dynamic
     *  type constraints (those relative to other variables).
     *  @param workspace The workspace in which to place the cloned variable.
     *  @exception CloneNotSupportedException Not thrown in this base class.
     *  @see java.lang.Object#clone()
     *  @return The cloned variable.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Variable newvar = (Variable)super.clone(workspace);
        newvar._scopeVariables = null;
        // _currentExpression and _initialExpression are preserved in clone
        if (_currentExpression != null) {
            newvar._needsEvaluation = true;
        }
        newvar._dependencyLoop = false;
        // _noTokenYet and _initialToken are preserved in clone
        newvar._scopeDependents = null;
        newvar._valueDependents = null;
        newvar._scope = null;
        newvar._scopeVersion = -1;

	// set _declaredType and _varType
	if (_declaredType instanceof StructuredType &&
					!_declaredType.isConstant()) {
	    newvar._declaredType =
				(Type)((StructuredType)_declaredType).clone();
	    newvar._varType = newvar._declaredType;
	}
        // _typeAtMost is preserved
        newvar._parser = null;
        newvar._parseTree = null;

	newvar._constraints = new LinkedList();
        newvar._typeTerm = null;
        return newvar;
    }

    /** Write a MoML description of this object, which in this case is
     *  empty.  Nothing is written.
     *  MoML is an XML modeling markup language.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     */
    public void exportMoML(Writer output, int depth) throws IOException {
    }

    /** Get the expression currently used by this variable. If the
     *  variable was last set directly via setToken(),
     *  then the variable does not have an expression and null
     *  is returned.
     *  @return The expression used by this variable.
     */
    public String getExpression() {
        return _currentExpression;
    }

    /** Return a NamedList of the variables that the value of this
     *  variable can depend on. These include the variables added to
     *  the scope of this variable by the addToScope()
     *  methods, and the variables in the container (if any) and in the
     *  container's container (if any).
     *  If there are variables with the same name in these various
     *  places, then they are shadowed as follows.  The most recently
     *  added variable using addToScope() is given priority, followed by
     *  a variable contained by the container of this variable, followed
     *  by a variable contained by the container of the container.
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
            if (_scopeVariables != null) {
                _scope = new NamedList(_scopeVariables);
            } else {
                _scope = new NamedList();
            }
            NamedObj container = (NamedObj)getContainer();
            if (container != null) {
                NamedObj containerContainer =
                    (NamedObj)container.getContainer();
                Iterator level1 = container.attributeList().iterator();
                Attribute var = null;
                while (level1.hasNext()) {
                    // add the variables in the same NamedObj to _scope,
                    // excluding this
                    var = (Attribute)level1.next();
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
                    Iterator level2 =
                        containerContainer.attributeList().iterator();
                    while (level2.hasNext()) {
                        var = (Attribute)level2.next();
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

    /** Get the token contained by this variable.  The type of the returned
     *  token is always that returned by getType().  Calling this method
     *  will trigger evaluation of the expression, if the value has been
     *  given by setExpression(). Notice the evaluation of the expression
     *  can trigger an exception if the expression is not valid, or if the
     *  result of the expression violates type constraints specified by
     *  setTypeEquals() or setTypeAtMost(), or if the result of the expression
     *  is null and there are other variables that depend on this one.
     *  The returned value will be null if neither an expression nor a
     *  token has been set, or either has been set to null.
     *  @return The token contained by this variable converted to the
     *   type of this variable, or null if there is none.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    public ptolemy.data.Token getToken() throws IllegalActionException {
        _evaluate();
        return _token;
    }

    /** Get the type of this variable. It is BaseType.NAT if the type has
     *  not set by setTypeEquals(), no token has been set by setToken(),
     *  and no expression has been set by setExpression(). Calling this method
     *  will trigger evaluation of the expression, if the value has been
     *  given by setExpression(). Notice the evaluation of the expression
     *  can trigger an exception if the expression is not valid, or if the
     *  result of the expression violates type constraints specified by
     *  setTypeEquals() or setTypeAtMost(), or if the result of the expression
     *  is null and there are other variables that depend on this one.
     *  The returned value will be BaseType.NAT if neither an expression nor a
     *  token has been set.
     *  @return The type of this variable.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    public Type getType()
	    throws IllegalActionException {
        _evaluate();
        return _varType;
    }

    /** Return an InequalityTerm whose value is the type of this variable.
     *  @return An InequalityTerm.
     */
    public InequalityTerm getTypeTerm() {
        if (_typeTerm == null) {
	    _typeTerm = new TypeTerm(this);
	}
        return _typeTerm;
    }

    /** Remove the items in the enumeration from the scope of this variable.
     *  Any item in the enumeration that is not an instance of variable
     *  is ignored.  Also, variables that are in the scope because they
     *  are contained by the container of this variables (or its container)
     *  cannot be removed.  An attempt to do so will be ignored.
     *  Note also that if any of the removed variables are shadowing
     *  another variable with the same name, the shadowed variable <i>does
     *  not</i> reappear in the scope.  It has to be specifically added again.
     *  @param variables An enumeration of variables to be removed from scope.
     */
    public void removeFromScope(Enumeration variables) {
        while (variables.hasMoreElements()) {
            Object var = variables.nextElement();
            if (var instanceof Variable) {
                removeFromScope((Variable)var);
            }
        }
    }

    /** Remove the argument from the scope of this variable.
     *  Also, variables that are in the scope because they
     *  are contained by the container of this variables (or its container)
     *  cannot be removed.  An attempt to do so will be ignored.
     *  Note also that if the removed variable is shadowing
     *  another variable with the same name, the shadowed variable <i>does
     *  not</i> reappear in the scope.  It has to be specifically added again.
     *  @param The variable to be removed from scope.
     */
    public void removeFromScope(Variable var) {
        if (_scopeVariables != null) {
            _scopeVariables.remove(var);
        }
        _scopeVersion = -1;
        _destroyParseTree();
    }

    /** Remove a listener from the list of listeners that is
     *  notified when the value of this variable changes.  If no such listener
     *  exists, do nothing.
     *  @param listener The listener to remove.
     */
    public void removeValueListener(ValueListener listener) {
        if (_valueListeners != null) {
            _valueListeners.remove(listener);
        }
    }

    /** Reset the variable to its initial value. If the variable was
     *  originally set from a token, then this token is placed again
     *  in the variable, and the type of the variable is set to equal
     *  that of the token. If the variable was originally given an
     *  expression, then this expression is placed again in the variable
     *  (but not evaluated), and the type is reset to BaseType.NAT.
     *  The type will be determined when the expression is evaluated or
     *  when type resolution is done.
     */
    public void reset() {
        if (_noTokenYet) return;
        if (_initialToken != null) {
            try {
                setToken(_initialToken);
            } catch (IllegalActionException ex) {
                // should not occur
                throw new InternalErrorException(ex.getMessage());
            }
        } else {
            // must have an initial expression
            setExpression(_initialExpression);
        }
    }

    /** Specify the container, and add this variable to the list
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
     *  nothing.
     *  <p>
     *  If this method results in a change of container (which it usually
     *  does), then remove this variable from the scope of any
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
     *   an attribute with the name of this variable.
     */
    public void setContainer(NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        Nameable cont = getContainer();
        super.setContainer(container);
        if (container != cont) {
            // This variable changed container, clear all dependencies
            // involving this variable.
            if (_scopeDependents != null) {
                Iterator variables = _scopeDependents.iterator();
                while (variables.hasNext()) {
                    Variable var = (Variable)variables.next();
                    var.removeFromScope(this);
                }
                _scopeDependents.clear();
            }

            _destroyParseTree();
            if (_scope != null) {
                Iterator vars = _scope.elementList().iterator();
                while (vars.hasNext()) {
                    Variable var = (Variable)vars.next();
                    var._removeScopeDependent(this);
                }
                /*
                Enumeration vars = _scope.elements();
                while (vars.hasMoreElements()) {
                    Variable var = (Variable)vars.nextElement();
                    var._removeScopeDependent(this);
                }
                */
            }
            if (_scopeVariables != null) {
                _scopeVariables.removeAll();
            }
        }
    }

    /** Set the expression of this variable. Evaluation is deferred until
     *  the value of the variable is accessed by getToken().  The
     *  container is not notified of the change until then.  If you need
     *  to notify the container right away, then call getToken().  If the
     *  argument is null, then getToken() will return null. However, if
     *  there are other variables that depend on its value, then upon
     *  evaluation to null, an exception will be thrown (by getToken()).
     *  If the type of this variable has been set with
     *  setTypeEquals(), then upon evaluation, the token will be
     *  converted into that type, if possible, or an exception will
     *  be thrown, if not.  If setTypeAtMost() has been called, then
     *  upon evaluation, it will be verified that the type
     *  constraint is satisfied, and if not, an exception will be thrown.
     *  @param expr The expression for this variable.
     */
    public void setExpression(String expr) {
        // NOTE: This should probably be a bit smarter and detect any
        // whitespace-only expression.
        if (expr == null || expr.equals("")) {
            _token = null;
            _needsEvaluation = false;
	    // set _varType
	    if (_declaredType instanceof BaseType) {
	    	_varType = _declaredType;
	    } else {
		// _varType = _declaredType
		((StructuredType)_varType).reset();
	    }
        } else {
            _needsEvaluation = true;
	    if (_varType instanceof StructuredType) {
		((StructuredType)_varType).needEvaluate(this);
	    }
        }
        _currentExpression = expr;
        _destroyParseTree();
    }

    /** Put a new token in this variable. If an expression had been
     *  previously given using setExpression(), then that expression
     *  is forgotten. If the type of this variable has been set with
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

        // Override any expression that may have been previously given.
        if (_currentExpression != null) {
            _currentExpression = null;
            _destroyParseTree();
        }
    }

    /** Constrain the type of this variable to be equal to or
     *  greater than the type of the specified object.
     *  This constraint is not enforced
     *  here, but is returned by the typeConstraintList() method for use
     *  by a type system.
     *  @param lesser A Typeable object.
     */
    public void setTypeAtLeast(Typeable lesser) {
        Inequality ineq = new Inequality(lesser.getTypeTerm(),
                this.getTypeTerm());
	_constraints.add(ineq);
    }

    /** Constrain the type of this variable to be equal to or
     *  greater than the type represented by the specified InequalityTerm.
     *  This constraint is not enforced here, but is returned by the
     *  typeConstraintList() method for use by a type system.
     *  @param typeTerm An InequalityTerm object.
     */
    public void setTypeAtLeast(InequalityTerm typeTerm) {
        Inequality ineq = new Inequality(typeTerm, this.getTypeTerm());
	_constraints.add(ineq);
    }

    /** Set a type constraint that the type of this object be less than
     *  or equal to the specified class in the type lattice.
     *  This replaces any constraint specified
     *  by an earlier call to this same method (note that there is no
     *  point in having two separate specifications like this because
     *  it would be equivalent to a single specification using the
     *  greatest lower bound of the two). This is an absolute type
     *  constraint (not relative to another Typeable object), so it
     *  is checked every time the value of the variable is set by
     *  setToken() or by evaluating an expression.  This type constraint
     *  is also returned by the typeConstraintList() methods.
     *  To remove the type constraint, call this method will a BaseType.NAT
     *  argument.
     *  @exception IllegalActionException If the type of this object
     *   already violates this constraint, or if the argument is not
     *   an instantiable type in the type lattice.
     */
    public void setTypeAtMost(Type type) throws IllegalActionException {
        if (type == BaseType.NAT) {
            _typeAtMost = BaseType.NAT;
            return;
        }
        if (!type.isInstantiable()) {
            throw new IllegalActionException(this, "setTypeAtMost(): "
                    + "the argument " + type
                    + " is not an instantiable type in the type lattice.");
        }

        Type currentType = getType();
        int typeInfo = TypeLattice.compare(currentType, type);
        if ((typeInfo == CPO.HIGHER) || (typeInfo == CPO.INCOMPARABLE)) {
            throw new IllegalActionException(this, "setTypeAtMost(): "
                    + "the current type " + currentType.toString()
                    + " is not less than the desired bounding type "
                    + type.toString());
        }
        _typeAtMost = type;
    }

    /** Set a type constraint that the type of this object equal
     *  the type corresponding to the specified Class. This is an absolute
     *  type constraint (not relative to another Typeable object), so it is
     *  checked every time the value of the variable is set by setToken() or
     *  by evaluating an expression.  If the variable already has a value,
     *  then that value is converted to the specified type, if possible, or
     *  an exception is thrown.
     *  @param c A Class.
     *  @exception IllegalActionException If the type of this object
     *   already violates this constraint, in that the currently contained
     *   token cannot be converted losslessly to the specified type; or
     *   the specified Class does not corresponds to a BaseType.
     *  @deprecated Use the method with a Type argument instead.
     */
    public void setTypeEquals(Class c) throws IllegalActionException {
	try {
	    Token token = (Token)c.newInstance();
	    setTypeEquals(token.getType());

	} catch (InstantiationException ie) {
	    throw new IllegalActionException("Variable.setTypeEquals(Class): "
		+ "Cannot create a token from the specified Class object. " +
		ie.getMessage());
	} catch (IllegalAccessException iae) {
	    throw new IllegalActionException("Variable.setTypeEquals(Class): "
		+ "Cannot create a token from the specified Class object. " +
		iae.getMessage());
	}
    }

    /** Set a type constraint that the type of this object equal
     *  the specified value. This is an absolute type constraint (not
     *  relative to another Typeable object), so it is checked every time
     *  the value of the variable is set by setToken() or by evaluating
     *  an expression.  If the variable already has a value, then that
     *  value is converted to the specified type, if possible, or an
     *  exception is thrown.
     *  To remove the type constraint, call this method with the argument
     *  BaseType.NAT.
     *  @param type A Type.
     *  @exception IllegalActionException If the type of this object
     *   already violates this constraint, in that the currently contained
     *   token cannot be converted losslessly to the specified type.
     */
    public void setTypeEquals(Type type) throws IllegalActionException {
        if (_token != null) {
	    if (type.isCompatible(_token)) {
		_token = type.convert(_token);
	    } else {
                throw new IllegalActionException(this,
		    "Variable.setTypeEquals(): the currently contained " +
		    "token " + _token.getClass().getName() + "(" +
		    _token.toString() + ") is not compatible " +
		    "with the desired type " + type.toString());
	    }
        }

	// set _declaredType to the argument.
	if (type instanceof BaseType) {
            _declaredType = type;
	} else {
	    // new type is StructuredType
	    StructuredType typeStruct = (StructuredType)type;

	    if (typeStruct.isConstant()) {
          	_declaredType = type;
	    } else {
		// new type is a variable StructuredType.
		try {
		    if (typeStruct.getUser() == null) {
		        typeStruct.setUser(this);
			_declaredType = type;
		    } else {
		        // new type already has a user, clone it.
			StructuredType newType =
				    (StructuredType)typeStruct.clone();
			newType.setUser(this);
			_declaredType = newType;
		    }
		} catch (IllegalActionException ex) {
		    // since the user was null, this should never happen.
		    throw new InternalErrorException("Variable.setTypeEquals: "
			    + "Cannot set user on the new type."
			    + ex.getMessage());
		}
	    }
	}

	// set _varType. It is _token.getType() if _token is not null, or
	// _declaredType if _token is null.
	_varType = _declaredType;
	if (_token != null && _declaredType instanceof StructuredType) {
	    ((StructuredType)_varType).updateType(
					(StructuredType)_token.getType());
        }
    }

    /** Constrain the type of this variable to be the same as the
     *  type of the specified object.  This constraint is not enforced
     *  here, but is returned by the typeConstraintList() method for use
     *  by a type system.
     *  @param equal A Typeable object.
     */
    public void setTypeSameAs(Typeable equal) {
        Inequality ineq = new Inequality(this.getTypeTerm(),
                equal.getTypeTerm());
	_constraints.add(ineq);
	ineq = new Inequality(equal.getTypeTerm(),
                this.getTypeTerm());
	_constraints.add(ineq);
    }

    /** Return a string representing the (possibly unevaluated) value
     *  of this variable.  If the value has been set by an expression,
     *  then return that expression.  If the value has been set via
     *  a token, then return a string representation of the value of that
     *  token that can be passed to the expression language in order to
     *  return a token with the same value.  If neither, then return an
     *  empty string.
     *  @return A string representation of this variable.
     */
    public String stringRepresentation() {
        String value = getExpression();
        if (value == null) {
            ptolemy.data.Token token = null;
            try {
                token = getToken();
            } catch (IllegalActionException ex) {}
            if (token != null) {
                if(token instanceof ptolemy.data.StringToken) {
                    // Double quotes must be added to the value of a
                    // string token.
                    value = "\"" + token.toString() + "\"";
                } else {
                    value = token.toString();
                }
            }
        }
        if (value == null) {
            value = "";
        }
        return value;
    }

    /** Return a string representation of the current evaluated variable value.
     *  @return A string representing the class and the current token.
     */
    public String toString() {
        ptolemy.data.Token val = null;
        try {
            val = getToken();
        } catch (IllegalActionException ex) {
            // The value of this variable is undefined.
        }
        return super.toString() + " " +
                ((val == null) ? "value undefined" : val.toString());
    }

    /** Return the type constraints of this variable.
     *  The constraints include the ones explicitly set to this variable,
     *  plus the constraint that the type of this variable must be no less
     *  than its current type, if it has one.
     *  The constraints are a list of inequalities.
     *  @return a list of Inequality objects.
     *  @see ptolemy.graph.Inequality
     */
    public List typeConstraintList() {
	// If this variable has a structured type, and the TypeTerm of this
	// variable is unsettable, make the component of the structured type
	// to be unsettable.
	if (_varType instanceof StructuredType) {
	    if ( !getTypeTerm().isSettable()) {
	    	((StructuredType)_varType).fixType();
	    } else  {
	    	((StructuredType)_varType).unfixType();
	    }
	}

        // Include all relative types that have been specified.
	List result = new LinkedList();
	result.addAll(_constraints);

        // If the variable has a type, add a constraint.
        // Type currentType = getType();
        // if (currentType != BaseType.NAT) {
        //     TypeConstant current = new TypeConstant(currentType);
        //     Inequality ineq = new Inequality(current, getTypeTerm());
        //     result.add(ineq);
        // }

        // If an upper bound has been specified, add a constraint.
        if (_typeAtMost != BaseType.NAT) {
            TypeConstant atMost = new TypeConstant(_typeAtMost);
            Inequality ineq = new Inequality(getTypeTerm(), atMost);
            result.add(ineq);
        }

	return result;
    }

    /** Return the type constraints of this variable.
     *  The constraints include the ones explicitly set to this variable,
     *  plus the constraint that the type of this variable must be no less
     *  than its current type, if it has one.
     *  The constraints are an enumeration of inequalities.
     *  @return an enumeration of Inequality objects.
     *  @see ptolemy.graph.Inequality
     *  @deprecated Use typeConstraintList() instead.
     */
    public Enumeration typeConstraints() {
	return Collections.enumeration(typeConstraintList());
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
        if (_scopeDependents.contains(var)) {
            return;
        }
        _scopeDependents.add(var);
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
        if (_valueDependents.contains(var)) {
            return;
        }
        _valueDependents.add(var);
    }

    /** Return a description of this variable.  This returns the same
     *  information returned by toString(), but with optional indenting
     *  and brackets.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A string describing this variable.
     */
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

    /** Evaluate the current expression to a token. If this variable
     *  was last set directly with a token, then do nothing. In other words,
     *  the expression is evaluated only if the value of the token was most
     *  recently given by an expression.  The expression is also evaluated
     *  if any of the variables it refers to have changed since the last
     *  evaluation.  If the value of this variable
     *  changes due to this evaluation, then notify all
     *  value dependents and notify the container (if there is one) by
     *  calling its attributeChanged() and attributeTypeChanged() methods,
     *  as appropriate. An exception is thrown
     *  if the expression is illegal, for example if a parse error occurs
     *  or if there is a dependency loop.
     *  <p>
     *  If evaluation results in a token that is not of the same type
     *  as the current type of the variable, then the type of the variable
     *  is changed, unless the new type is incompatible with statically
     *  specified types (setTypeEquals() and setTypeAtMost()).
     *  If the type is changed, the attributeTypeChanged() method of
     *  the container is called.  The container can reject the change
     *  by throwing an exception.
     *  <p>
     *  Part of this method is read-synchronized on the workspace.
     *
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated.
     */
    protected void _evaluate() throws IllegalActionException {
        if (!_needsEvaluation) return;
        // NOTE: This should probably be a bit smarter and detect any
        // whitespace-only expression.
        if (_currentExpression == null || _currentExpression.equals("")) {
            _setToken(null);
            return;
        }
        // If _dependencyLoop is true, then this call to evaluate() must
        // have been triggered by evaluating the expression of this variable,
        // which means that the expression directly or indirectly refers
        // to itself.
	if (_dependencyLoop) {
            _dependencyLoop = false;
            throw new IllegalActionException("Found dependency loop "
                    + "when evaluating " + getFullName()
                    + ": " + _currentExpression);
        }
        _dependencyLoop = true;

        try {
            workspace().getReadAccess();
            _buildParseTree();
            Token result = _parseTree.evaluateParseTree();
            _dependencyLoop = false;
            _setTokenAndNotify(result);
        } catch (IllegalActionException ex) {
            _needsEvaluation = true;
            throw ex;
        } finally {
	    _dependencyLoop = false;
	    workspace().doneReading();
	}
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
     *  changed.  This marks them to trigger evaluation next time their
     *  value is accessed.
     */
    protected void _notifyValueDependents() {
        if (_valueDependents != null) {
            Iterator variables = _valueDependents.iterator();
            while (variables.hasNext()) {
                Variable var = (Variable)variables.next();
                // Already marked?
                if (!var._needsEvaluation) {
                    var._needsEvaluation = true;
                    var._notifyValueDependents();
                }
            }
        }
    }

    /** Remove the argument from the list of scope dependents of this
     *  variable.
     *  @param var The variable whose scope no longer includes this
     *   variable.
     */
    protected void _removeScopeDependent(Variable var) {
        _scopeDependents.remove(var);
    }

    /** Remove the argument from the list of value dependents of this
     *  variable.
     *  @param var The variable whose value no longer depends on this
     *   variable.
     */
    protected void _removeValueDependent(Variable var) {
        _valueDependents.remove(var);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Stores the variables whose expression references this variable. */
    /*protected LinkedList _valueDependents = null; */

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  Build a parse tree for the current expression using PtParser.
     *  Do nothing if a parse tree already exists.
     */
    private void _buildParseTree() throws IllegalActionException {
        if (_parseTree != null) {
            return;
        }
        if (_parser == null) {
            _parser = new PtParser(this);
        }
        _parseTree = _parser.generateParseTree(_currentExpression, getScope());
        return;
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

    /*  Destroy the current parse tree and mark all value dependents
     *  as needing to be evaluated.
     */
    private void _destroyParseTree() {
        if (_parseTree != null) {
            _clearDependencies(_parseTree);
            _parseTree = null;
        }
        if (_currentExpression != null) {
            _needsEvaluation = true;
        }
        _notifyValueDependents();
    }

    /*  Set the token value and type of the variable.
     *  If the type of the specified token is incompatible with specified
     *  absolute type constraints (i.e. those that can be checked), then
     *  throw an exception.  It is converted to the type given by
     *  setTypeEquals() if necessary and possible. If the argument is null,
     *  then no type checks are done, and the contents of the variable is set
     *  to null.
     *  @param newToken The new value of the variable.
     *  @exception IllegalActionException If the token type is not
     *   compatible with specified constraints, or if you are attempting
     *   to set to null a variable that has value dependents.
     */
    private void _setToken(Token newToken) throws IllegalActionException {
        if (newToken == null) {
            if (_valueDependents != null && !_valueDependents.isEmpty()) {
                throw new IllegalActionException(this,
                        "Cannot set contents to null because there " +
                        "are variables that depend on its value.");
            }
            _token = null;
            _needsEvaluation = false;

	    // set _varType
	    if (_declaredType instanceof BaseType) {
	    	_varType = _declaredType;
	    } else {
		// _varType = _declaredType
		((StructuredType)_varType).reset();
	    }
        } else {
	    if (_declaredType.isCompatible(newToken)) {
		newToken = _declaredType.convert(newToken);
	    } else {
                throw new IllegalActionException(this, "Variable._setToken: " +
		    "Cannot store a token of type " +
		    newToken.getType().toString() + ", which is incompatible" +
		    " with type " + _varType.toString());
	    }

	    // update _varType to the type of the new token.
	    if (_declaredType instanceof StructuredType) {
	    	((StructuredType)_varType).updateType(
				(StructuredType)newToken.getType());
	    } else {
		// _declaredType is a BaseType
		_varType = newToken.getType();
	    }

            // Check setTypeAtMost constraint.
            if (_typeAtMost != BaseType.NAT) {
                // Recalculate this in case the type has changed.
                Type tokenType = newToken.getType();
                int comparison
                        = TypeLattice.compare(tokenType, _typeAtMost);
                if ((comparison == CPO.HIGHER)
                        || (comparison == CPO.INCOMPARABLE)) {
                    // Incompatible type!
                    throw new IllegalActionException(this,
                    "Cannot store a token of type "
                    + tokenType.toString()
                    + ", which is not less than or equal to "
                    + _typeAtMost.toString());

                }
            }
            if (_noTokenYet) {
                // This is the first token stored in this variable.
                _initialExpression = _currentExpression;
                if (_currentExpression == null) {
                    // The token is being set directly.
                    _initialToken = newToken;
                }
                _noTokenYet = false;
            }
            _token = newToken;
            _needsEvaluation = false;
        }
    }

    /*  Set the token value and type of the variable, and notify the
     *  container that the value (and type, if appropriate) has changed.
     *  Also notify value dependents that they need to be re-evaluated,
     *  and notify any listeners that have been registered with
     *  addValueListener().
     *  If setTypeEquals() has been called, then attempt to convert
     *  the specified token into one of the appropriate type, if needed,
     *  rather than changing the type.
     *  @param newToken The new value of the variable.
     *  @exception IllegalActionException If the token type is not
     *   compatible with specified constraints, or if you are attempting
     *   to set to null a variable that has value dependents.
     */
    private void _setTokenAndNotify(Token newToken)
            throws IllegalActionException {

        // Save to restore in case the change is rejected.
        Token oldToken = _token;
        Type oldVarType = _varType;
	if (_varType instanceof StructuredType) {
	    oldVarType = (Type)((StructuredType)_varType).clone();
	}
        boolean oldNoTokenYet = _noTokenYet;
        String oldInitialExpression = _initialExpression;
        Token oldInitialToken = _initialToken;

        try {
            _setToken(newToken);
            _notifyValueDependents();
            NamedObj container = (NamedObj)getContainer();
            if (container != null) {
                if( !oldVarType.isEqualTo(_varType) &&
					oldVarType != BaseType.NAT) {
                    container.attributeTypeChanged(this);
                }
                container.attributeChanged(this);
            }
            if (_valueListeners != null) {
                Iterator listeners = _valueListeners.iterator();
                while (listeners.hasNext()) {
                    ValueListener listener = (ValueListener)listeners.next();
                    listener.valueChanged(this);
                }
            }
        } catch (IllegalActionException ex) {
            // reverse the changes
            _token = oldToken;
	    if (_varType instanceof StructuredType) {
                ((StructuredType)_varType).updateType(
						(StructuredType)oldVarType);
	    } else {
		_varType = oldVarType;
	    }
            _noTokenYet = oldNoTokenYet;
            _initialExpression = oldInitialExpression;
            _initialToken = oldInitialToken;
            throw ex;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Stores the expression used to set this variable. It is null if
    // the variable was set from a token.
    private String _currentExpression = null;

    // Used to check for dependency loops among variables.
    private transient boolean _dependencyLoop = false;

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
    private ptolemy.data.Token _initialToken;

    // Stores the variables whose scope contains this variable.
    private LinkedList _scopeDependents = null;

    /** Stores the variables whose expression references this variable. */
    private LinkedList _valueDependents = null;

    // The variables this variable may reference in its expression.
    // The list is cached.
    private NamedList _scope = null;
    private long _scopeVersion = -1;

    // Stores the variables added to the scope of this variable.
    private NamedList _scopeVariables = null;

    // Stores the Class object which represents the type of this variable.
    private Type _varType = BaseType.NAT;

    // The parser used by this variable to parse expressions.
    private PtParser _parser;

    // If the variable was last set from an expression, this stores
    // the parse tree for that expression.
    private ASTPtRootNode _parseTree;

    // The token contained by this variable.
    private ptolemy.data.Token _token;

    // Type constraints.
    private List _constraints = new LinkedList();

    // The type set by setTypeEquals(). If _declaredType is not BaseType.NAT,
    // the type of this Variable is fixed to that type.
    private Type _declaredType = BaseType.NAT;

    // If setTypeAtMost() has been called, then the type bound is stored here.
    private Type _typeAtMost = BaseType.NAT;

    // Reference to the inner class that implements InequalityTerm.
    TypeTerm _typeTerm = null;

    // Listeners for changes in value.
    private List _valueListeners;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private class TypeTerm implements InequalityTerm {

	// Pass the variable reference in the constructor so it can be
	// returned by getAssociatedObject().
	private TypeTerm(Variable var) {
	    _variable = var;
	}

	///////////////////////////////////////////////////////////////
	////                       public inner methods            ////

	/** Disallow the value of this term to be changed.
	 */
	public void fixValue() {
	    _valueFixed = true;
	    Object value = getValue();
	    if (value instanceof StructuredType) {
	    	((StructuredType)value).fixType();
	    }
	}

	/** Return this Variable.
	 *  @return A Variable.
	 */
	public Object getAssociatedObject() {
	    return _variable;
	}

	/** Return the type of this Variable.
	 */
	public Object getValue() {
	    try {
	        return getType();
	    } catch (IllegalActionException ex) {
		throw new InternalErrorException("Variable " +
		"TypeTerm.getValue(): Cannot get type. " + ex.getMessage());
	    }
        }

        /** Return this TypeTerm in an array if this term represent
	 *  a type variable. This term represents a type variable
	 *  if the type of this variable is not set through setTypeEquals().
         *  If the type of this variable is set, return an array of size zero.
	 *  @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
	    if (isSettable()) {
	    	InequalityTerm[] result = new InequalityTerm[1];
	    	result[0] = this;
	    	return result;
	    }
	    return (new InequalityTerm[0]);
        }

	/** Reset the variable part of this type to te specified type.
	 *  @param e A Type.
	 *  @exception IllegalActionException If the type is not settable,
	 *   or the argument is not a Type.
	 */
	public void initialize(Object e)
		throws IllegalActionException {
	    if ( !isSettable()) {
		throw new IllegalActionException("TypeTerm.initialize: " +
		    "The type is not settable.");
	    }

	    if ( !(e instanceof Type)) {
		throw new IllegalActionException("TypeTerm.initialize: " +
		    "The argument is not a Type.");
	    }

	    if (_declaredType == BaseType.NAT) {
		_varType = BaseType.NAT;
	    } else {
		// _declaredType is a StructuredType
		((StructuredType)_varType).reset();
	    }
	}

        /** Test if the type of this variable is fixed. The type is fixed if
	 *  setTypeEquals() is called with an argument that is not
	 *  BaseType.NAT, or the user has set a non-null expression or token
	 *  into this variable.
         *  @return True if the type of this variable is set;
	 *   false otherwise.
         */
        public boolean isSettable() {
	    // return ( !_declaredType.isConstant());

	    if (_token != null || _currentExpression != null ||
		_declaredType.isConstant() || _valueFixed) {
		return false;
	    }
	    return true;
        }

        /** Check whether the current type of this term is acceptable,
         *  and return true if it is.  A type is acceptable
         *  if it represents an instantiable object.
         *  @return True if the current type is acceptable.
         */
        public boolean isValueAcceptable() {
	    try {
            	if (getType().isInstantiable()) {
                    return true;
                }
                return false;
	    } catch (IllegalActionException ex) {
		throw new InternalErrorException("Variable " +
		"TypeTerm.isValueAcceptable(): Cannot get type. " +
		ex.getMessage());
	    }
        }

        /** Set the type of this variable.
         *  @exception IllegalActionException If this type is not settable,
	 *   or the argument is not a substitution instance of this type.
         */
        public void setValue(Object e) throws IllegalActionException {
	    if ( !isSettable()) {
	    	throw new IllegalActionException("TypeTerm.setValue: The " +
		    "type is not settable.");
	    }

	    if ( !_declaredType.isSubstitutionInstance((Type)e)) {
	    	throw new IllegalActionException("TypeTerm.setValue: The " +
		    "argument is not a substitution instance of the type " +
		    "of this variable.");
	    }

	    if (_declaredType == BaseType.NAT) {
		_varType = (Type)e;
	    } else {
		// _declaredType is a StructuredType
		((StructuredType)_varType).updateType((StructuredType)e);
	    }
        }

        /** Override the base class to give a description of the variable
         *  and its type.
         *  @return A description of the variable and its type.
         */
        public String toString() {
	    try {
                return "(" + _variable.toString() + ", " + getType() + ")";
	    } catch (IllegalActionException ex) {
		throw new InternalErrorException("Variable " +
		"TypeTerm.toString(): Cannot get type. " + ex.getMessage());
	    }
        }

	/** Allow the value of this term to be changed, if this term is a
	 *  variable.
	 */
	public void unfixValue() {
	    _valueFixed = false;
	    Object value = getValue();
	    if (value instanceof StructuredType) {
		((StructuredType)value).unfixType();
	    }
	}

        ///////////////////////////////////////////////////////////////
        ////                       private inner variable          ////

        private Variable _variable = null;
	private boolean _valueFixed = false;
    }
}
