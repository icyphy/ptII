/* A Variable is an Attribute that contains a token and can be referenced
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)

*/

package ptolemy.data.expr;

import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.Typeable;
import ptolemy.data.TypeLattice;
import ptolemy.data.TypeConstant;
import ptolemy.graph.*;
import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// Variable
/**
A Variable is an Attribute that contains a token, and can be set by an
expression that can refer to other variables.
<p>
A variable can be given a token or an expression as its value. To create
a variable with a token, either call the appropriate constructor, or create
the variable with the appropriate container and name, and then call
setToken(). To set the value from an expression, call setExpression().
The expression is not actually evaluated until you call getToken().
If the expression string is null or empty, then getToken() will return
null.
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
added to the scope of this variable before the expression is evaluated
(i.e., before getToken() is called). Otherwise, getToken() will throw
an exception.  By default, all variables
contained by the same container, and those contained by the container's
container, are in the scope of this variable. Thus, in the above,
all three variables are in each other's scope because they belong
to the same container.
<p>
If a variable is referred
to by expressions of other variables, then the name of the variable must be a
valid identifier as defined by the Ptolemy II expression language syntax.
<p>
A variable is a Typeable object. Constraints on its type can be
specified relative to other Typeable objects (as inequalities on the types).
The type of the variable can be specified in a number of ways, all of
which require the type to be consistent with the specified constraints
(or an exception will be thrown):
<ul>
<li> It can be set directly by a call to setTypeEquals(). If this call occurs
after the variable has a value, then the specified type must be compatible
with the value.  Otherwise, an exception will be thrown. Type resolution
will not change the type set through setTypeEquals() unless the argument
of that call is null. If this method is not called, or called with a null
argument, type resolution will resolve the variable type according to
all the type constraints.
Note that when calling setTypeEquals() with a non-null argument while
the variable already constrains a non-null token, the argument must
be a type no less than then the type of the contained token. To set type
of the variable lower than the type of the currently contained token,
setToken() must be called with a null argument before setTypeEquals().
<li> Setting the value of the variable to a non-null token constrains the
variable type to be no less than the type of the token. This constraint
will be used in type resolution, together with other constraints.
<li> The type is also constrained when an expression is evaluated.
The variable type must be no less than the type of the token the
expression is evaluated to.
<li>
If the variable does not yet have a value, then the type of a variable may
be determined by type resolution. In this case, a set of type constraints is
derived from the expression of the variable (which presumably has not yet
been evaluated, or the type would be already determined). Additional type
constraints can be added by calls to the setTypeAtLeast() and
setTypeSameAs() methods.
</ul>
Note that subject to specified constraints, the type of a variable can
be changed at any time.  Some of the type constraints, however, are not
verified until type resolution is done.  If type resolution is not done,
then these constraints are not enforced.  Type resolution is normally
done by the Manager that executes a model.
<p>
The token returned by getToken() is always an instance of the class given
by the getType() method.  This is not necessarily the same as the class
of the token that was inserted via setToken().  It might be a distinct
type if the contained token can be converted losslessly into one of the
type given by getType().  In rare circumstances, you may need to directly
access the contained token without any conversion occurring.  To do this,
use getContainedToken().
<p>
A variable can also be reset. If the variable was originally set from a
token, then this token is placed again in the variable, and the type of the
variable is set to equal that of the token. If the variable
was originally given an expression, then this expression is placed again
in the variable (but not evaluated), and the type is reset to null.
The type will be determined when the expression is evaluated or when
type resolution is done.

@author Neil Smyth, Xiaojun Liu, Edward A. Lee, Yuhong Xiong
@version $Id$

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
     *  derived class) are ignored.
     *  @param variables An enumeration of variables to be added to scope.
     */
    public void addToScope(Enumeration variables) {
        while (variables.hasMoreElements()) {
            Object var = variables.nextElement();
            if (var instanceof Variable) {
                addToScope((Variable)var);
            }
        }
    }

    /** Add the variables listed in the argument to the scope of this
     *  variable. If any of the variables bears the same name as one
     *  already in the scope, then it will shadow the one in the scope.
     *  Items in the list that are not instances of the class Variable (or a
     *  derived class) are ignored.
     *  @deprecated
     *  @param varList A list of variables to be added to scope.
     */
    public void addToScope(VariableList varList) {
        // FIXME: Remove this method when fsm no longer depends on it.
        if (varList == null) {
            return;
        }
        addToScope(varList.getVariables());
	varList._addDependent(this);
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

    /** Clone the variable.  This creates a new variable containing the
     *  same token (if the value was set with setToken()) or the same
     *  (unevaluated) expression, if the expression was set with
     *  setExpression().  The clone also recalls the same initial
     *  expression or initial token, so that reset() on the clone behaves
     *  as reset() on the original. The list of variables added to the scope
     *  is not cloned; i.e., the clone has an empty scope.
     *  Also, the clone will not have any type constraints.
     *  @param The workspace in which to place the cloned variable.
     *  @exception CloneNotSupportedException Thrown only in derived classes.
     *  @see java.lang.Object#clone()
     *  @return The cloned variable.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Variable newvar = (Variable)super.clone(ws);
        newvar._scopeVariables = null;
        newvar._castToken = null;
        newvar._convertMethod = null;
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
        // _varType is preserved.
        newvar._parser = null;
        newvar._parseTree = null;

	newvar._constraints = new LinkedList();
        newvar._typeTerm = null;
        return newvar;
    }

    /** Get the token contained by this variable. It will be null if
     *  neither an expression nor a token has been set.
     *  The token is not converted to the type of this variable.
     *  The contained token is either a token that has been placed
     *  directly into this variable using setToken(), or the result
     *  of evaluating the current expression.  Note that evaluating
     *  this expression can result in an exception being thrown.
     *  @return The token contained by this variable, or null if there
     *   is none.
     *  @exception IllegalExpressionException If the expression cannot
     *   be parsed or cannot be evaluated.  This is a runtime exception
     *   so it need not be declared explicitly.
     */
    // FIXME: This should not throw a runtime exception!
    public ptolemy.data.Token getContainedToken()
            throws IllegalExpressionException {
        _evaluate();
        return _token;
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

    /** Obtain a NamedList of the variables that the value of this
     *  variable can depend on. These include the variables added to
     *  the scope of this variable by the addToScope()
     *  methods, and the variables in the same
     *  NamedObj and those one level up in the hierarchy.
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
                Enumeration level1 = container.getAttributes();
                Attribute var = null;
                while (level1.hasMoreElements()) {
                    // add the variables in the same NamedObj to _scope,
                    // excluding this
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

    /** Get the token contained by this variable, converted to the type of
     *  this variable if necessary. The result will be null if neither
     *  an expression nor a token has been set. Conversion is done if the
     *  the contained token is not an instance of the class of the type
     *  of this variable, or of some derived class.  This conversion is
     *  lossless. Notice the evaluation of the expression can trigger
     *  an exception if the expression is not valid.
     *  @return The token contained by this variable converted to the
     *   type of this variable, or null if there is none.
     *  @exception IllegalExpressionException If the expression cannot
     *   be parsed or cannot be evaluated.  This is a runtime exception
     *   so it need not be declared explicitly.
     */
    // FIXME: This should not be a runtime exception.
    public ptolemy.data.Token getToken() throws IllegalExpressionException {
        _evaluate();
        if (_token == null) {
            return null;
        }
        if (_varType.isInstance(_token)) {
            return _token;
        }
        if (_castToken != null) {
            return _castToken;
        }
        // Need to convert the token contained by this variable to
        // the type of this variable.
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

    /** Get the type of this variable. It is null if the type has not set
     *  by setTypeEquals(), no token has been set by setToken(), and no
     *  expression has been set by setExpression().  If an expression has
     *  been set, then calling this method causes this expression to be
     *  evaluated, which can cause an exception to be thrown.
     *  @return The type of this variable.
     *  @exception IllegalExpressionException If the expression cannot
     *   be parsed or cannot be evaluated.  This is a runtime exception
     *   so it need not be declared explicitly.
     */
    // FIXME: This should not be a runtime exception.
    public Class getType() throws IllegalExpressionException {
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
     *  Any item in the enumeration that is not an instance of Variable
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
     *  Note also that if the removed variables is shadowing
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

    /** Remove the variables in the argument from the scope of this variable.
     *  @deprecated
     *  @param varList A list of variables to be removed from scope.
     */
    public void removeFromScope(VariableList varList) {
        // FIXME: Remove this when sc (fsm) no longer depends on it.
        removeFromScope(varList.getVariables());
	varList._removeDependent(this);
    }

    /** Reset the variable to its initial value. If the variable was
     *  originally set from a token, then this token is placed again
     *  in the variable, and the type of the variable is set to equal
     *  that of the token. If the variable was originally given an
     *  expression, then this expression is placed again in the variable
     *  (but not evaluated), and the type is reset to null.
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
            if (_scopeVariables != null) {
                _scopeVariables.removeAll();
            }
        }
    }

    /** Set the expression of this variable. Evaluation is deferred until
     *  the value of the variable is accessed by getToken().  The
     *  container is not notified of the change until then.  If you need
     *  to notify the container right away, then call getToken().  If the
     *  argument is null, then getToken() will return null.
     *  @param expr The expression for this variable.
     */
    public void setExpression(String expr) {
        if (expr == null) {
            _token = null;
            _castToken = null;
            _needsEvaluation = false;
        } else {
            _needsEvaluation = true;
        }
        _currentExpression = expr;
        _destroyParseTree();
    }

    /** Put a new token in this variable. If an expression had been
     *  previously given using setExpression(), then that expression
     *  is forgotten.
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
     */
    public void setTypeAtLeast(Typeable lesser) {
        Inequality ineq = new Inequality(lesser.getTypeTerm(),
                this.getTypeTerm());
	_constraints.insertLast(ineq);
    }

    /** Set a type constraint that the type of this object be at most
     *  the specified value.
     *  @exception IllegalActionException If the type of this object
     *   already violates this constraint. Also
     *   thrown if the argument is not an instantiable type
     *   in the type lattice.
     */
    public void setTypeAtMost(Class type) throws IllegalActionException {
        if (!TypeLattice.isInstantiableType(type)) {
            throw new IllegalActionException(this, "setTypeAtMost(): "
                    + "the argument " + type
                    + " is not an instantiable type in the type lattice.");
        }

        if (_token != null) {
            int typeInfo = TypeLattice.compare(_token.getClass(), type);
            if ((typeInfo == CPO.HIGHER) || (typeInfo == CPO.INCOMPARABLE)) {
                throw new IllegalActionException(this, "setTypeAtMost(): "
                        + "the currently contained token " + _token.toString()
                        + " is not less than the desired bounding type "
                        + type.toString());
            }
        }

        Inequality ineq = new Inequality(this.getTypeTerm(),
                new TypeConstant(type));
	_constraints.insertLast(ineq);
    }

    /** Set a type constraint that the type of this object equal
     *  the specified value. This is an absolute type constraint (not
     *  relative to another Typeable object), so it is checked every time
     *  the value of the variable is set by setToken() or by evaluating
     *  an expression.
     *  To remove the type constraint, call this method will a null argument.
     *  @exception IllegalActionException If the type of this object
     *   already violates this constraint, in that the currently contained
     *   token cannot be converted losslessly to the specified type. Also
     *   thrown if the argument is not an instantiable type
     *   in the type lattice.
     */
    public void setTypeEquals(Class type) throws IllegalActionException {
        if (type == null) {
            _declaredType = null;
            return;
        }
        if (!TypeLattice.isInstantiableType(type)) {
            throw new IllegalActionException(this, "setTypeEquals(): "
                    + "the argument " + type
                    + " is not an instantiable type in the type lattice.");
        }
        if (_token != null) {
            int typeInfo = TypeLattice.compare(_token.getClass(), type);
            if ((typeInfo == CPO.HIGHER) || (typeInfo == CPO.INCOMPARABLE)) {
                throw new IllegalActionException(this, "setTypeEquals(): "
                        + "the currently contained token " + _token.toString()
                        + " cannot be losslessly converted to the desired type "
                        + type.toString());
            }
        }
        _declaredType = type;
        _varType = type;
        _convertMethod = null;
        _castToken = null;
    }


    /** Constrain the type of this variable to be the same as the
     *  type of the specified object.
     *  @param equal A Typeable object.
     */
    public void setTypeSameAs(Typeable equal) {
        Inequality ineq = new Inequality(this.getTypeTerm(),
                equal.getTypeTerm());
	_constraints.insertLast(ineq);
	ineq = new Inequality(equal.getTypeTerm(),
                this.getTypeTerm());
	_constraints.insertLast(ineq);
    }

    /** Return a string representation of the current variable value.
     *  @return A string representing the class and the current token.
     */
    public String toString() {
        return super.toString() + " " + getToken();
    }

    /** Return the type constraints of this variable.
     *  The constraints include the ones explicitly set to this Variable,
     *  plus the constraint that the type of this Variable must be no less
     *  than the type of the contained token or expression, if the contained
     *  token or expression is not null.
     *  The constraints are an enumeration of inequalities.
     *  @return an enumeration of Inequality.
     *  @see ptolemy.graph.Inequality
     */
    public Enumeration typeConstraints() {
        // FIXME: before we can generate constraints from the expressions
	// directly, try to evaluate the expression to get a token.
	// if the evaluate throws an exception, ignore the constraints
	// between the expression and the type of this variable.
	Token containedToken = null;
	try {
	    containedToken = getContainedToken();
	} catch (Exception ex) {
	    // FIXME: before the exception mechanism in the expr
	    // package is fixed, catch all exceptions above.

	    // do nothing, not generating constraints.
	}
	if (containedToken == null) {
	    return _constraints.elements();
	}

	LinkedList result = new LinkedList();
	result.appendElements(_constraints.elements());

	TypeConstant tokenType =
            new TypeConstant(containedToken.getClass());
	Inequality ineq = new Inequality(tokenType, this.getTypeTerm());
	result.insertLast(ineq);

	return result.elements();
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
     *  as the current type of the variable, and cannot be converted
     *  to the current type of the variable, then the type of this variable
     *  is changed.  In this case, the attributeTypeChanged() method of
     *  the container is called.  It is up to the container to check
     *  that any type constraints are valid.
     *  <p>
     *  Part of this method is read-synchronized on the workspace.
     *
     *  @exception IllegalExpressionException If the expression cannot
     *   be parsed or cannot be evaluated.  This is a runtime exception
     *   so it need not be declared explicitly.
     */
    protected void _evaluate() throws IllegalExpressionException {
        if (!_needsEvaluation) return;
        if (_currentExpression == null || _currentExpression.equals("")) {
            try {
                _setToken(null);
            } catch (IllegalActionException ex) {
                throw new IllegalExpressionException(ex.getMessage());
            }
            return;
        }
        // If _dependencyLoop is true, then this call to evaluate() must
        // have been triggered by evaluating the expression of this variable,
        // which means that the expression directly or indirectly refers
        // to itself.
	if (_dependencyLoop) {
            _dependencyLoop = false;
            throw new IllegalExpressionException("Found dependency loop "
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
            throw new IllegalExpressionException(ex.getMessage());
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
            Enumeration vars = _valueDependents.elements();
            while (vars.hasMoreElements()) {
                Variable var = (Variable)vars.nextElement();
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
     *  throw an exception.  If the argument is null,
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
                        "Cannot set contents to null because there are variables " +
                        "that depend on its value.");
            }
            _token = null;
            _needsEvaluation = false;
            _castToken = null;
            return;
        }
        Class tokenType = newToken.getClass();
        if (_varType != null) {
            // Type has been set before.
            // Check to see whether new type can be converted to old.
            int typeInfo = TypeLattice.compare(_varType, tokenType);
            if ((typeInfo == CPO.HIGHER) || (typeInfo == CPO.SAME)) {
                _token = newToken;
                _needsEvaluation = false;
                _castToken = null;
                return;
            }
        }
        // Type has not been set before, or the new type cannot be converted
        // to the old.  If an equality constraint has been set, trouble!
        if (_declaredType != null) {
            // Incompatible type!
            throw new IllegalActionException(this, "Cannot store a token of "
                    + "type "
                    + tokenType.getName() + ", which is incompatible with type "
                    + _varType.getName());
        }

        _varType = tokenType;
        _convertMethod = null;

        _castToken = null;

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

    /*  Set the token value and type of the variable, and notify the
     *  container that the value (and type, if appropriate) has changed.
     *  Also notify value dependents that they need to be re-evaluated.
     *  @param newToken The new value of the variable.
     *  @exception IllegalActionException If the token type is not
     *   compatible with specified constraints, or if you are attempting
     *   to set to null a variable that has value dependents.
     */
    private void _setTokenAndNotify(Token newToken)
            throws IllegalActionException {

        Token oldToken = _token;
        Token oldCastToken = _castToken;
        Class oldVarType = _varType;
        java.lang.reflect.Method oldConvertMethod = _convertMethod;
        boolean oldNoTokenYet = _noTokenYet;
        String oldInitialExpression = _initialExpression;
        Token oldInitialToken = _initialToken;

        try {
            _setToken(newToken);
            _notifyValueDependents();
            NamedObj container = (NamedObj)getContainer();
            if (container != null) {
                if(oldVarType != _varType && oldVarType != null) {
                    container.attributeTypeChanged(this);
                }
                container.attributeChanged(this);
            }
        } catch (IllegalActionException ex) {
            // reverse the changes
            _token = oldToken;
            _castToken = oldCastToken;
            _varType = oldVarType;
            _convertMethod = oldConvertMethod;
            _noTokenYet = oldNoTokenYet;
            _initialExpression = oldInitialExpression;
            _initialToken = oldInitialToken;
            throw ex;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

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
    private Class _varType;

    // The parser used by this variable to parse expressions.
    private PtParser _parser;

    // If the variable was last set from an expression, this stores
    // the parse tree for that expression.
    private ASTPtRootNode _parseTree;

    // The token contained by this parameter.
    private ptolemy.data.Token _token;

    // Type constraints.
    private LinkedList _constraints = new LinkedList();

    // The type set by setTypeEquals(). If _declaredType is not null,
    // the type of this Variable is fixed to that type.
    private Class _declaredType = null;

    // Reference to the inner class that implements InequalityTerm.
    TypeTerm _typeTerm = null;


    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private class TypeTerm implements InequalityTerm {

	// pass the variable reference in the constructor so it can be
	// returned by getAssociatedObject().
	private TypeTerm(Variable var) {
	    _variable = var;
	}

	///////////////////////////////////////////////////////////////
	////                       public methods                  ////

	/** Return this Variable.
	 *  @return A Variable.
	 */
	public Object getAssociatedObject() {
	    return _variable;
	}

	/** Return the type of this TypedIOPort.
	 */
	public Object getValue() {
	    return getType();
        }

        /** Return this TypeTerm in an array if this term represent
	 *  a type variable. This term represents a type variable
	 *  if the type of this Variable is not set through setTypeEquals().
         *  If the type of this Variable is set, return an array of size zero.
	 *  @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
	    if (_declaredType == null) {
	    	InequalityTerm[] result = new InequalityTerm[1];
	    	result[0] = this;
	    	return result;
	    }
	    return (new InequalityTerm[0]);
        }

        /** Test if the type of this Variable is set thought
	 *  setTypeEquals().
         *  @return True if the type of this Variable is set;
	 *   false otherwise.
         */
        public boolean isSettable() {
	    if (_declaredType == null) {
		return true;
	    }
	    return false;
        }

        /** Check whether the current type of this term is acceptable,
         *  and return true if it is.  A type is acceptable
         *  if it represents an instantiable object.
         *  @return True if the current type is acceptable.
         */
        public boolean isValueAcceptable() {
            if (TypeLattice.isInstantiableType(getType())) {
                return true;
            }
            return false;
        }

        /** Set the type of this port if it is not set through
	 *  setTypeEquals().
         *  @exception IllegalActionException If the type is already set
	 *   through setTypeEquals().
         */
        public void setValue(Object e) throws IllegalActionException {
	    if (_declaredType == null) {
		_varType = (Class)e;
		return;
	    }

	    throw new IllegalActionException("TypeTerm.setValue: Cannot set "
                    + "the value of a type constant.");
        }

        ///////////////////////////////////////////////////////////////
        ////                       private variable                ////

        private Variable _variable = null;
    }
}
