/* A variable is an attribute that contains a token and can be referenced
in expressions.

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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.data.expr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.StructuredType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeConstant;
import ptolemy.data.type.TypeLattice;
import ptolemy.data.type.Typeable;
import ptolemy.graph.CPO;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedList;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;

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
The expression is not actually evaluated until you call getToken(),
getType(). By default, it is also evaluated when you call validate(),
unless you have called setLazyValidation(true), in which case it will only
be evaluated if there are other variables that depend on it and those
have not had setLazyValidation(true) called.
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
is not evaluated until getToken() is called.  Equivalently, we
could have called, for example,
<pre>
   v3.validate();
</pre>
This will force <code>v3</code> to be evaluated,
and also <code>v1</code> and <code>v2</code>
to be evaluated.
<p>
There is a potentially confusing subtlety.  In the above code,
before the last line is executed, the expression for <code>v3</code>
has not been evaluated, so the dependence that <code>v3</code> has
on <code>v1</code> and <code>v2</code> has not been recorded.
Thus, if we call
<pre>
   v1.validate();
</pre>
before <code>v3</code> has ever been evaluated, then it will <i>not</i>
trigger an evaluation of <code>v3</code>.  Because of this, we recommend
that user code call validate() immediately after calling
setExpression().
<p>
If the expression string is null or empty,
or if no value has been specified, then getToken() will return null.
<p>
The expression can reference variables that are in scope before the
expression is evaluated (i.e., before getToken() or validate() is called).
Otherwise, getToken() will throw an exception. All variables
contained by the same container, and those contained by the container's
container, are in the scope of this variable. Thus, in the above,
all three variables are in each other's scope because they belong
to the same container. If there are variables in the scope with the
same name, then those lower in the hierarchy shadow those that are higher.
An instance of ScopeExtendingAttribute can also be used to
aggregate a set of variables and add them to the scope.
<p>
If a variable is referred
to by expressions of other variables, then the name of the variable must be a
valid identifier as defined by the Ptolemy II expression language syntax.
A valid identifier starts with a letter or underscore, and contains
letters, underscores, numerals, dollar signs ($),
at signs (@), or pound signs (#).
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
<li> If the variable already has a value (set by setToken() or
     setExpression()) when you set the static type constraint, then
     the value must satisfy the type constraint; and
<li> If after setting a static type constraint you give the token
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
A variable by default has no MoML description (MoML is an XML modeling markup
language).  Thus, a variable contained by a named object is not
persistent, in that if the object is exported to a MoML file, the
variable will not be represented.  If you prefer that the variable
be represented, then you should use the derived class Parameter instead
or call setPersistent(true).
<p>
A variable is also normally not settable by casual users from the user
interface.  This is because, by default, getVisibility() returns EXPERT.
The derived class Parameter is fully visible by default.
<p>
In addition, this class provides as a convenience a "string mode."
If the variable is in string mode, then when setting the value of
this variable, the string that you pass to setExpression(String)
is taken to be literally the value of the instance of StringToken
that represents the value of this parameter. It is not necessary
to enclose it in quotation marks (and indeed, if you do, the quotation
marks will become part of the value of the string).  In addition,
the type of this parameter will be set to string. In addition,
getToken() will never return null; if the value of the string
has never been set, then an instance of StringToken is returned
that has an empty string as its value. A parameter is
in string mode if either setStringMode(true) has been called or
it contains an attribute named "_stringMode".
<p>
In string mode, the value passed to setExpression(String) may contain
references to other variables in scope using the syntax $id,
${id} or $(id).  The first case only works if the id consists
only of alphanumeric characters and/or underscore, and if the
character immediately following the id is not one of these.
To get a simple dollar sign, use $$.

@author Neil Smyth, Xiaojun Liu, Edward A. Lee, Yuhong Xiong
@version $Id$
@since Ptolemy II 0.2

@see ptolemy.data.Token
@see ptolemy.data.expr.PtParser
@see ptolemy.data.expr.Parameter
@see ScopeExtendingAttribute
@see #setPersistent(boolean)
*/

public class Variable extends Attribute
    implements Typeable, Settable, ValueListener {

    /** Construct a variable in the default workspace with an empty string
     *  as its name. The variable is added to the list of objects in the
     *  workspace. Increment the version number of the workspace.
     */
    public Variable() {
        super();
        setPersistent(false);
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
        setPersistent(false);
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
        setPersistent(false);
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
        setPersistent(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a listener to be notified when the value of this variable changes.
     *  @param listener The listener to add.
     */
    public synchronized void addValueListener(ValueListener listener) {
        if (_valueListeners == null) {
            _valueListeners = new LinkedList();
        }
        if (!_valueListeners.contains(listener)) {
            _valueListeners.add(listener);
        }
    }

    /** Clone the variable.  This creates a new variable containing the
     *  same token (if the value was set with setToken()) or the same
     *  (unevaluated) expression, if the expression was set with
     *  setExpression().  The list of variables added to the scope
     *  is not cloned; i.e., the clone has an empty scope.
     *  The clone has the same static type constraints (those given by
     *  setTypeEquals() and setTypeAtMost()), but none of the dynamic
     *  type constraints (those relative to other variables).
     *  @param workspace The workspace in which to place the cloned variable.
     *  @exception CloneNotSupportedException Not thrown in this base class.
     *  @see java.lang.Object#clone()
     *  @return The cloned variable.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        Variable newObject = (Variable)super.clone(workspace);

        // _currentExpression and _initialExpression are preserved in clone
        if (_currentExpression != null) {
            newObject._needsEvaluation = true;
        }
        newObject._dependencyLoop = false;
        // _noTokenYet and _initialToken are preserved in clone
        newObject._parserScope = null;
        
        // Very subtle bug from missing this.
        // This bug only showed up when using MoML classes (e.g.
        // SmoothedPeriodogram actors, which are composite actors
        // in the library), because these are cloned when copied.
        newObject._variablesDependentOn = null;

        // set _declaredType and _varType
        if (_declaredType instanceof StructuredType &&
                !_declaredType.isConstant()) {
            newObject._declaredType =
                (Type)((StructuredType)_declaredType).clone();
            newObject._varType = newObject._declaredType;
        }
        // _typeAtMost is preserved
        newObject._parseTree = null;
        newObject._parseTreeValid = false;

        newObject._constraints = new LinkedList();
        newObject._typeTerm = null;
        return newObject;
    }

    /** If setTypeEquals() has been called, then return the type specified
     *  there. Otherwise, return BaseType.UNKNOWN.
     *  @return The declared type of this variable.
     *  @see #setTypeEquals(Type)
     *  @see BaseType
     */
    public Type getDeclaredType() {
        return _declaredType;
    }

    /** Return the list of identifiers referenced by the current expression.
     *  @return A set of Strings.
     *  @exception IllegalActionException If the expression cannot be parsed.
     */
    public Set getFreeIdentifiers() throws IllegalActionException {
        if(_currentExpression == null) {
            return Collections.EMPTY_SET;
        }
        try {
            workspace().getReadAccess();
            _parseIfNecessary();
            ParseTreeFreeVariableCollector collector =
                new ParseTreeFreeVariableCollector();
            return collector.collectFreeVariables(_parseTree);
        } finally {
            workspace().doneReading();
        }
    }

    /** Get the expression currently used by this variable. The expression
     *  is either the value set by setExpression(), or a string representation
     *  of the value set by setToken(), or an empty string if no value
     *  has been set.
     *  @return The expression used by this variable.
     */
    public String getExpression() {
        String value = _currentExpression;
        if (value == null) {
            ptolemy.data.Token token = null;
            try {
                token = getToken();
            } catch (IllegalActionException ex) {}
            if (token != null) {
                if (isStringMode()) {
                    value = ((StringToken)token).stringValue();
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

    /** Return a NamedList of the variables that the value of this
     *  variable can depend on.  These include other variables contained
     *  by the same container or any container that deeply contains
     *  this variable, as well as any variables in a ScopeExtendingAttribute
     *  contained by any of these containers.
     *  If there are variables with the same name in these various
     *  places, then they are shadowed as follows. A variable contained
     *  by the container of this variable has priority, followed
     *  by variables in a ScopeExtendingAttribute, followed by
     *  by a variable contained by the container of the container, etc.
     *  <p>
     *  Note that this method is an extremely inefficient to refer
     *  to the scope of a variable because it constructs a list containing
     *  every variable in the scope.  It is best to avoid calling it
     *  and instead just use the get() method of the VariableScope
     *  inner class.
     *  <p>
     *  This method is read-synchronized on the workspace.
     *  @return The variables on which this variable can depend.
     */
    public NamedList getScope() {
        try {
            workspace().getReadAccess();
            NamedList scope = new NamedList();
            NamedObj container = (NamedObj)getContainer();
            while (container != null) {
                Iterator level1 = container.attributeList().iterator();
                Attribute var = null;
                while (level1.hasNext()) {
                    // add the variables in the same NamedObj to scope,
                    // excluding this
                    var = (Attribute)level1.next();
                    if ((var instanceof Variable) && (var != this)) {
                        if (!_isLegalInScope((Variable)var)) {
                            continue;
                        }
                        try {
                            scope.append(var);
                        } catch (NameDuplicationException ex) {
                            // This occurs when a variable is shadowed by one
                            // that has been previously entered in the scope.
                        } catch (IllegalActionException ex) {
                            // This should not happen since we are dealing with
                            // variables which are Nameable.
                        }
                    }
                }
                level1 =
                    container.attributeList(ScopeExtender.class).iterator();
                while (level1.hasNext()) {
                    ScopeExtender extender = (ScopeExtender)level1.next();
                    Iterator level2 = extender.attributeList().iterator();
                    while (level2.hasNext()) {
                        // add the variables in the scope extender to scope,
                        // excluding this
                        var = (Attribute)level2.next();
                        if ((var instanceof Variable) && (var != this)) {
                            if (!_isLegalInScope((Variable)var)) {
                                continue;
                            }
                            try {
                                scope.append(var);
                            } catch (NameDuplicationException ex) {
                                // This occurs when a variable is shadowed by
                                // one that has been previously entered in the
                                // scope.
                            } catch (IllegalActionException ex) {
                                // This should not happen since we are dealing
                                // with variables which are Nameable.
                            }
                        }
                    }
                }

                container = (NamedObj)container.getContainer();
            }
            return scope;
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
        if (_isTokenUnknown) throw new UnknownResultException(this);
        // If the value has been set with an expression, then
        // reevaluate the token.
        if (_needsEvaluation) _evaluate();
        if (_token == null && isStringMode()) {
            _token = _EMPTY_STRING_TOKEN;
        }
        return _token;
    }

    /** Get the type of this variable. If a token has been set by setToken(),
     *  the returned type is the type of that token; If an expression has
     *  been set by setExpression(), and the expression can be evaluated, the
     *  returned type is the type the evaluation result. If the expression
     *  cannot be evaluated at this time, the returned type is the declared
     *  type of this Variable, which is either set by setTypeEquals(), or
     *  the default BaseType.UNKNOWN; If no token has been set by setToken(),
     *  no expression has been set by setExpression(), and setTypeEquals()
     *  has not been called, the returned type is BaseType.UNKNOWN.
     *  @return The type of this variable.
     */
    public Type getType() {
        try {
            if (_needsEvaluation) _evaluate();
            return _varType;
        } catch (IllegalActionException iae) {
            return _declaredType;
        }
    }

    /** Return an InequalityTerm whose value is the type of this variable.
     *  @return An InequalityTerm.
     */
    public InequalityTerm getTypeTerm() {
        if (_typeTerm == null) {
            _typeTerm = new TypeTerm();
        }
        return _typeTerm;
    }

    /** Get the visibility of this variable, as set by setVisibility().
     *  The visibility is set by default to EXPERT.
     *  @return The visibility of this variable.
     */
    public Settable.Visibility getVisibility() {
        return _visibility;
    }

    /** Mark this variable, and all variables that depend on it, as
     *  needing to be evaluated.  Remove this variable from being
     *  notified by the variables it used to depend on.  Then notify
     *  other variables that depend on this one that its value has
     *  changed. That notification is done by calling their valueChanged()
     *  method, which flags them as needing to be evaluated. This might be
     *  called when something in the scope of this variable changes.
     */
    public void invalidate() {
        if (_currentExpression != null) {
            _needsEvaluation = true;
        }
        if (_variablesDependentOn != null) {
            Iterator entries = _variablesDependentOn.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry)entries.next();
                Variable variable = (Variable)entry.getValue();
                variable.removeValueListener(this);
            }
            _variablesDependentOn.clear();
        }
        _notifyValueListeners();
    }

    /** Return <i>true</i> if the value of this variable is known, and
     *  false otherwise.  In domains with fixed-point semantics, such
     *  as SR, a variable that depends on a port value may be unknown
     *  at various points during the execution.
     *  @see #setUnknown(boolean)
     *  @return True if the value is known.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    public boolean isKnown() throws IllegalActionException {
        try {
            getToken();
        } catch (UnknownResultException ex) {
            return false;
        }
        return true;
    }

    /** Return true if this variable is lazy.  By default, a variable
     *  is not lazy.
     *  @return True if this variable is lazy.
     *  @see #setLazy(boolean)
     */
    public boolean isLazy() {
        return _isLazy;
    }
   
    /** Return true if this parameter is in string mode.
     *  @return True if this parameter is in string mode.
     *  @see #setStringMode(boolean)
     */
    public boolean isStringMode() {
        if (_isStringMode) {
            return true;
        } else {
            return (getAttribute("_stringMode") != null);
        }
    }

    /** Check whether the current type of this variable is acceptable.
     *  A type is acceptable if it represents an instantiable object.
     *  @return True if the current type is acceptable.
     */
    public boolean isTypeAcceptable() {
        if (getType().isInstantiable()) {
            return true;
        }
        return false;
    }

    /** Remove a listener from the list of listeners that is
     *  notified when the value of this variable changes.  If no such listener
     *  exists, do nothing.
     *  @param listener The listener to remove.
     */
    public synchronized void removeValueListener(ValueListener listener) {
        if (_valueListeners != null) {
            _valueListeners.remove(listener);
        }
    }

    /** Reset the variable to its initial value. If the variable was
     *  originally set from a token, then this token is placed again
     *  in the variable, and the type of the variable is set to equal
     *  that of the token. If the variable was originally given an
     *  expression, then this expression is placed again in the variable
     *  (but not evaluated), and the type is reset to BaseType.UNKNOWN.
     *  The type will be determined when the expression is evaluated or
     *  when type resolution is done. Note that if this variable is
     *  cloned, then reset on the clone behaves exactly as reset on
     *  the original.
     *  @deprecated This capability may be removed to simplify this class.
     *   It is not currently used in Ptolemy II, as of version 2.0.
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
            // Every variable that this may shadow in its new location
            // must invalidate all their dependents.
            _invalidateShadowedSettables(container);

            // This variable must still be valid.
            // NOTE: This has the side effect of validating everything
            // that depends on this variable. If the container is being
            // set to null, this may result in errors in variables
            // for which this is no longer in scope.  The error handling
            // mechanism has to handle this.
            // NOTE: This is too early for attributeChanged() to be called
            // since typically the public variable referring to an attribute
            // has not been set yet.
            validate();
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
        if (_debugging) {
            _debug("setExpression: " + expr);
        }
        if (expr == null || expr.trim().equals("")) {
            _token = null;
            _needsEvaluation = false;
            // set _varType
            if (_declaredType instanceof StructuredType) {
                ((StructuredType)_varType).initialize(BaseType.UNKNOWN);
            } else {
                _varType = _declaredType;
            }
        } else {
            _needsEvaluation = true;
        }
        boolean changed = (expr != null && !expr.equals(_currentExpression));
        _currentExpression = expr;
        _parseTree = null;
        _parseTreeValid = false;
        
        // Make sure the new value is exported in MoML.  EAL 12/03.
        if (changed) {
            _setModifiedFromClass();
        }

        _notifyValueListeners();
    }

    /** Specify whether this variable is to be lazy.  By default, it is not.
     *  A lazy variable is a variable that is not evaluated until its
     *  value is needed. Its value is needed when getToken() or
     *  getType() is called, but not necessarily when validate()
     *  is called. In particular, validate() has the effect
     *  only of setting a flag indicating that the variable needs to be
     *  evaluated, but the evaluation is not performed. Thus, although
     *  validate() returns, there is no assurance that the expression
     *  giving the value of the variable can be evaluated without error.
     *  The validate() method, however, will validate value dependents.
     *  If those are also lazy, then they will not be evaluated either.
     *  If they are not lazy however (they are eager), then evaluating them
     *  may cause this variable to be evaluated.
     *  <p>
     *  A lazy variable may be used whenever its value will be actively
     *  accessed via getToken() when it is needed, and its type will be
     *  actively accessed via getType(). In particular, the container
     *  does not rely on a call to attributeChanged() or
     *  attributeTypeChanged() to notify it that the variable value has
     *  changed. Those methods will not be called when the value of the
     *  variable changes due to some other variable value that it
     *  depends on changing because the new value will not be
     *  immediately evaluated.
     *  @param lazy True to make the variable lazy.
     *  @see #validate()
     *  @see NamedObj#attributeChanged(Attribute)
     *  @see NamedObj#attributeTypeChanged(Attribute)
     */
    public void setLazy(boolean lazy) {
        if (_debugging) {
            _debug("setLazy: " + lazy);
        }
        _isLazy = lazy;
    }

    /** Set a new parseTreeEvaluator.
     *  @param parseTreeEvaluator The new parseTreeEvaluator used by
     *  this variable.
     */
    public void setParseTreeEvaluator(ParseTreeEvaluator parseTreeEvaluator) {
        _parseTreeEvaluator = parseTreeEvaluator;
    }

    /** Specify whether this parameter should be in string mode.
     *  If the argument is true, then specify that the type of this
     *  parameter is string. Otherwise, specify that the type is
     *  unknown.  Note that it probably does not make sense to
     *  switch between string mode and not string mode after the
     *  variable has a value.
     *  @param stringMode True to put the parameter in string mode.
     *  @exception IllegalActionException If the current value of this
     *   parameter is incompatible with the resulting type.
     *  @see #isStringMode()
     */
    public void setStringMode(boolean stringMode)
            throws IllegalActionException {
        _isStringMode = stringMode;
        if (_isStringMode) {
            setTypeEquals(BaseType.STRING);
        } else {
            setTypeEquals(BaseType.UNKNOWN);
        }
    }

    /** Put a new token in this variable and notify the container and
     *  and value listeners. If an expression had been
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
        if (_debugging) {
            _debug("setToken: " + token);
        }
        _setTokenAndNotify(token);

        // Override any expression that may have been previously given.
        if (_currentExpression != null) {
            _currentExpression = null;
            
            // Make sure the new value is exported in MoML.  EAL 12/03.
            _setModifiedFromClass();

            _parseTree = null;
            _parseTreeValid = false;
        }

        setUnknown(false);
    }
    
    /** Set the expression for this variable by calling
     *  setExpression(), and then evaluate it by calling
     *  validate().  This will cause any other variables
     *  that are dependent on it to be evaluated, and will
     *  also cause the container to be notified of the change,
     *  unlike setExpression().
     *  @param expression The expression.
     *  @see #setExpression(String)
     *  @see #validate()
     *  @exception IllegalActionException If this variable or a
     *   variable dependent on this variable cannot be evaluated (and is
     *   not lazy) and the model error handler throws an exception.
     *   Also thrown if the change is not acceptable to the container.
     */
    public void setToken(String expression) throws IllegalActionException {
        setExpression(expression);
        validate();
    }

    /** Constrain the type of this variable to be equal to or
     *  greater than the type of the specified object.
     *  This constraint is not enforced
     *  here, but is returned by the typeConstraintList() method for use
     *  by a type system.
     *  @param lesser A Typeable object.
     */
    public void setTypeAtLeast(Typeable lesser) {
        if (_debugging) {
            String name = "not named";
            if (lesser instanceof Nameable) {
                name = ((Nameable)lesser).getFullName();
            }
            _debug("setTypeAtLeast: " + name);
        }
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
        if (_debugging) {
            String name = "not named";
            if (typeTerm.getAssociatedObject() instanceof Nameable) {
                name = ((Nameable)typeTerm.getAssociatedObject()).getFullName();
            }
            _debug("setTypeAtLeast: " + name);
        }
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
     *  To remove the type constraint, call this method with a
     *  BaseType.UNKNOWN argument.
     *  @exception IllegalActionException If the type of this object
     *   already violates this constraint, or if the argument is not
     *   an instantiable type in the type lattice.
     */
    public void setTypeAtMost(Type type) throws IllegalActionException {
        if (_debugging) {
            _debug("setTypeAtMost: " + type);
        }
        if (type == BaseType.UNKNOWN) {
            _typeAtMost = BaseType.UNKNOWN;
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
     *  the specified value. This is an absolute type constraint (not
     *  relative to another Typeable object), so it is checked every time
     *  the value of the variable is set by setToken() or by evaluating
     *  an expression.  If the variable already has a value, then that
     *  value is converted to the specified type, if possible, or an
     *  exception is thrown.
     *  To remove the type constraint, call this method with the argument
     *  BaseType.UNKNOWN.
     *  @param type A Type.
     *  @exception IllegalActionException If the type of this object
     *   already violates this constraint, in that the currently contained
     *   token cannot be converted losslessly to the specified type.
     */
    public void setTypeEquals(Type type) throws IllegalActionException {
        if (_debugging) {
            _debug("setTypeEquals: " + type);
        }
        if (_token != null) {
            if (type.isCompatible(_token.getType())) {
                _token = type.convert(_token);
            } else {
                throw new IllegalActionException(this,
                        "The currently contained token "
                        + _token.getClass().getName()
                        + "("
                        + _token.toString()
                        + ") is not compatible with the desired type "
                        + type.toString());
            }
        }

        // set _declaredType to a clone of the argument since the argument
        // may be a structured type and may change later.
        try {
            _declaredType = (Type)type.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new InternalErrorException("Variable.setTypeEquals: " +
                    "The specified type cannot be cloned.");
        }

        // set _varType. It is _token.getType() if _token is not null, or
        // _declaredType if _token is null.
        _varType = _declaredType;
        if (_token != null && _declaredType instanceof StructuredType) {
            ((StructuredType)_varType).updateType(
                    (StructuredType)_token.getType());
        }
    }

    /** Mark the value of this variable to be unknown if the argument is
     *  <i>true</i>, or known if the argument is <i>false</i>.  In domains
     *  with fixed-point semantics, such as SR, a variable that depends on
     *  a port value may be unknown at various points during the execution.
     *  @see #isKnown()
     *  @param value True to change mark this variable unknown.
     */
    public void setUnknown(boolean value) {
        if (_debugging) {
            _debug("setUnknown: " + value);
        }
        _isTokenUnknown = value;
    }

    /** Set the visibility of this variable.  The argument should be one
     *  of the public static instances in Settable.
     *  @param visibility The visibility of this variable.
     */
    public void setVisibility(Settable.Visibility visibility) {
        if (_debugging) {
            _debug("setVisibility: " + visibility);
        }
        _visibility = visibility;
    }

    /** Constrain the type of this variable to be the same as the
     *  type of the specified object.  This constraint is not enforced
     *  here, but is returned by the typeConstraintList() method for use
     *  by a type system.
     *  @param equal A Typeable object.
     */
    public void setTypeSameAs(Typeable equal) {
        if (_debugging) {
            String name = "not named";
            if (equal instanceof Nameable) {
                name = ((Nameable)equal).getFullName();
            }
            _debug("setTypeSameAs: " + name);
        }
        Inequality ineq = new Inequality(this.getTypeTerm(),
                equal.getTypeTerm());
        _constraints.add(ineq);
        ineq = new Inequality(equal.getTypeTerm(),
                this.getTypeTerm());
        _constraints.add(ineq);
    }

    /** Same as getExpression().
     *  @return A string representation of this variable.
     *  @deprecated
     */
    public String stringRepresentation() {
        return getExpression();
    }

    /** Return a string representation of the current evaluated variable value.
     *  @return A string representing the class and the current token.
     */
    public String toString() {
        ptolemy.data.Token value = null;
        try {
            value = getToken();
        } catch (IllegalActionException ex) {
            // The value of this variable is undefined.
        }
        String tokenString;
        if (value == null) {
            tokenString = "value undefined";
        } else {
            tokenString = value.toString();
        }

        if (tokenString.length() > 50) {
            tokenString = "value elided";
        }

        return super.toString() + " " +
            tokenString;
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
        // Include all relative types that have been specified.
        List result = new LinkedList();
        result.addAll(_constraints);

        // If the variable has a value known at this time, add a constraint.
        // If the variable is not evaluatable at this time (an exception is
        // thrown in _evaluate(), do nothing.
        // Add the inequality to the result list directly to add the
        // constraint only for this round of type resolution. If using
        // setTypeAtLeast(), the constraint will be permanent for this
        // Variable.
        try {
            Token currentToken = getToken();
            if (currentToken != null) {
                Type currentType = currentToken.getType();

                TypeConstant current = new TypeConstant(currentType);
                Inequality ineq = new Inequality(current, getTypeTerm());
                result.add(ineq);
            }
        } catch (Exception e) {
            // expression cannot be evaluated at this time.
            // do nothing.
        }

        // If the variable has a type, add a constraint.
        // Type currentType = getType();
        // if (currentType != BaseType.UNKNOWN) {
        //     TypeConstant current = new TypeConstant(currentType);
        //     Inequality ineq = new Inequality(current, getTypeTerm());
        //     result.add(ineq);
        // }

        // If an upper bound has been specified, add a constraint.
        if (_typeAtMost != BaseType.UNKNOWN) {
            TypeConstant atMost = new TypeConstant(_typeAtMost);
            Inequality ineq = new Inequality(getTypeTerm(), atMost);
            result.add(ineq);
        }

        return result;
    }

    /** If this variable is not lazy (the default) then evaluate
     *  the expression contained in this variable, and notify any
     *  value dependents. If those are not lazy, then they too will
     *  be evaluated.  Also, if the variable is not lazy, then
     *  notify its container, if there is one, by calling its
     *  attributeChanged() method.
     *  <p>
     *  If this variable is lazy, then mark this variable and any
     *  of its value dependents as needing evaluation and for any
     *  value dependents that are not lazy, evaluate them.
     *  Note that if there are no value dependents,
     *  or if they are all lazy, then this will not
     *  result in evaluation of this variable, and hence will not ensure
     *  that the expression giving its value is valid.  Call getToken()
     *  or getType() to accomplish that.
     *  @exception IllegalActionException If this variable or a
     *   variable dependent on this variable cannot be evaluated (and is
     *   not lazy) and the model error handler throws an exception.
     *   Also thrown if the change is not acceptable to the container.
     */
    public void validate() throws IllegalActionException {
        if (_debugging) {
            _debug("validate");
        }
        invalidate();
        // Unless the expression is null, the following will have
        // been set to true by the invalidate() call above.
        // See note below... this is not used anymore.
        // boolean neededEvaluation = _needsEvaluation;
        List errors = _propagate();
        if (errors != null && errors.size() > 0) {
            Iterator errorsIterator = errors.iterator();
            StringBuffer message = new StringBuffer();
            while (errorsIterator.hasNext()) {
                Exception error = (Exception)errorsIterator.next();
                message.append(error.getMessage());
                if (errorsIterator.hasNext()) {
                    message.append("\n-------------- and --------------\n");
                }
            }
            // NOTE: We could use exception chaining here to report
            // the cause, but this leads to very verbose error
            // error messages that are not very friendly.
            throw new IllegalActionException(message.toString());
        }
        // NOTE: The call to _propagate() above has already done
        // notification, but only if _needsEvaluation was true.
        // Note that this will not happen unless the expression is also null.
        // Thus, we do the call here only if _needsEvaluation was false.
        // Generally, this only happens on construction of parameters (?).
        // EAL 6/11/03
        // NOTE: Regrettably, this also happens when changing the value
        // of a parameter from non-null to null.  This erroneously prevents
        // notification of this change.  So this optimization is invalid.
        // I believe its intent was to prevent double invocation of this
        // method for each parameter, once when it is being constructed
        // and once when it's value is being set.
        // EAL 9/16/03
        // if (!_isLazy && !neededEvaluation) {
        if (!_isLazy) {
            NamedObj container = (NamedObj)getContainer();
            if (container != null) {
                container.attributeChanged(this);
            }
        }
    }

    /** React to the change in the specified instance of Settable.
     *  Mark this variable as needing reevaluation when next accessed.
     *  Notify the value listeners of this variable.
     *  @param settable The object that has changed value.
     */
    public void valueChanged(Settable settable) {
        if (!_needsEvaluation) {
            // If the value was set via an expression, then mark this
            // variable as needing evaluation.
            // NOTE: For some reason, until 12/24/02, there was no "if"
            // here, which means _needsEvaluation was set to true even
            // if this variable's value had been set by setToken().  Why? EAL
            if (_currentExpression != null) {
                _needsEvaluation = true;
            }
            _notifyValueListeners();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

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
     *  This method may trigger a model error, which is delegated up
     *  the container hierarchy until an error handler is found, and
     *  is ignored if no error handler is found.  A model error occurs
     *  if the expression cannot be parsed or cannot be evaluated.
     *  <p>
     *  Part of this method is read-synchronized on the workspace.
     *
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if a dependency loop is found.
     */
    protected void _evaluate() throws IllegalActionException {
        if (_currentExpression == null
                || _currentExpression.trim().equals("")) {
            _setToken(null);
            return;
        }
        // If _dependencyLoop is true, then this call to evaluate() must
        // have been triggered by evaluating the expression of this variable,
        // which means that the expression directly or indirectly refers
        // to itself.
        if (_dependencyLoop) {
            _dependencyLoop = false;
            throw new IllegalActionException("There is a dependency loop"
                    + " where " + getFullName() + " directly or indirectly"
                    + " refers to itself in its expression: "
                    + _currentExpression);
        }
        _dependencyLoop = true;

        try {
            workspace().getReadAccess();
            _parseIfNecessary();
            if (_parseTreeEvaluator == null) {
                _parseTreeEvaluator = new ParseTreeEvaluator();
            }
            if (_parserScope == null) {
                _parserScope = new VariableScope();
            }
            Token result = _parseTreeEvaluator.evaluateParseTree(
                    _parseTree, _parserScope);
            _setTokenAndNotify(result);
        } catch (IllegalActionException ex) {
            _needsEvaluation = true;
            throw new IllegalActionException(this, ex,
                    "Error evaluating expression: "
                    + _currentExpression);
        } finally {
            _dependencyLoop = false;
            workspace().doneReading();
        }
    }

    /** Notify the value listeners of this variable that this variable
     *  changed.
     */
    protected void _notifyValueListeners() {
        if (_valueListeners != null) {
            // Synchronize to be sure that listeners are not being
            // added or removed while we clone the list.  We clone
            // the list because notification can result in arbitrary
            // code executing, which if this were to happen within
            // the synchronized block, would create risk of deadlock.
            Iterator listeners;
            synchronized(this) {
                listeners = (new LinkedList(_valueListeners)).iterator();
            }
            while (listeners.hasNext()) {
                ValueListener listener = (ValueListener)listeners.next();
                listener.valueChanged(this);
            }
        }
    }
    
    /** Parse the expression, if the current parse tree is not valid.
     *  This method should only be called if the expression is valid.
     *  @exception IllegalActionException If the exception cannot be parsed.
     */
    protected final void _parseIfNecessary() throws IllegalActionException {
        if (!_parseTreeValid) {
            if(_currentExpression == null) {
                throw new IllegalActionException(this, 
                        "Empty expression cannot be parsed!");
            }
            PtParser parser = new PtParser();
            if(isStringMode()) {
                // Different parse rules for String mode parameters.
                _parseTree = parser.generateStringParseTree(
                        _currentExpression);
            } else {
                // Normal parse rules for expressions.
                _parseTree = parser.generateParseTree(
                        _currentExpression);
            } 
            _parseTreeValid = (_parseTree != null);
        }
    }

    /** Force evaluation of this variable, unless it is lazy,
     *  and call _propagate() on its value dependents.
     *  @return A list of instances of IllegalActionException, one
     *   for each exception triggered by a failure to evaluate a
     *   value dependent, or null if there were no failures.
     */
    protected List _propagate() {
        if (_propagating) {
            return null;
        }
        _propagating = true;
        try {
            List result = null;
            // Force evaluation.
            if (_needsEvaluation && !_isLazy) {
                try {
                    _evaluate();
                } catch (IllegalActionException ex) {
                    // This is very confusing code.
                    // Don't mess with it if it works.
                    try {
                        handleModelError(this, ex);
                    } catch (IllegalActionException ex2) {
                        result = new LinkedList();
                        result.add(ex2);
                    }
                }
            }
            // All the value dependents now need evaluation also.
            List additionalErrors = _propagateToValueListeners();
            if (result == null) {
                result = additionalErrors;
            } else {
                if (additionalErrors != null) {
                    result.addAll(additionalErrors);
                }
            }
            return result;
        } finally {
            _propagating = false;
        }
    }

    /** Call propagate() on all value listeners.
     *  @return A list of instances of IllegalActionException, one
     *   for each exception triggered by a failure to evaluate a
     *   value dependent, or null if there were no failures.
     */
    protected List _propagateToValueListeners() {
        List result = null;
        if (_valueListeners != null) {
            // Avoid co-modification exception.
            Iterator listeners;
            synchronized(this) {
                listeners = (new LinkedList(_valueListeners)).iterator();
            }
            while (listeners.hasNext()) {
                ValueListener listener = (ValueListener)listeners.next();
                // Avoid doing this more than once if the the value
                // dependent appears more than once.  This also has
                // the advantage of stopping circular reference looping.
                if (listener instanceof Variable) {
                    if (((Variable)listener)._needsEvaluation) {
                        List additionalErrors
                            = ((Variable)listener)._propagate();
                        if (additionalErrors != null) {
                            if (result == null) {
                                result = new LinkedList();
                            }
                            result.addAll(additionalErrors);
                        }
                    }
                }
            }
        }
        return result;
    }
    
    /** Set the token value and type of the variable.
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
    protected void _setToken(Token newToken) throws IllegalActionException {
        if (newToken == null) {
            _token = null;
            _needsEvaluation = false;

            // set _varType
            if (_declaredType instanceof StructuredType) {
                ((StructuredType)_varType).initialize(BaseType.UNKNOWN);
            } else {
                _varType = _declaredType;
            }
        } else {
            // newToken is not null, check if it is compatible with
            // _declaredType. For structured types, _declaredType and _varType
            // are the same reference, need to initialize this type
            // before checking compatibility. But if the new token is not
            // compatible with the declared type, the current resolved type
            // need to be preserved, so make a clone.
            Type declaredType;
            try {
                declaredType = (Type)_declaredType.clone();
            } catch (CloneNotSupportedException cnse) {
                throw new InternalErrorException("Variable._setToken: " +
                        "Cannot clone the declared type of this Variable.");
            }
            if (declaredType instanceof StructuredType) {
                ((StructuredType)declaredType).initialize(BaseType.UNKNOWN);
            }
            if (declaredType.isCompatible(newToken.getType())) {
                newToken = declaredType.convert(newToken);
            } else {
                throw new IllegalActionException(this,
                        "Variable._setToken: Cannot store a token of type " +
                        newToken.getType().toString() +
                        ", which is incompatible with type " +
                        declaredType.toString());
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
            if (_typeAtMost != BaseType.UNKNOWN) {
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
    protected void _setTokenAndNotify(Token newToken)
            throws IllegalActionException {

        // Save to restore in case the change is rejected.
        Token oldToken = _token;
        Type oldVarType = _varType;
        if (_varType instanceof StructuredType) {
            try {
                oldVarType = (Type)((StructuredType)_varType).clone();
            } catch (CloneNotSupportedException ex2) {
                throw new InternalErrorException(
                        "Variable._setTokenAndNotify: " +
                        " Cannot clone _varType" +
                        ex2.getMessage());
            }
        }
        boolean oldNoTokenYet = _noTokenYet;
        String oldInitialExpression = _initialExpression;
        Token oldInitialToken = _initialToken;


        try {
            _setToken(newToken);
            NamedObj container = (NamedObj)getContainer();
            if (container != null) {
                if ( !oldVarType.equals(_varType) &&
                        oldVarType != BaseType.UNKNOWN) {
                    container.attributeTypeChanged(this);
                }
                container.attributeChanged(this);
            }
            _notifyValueListeners();
        } catch (IllegalActionException ex) {
            // reverse the changes
            _token = oldToken;

            if (_varType instanceof StructuredType
                    && oldVarType instanceof StructuredType) {
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
    ////                         protected variables               ////

    /** Stores the expression used to set this variable. It is null if
     *  the variable was set from a token.
     */
    protected String _currentExpression = null;
    
    /** Flags that the expression needs to be evaluated when the value of this
     *  variable is queried.
     */
    protected boolean _needsEvaluation = false;
    
    /** The instance of VariableScope. */
    protected ParserScope _parserScope = null;
    
    /** Indicator that the parse tree is valid. */
    protected boolean _parseTreeValid = false;
    
    /** Listeners for changes in value. */
    protected List _valueListeners;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _invalidateShadowedSettables(NamedObj object)
            throws IllegalActionException {
        if (object == null) {
            // Nothing to do.
            return;
        }
        for (Iterator variables = object.attributeList(
                Variable.class).iterator();
             variables.hasNext();) {
            Variable variable = (Variable)variables.next();
            if (variable.getName().equals(getName())) {
                variable.invalidate();
            }
        }
        // Also invalidate the variables inside any
        // scopeExtendingAttributes.
        Iterator scopeAttributes = object.attributeList(
                ScopeExtendingAttribute.class).iterator();
        while (scopeAttributes.hasNext()) {
            ScopeExtendingAttribute attribute =
                (ScopeExtendingAttribute)scopeAttributes.next();
            Iterator variables = attribute.attributeList(
                    Variable.class).iterator();
            while (variables.hasNext()) {
                Variable variable = (Variable)variables.next();
                if (variable.getName().equals(getName())) {
                    variable.invalidate();
                }
            }
        }
        NamedObj container = (NamedObj)object.getContainer();
        if (container != null) {
            _invalidateShadowedSettables(container);
        }
    }

    /** Return true if the argument is legal to be added to the scope
     *  of this variable. In this base class, this method only checks
     *  that the argument is in the same workspace as this variable.
     *  @param var The variable to be checked.
     *  @return True if the argument is legal.
     */
    private boolean _isLegalInScope(Variable var) {
        return (var.workspace() == this.workspace());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Used to check for dependency loops among variables.
    private transient boolean _dependencyLoop = false;
    
    // Empty string token.
    private static StringToken _EMPTY_STRING_TOKEN = new StringToken("");

    // Stores the expression used to initialize this variable. It is null if
    // the first token placed in the variable is not the result of evaluating
    // an expression.
    private String _initialExpression;

    // Indicator that this variable is lazy.
    private boolean _isLazy;

    // Indicates if string mode is on.
    private boolean _isStringMode = false;

    // Indicates whether this variable has been flagged as unknown.
    private boolean _isTokenUnknown = false;

    // Flags whether the variable has not yet contained a token.
    private boolean _noTokenYet = true;

    // Stores the first token placed in this variable. It is null if the
    // first token contained by this variable was the result of evaluating
    // an expression.
    private ptolemy.data.Token _initialToken;

    /** Stores the variables that are referenced by this variable. */
    private HashMap _variablesDependentOn = null;
    
    /** Version of the workspace when _variablesDependentOn was updated. */
    private transient long _variablesDependentOnVersion = -1;

    // Stores the Class object which represents the type of this variable.
    private Type _varType = BaseType.UNKNOWN;

    // the parse tree evaluator used by this variable.
    private ParseTreeEvaluator _parseTreeEvaluator;

    // Flag indicating that _propagate() is in progress.
    private boolean _propagating;

    // The token contained by this variable.
    private ptolemy.data.Token _token;

    // Type constraints.
    private List _constraints = new LinkedList();

    // The type set by setTypeEquals(). If _declaredType is not
    // BaseType.UNKNOWN, the type of this Variable is fixed to that type.
    private Type _declaredType = BaseType.UNKNOWN;
    
    // If the variable was last set from an expression, this stores
    //  the parse tree for that expression.
    private ASTPtRootNode _parseTree;

    // If setTypeAtMost() has been called, then the type bound is stored here.
    private Type _typeAtMost = BaseType.UNKNOWN;

    // Reference to the inner class that implements InequalityTerm.
    private TypeTerm _typeTerm = null;

    // The visibility of this variable.
    private Settable.Visibility _visibility = Settable.EXPERT;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private class TypeTerm implements InequalityTerm {

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return this Variable.
         *  @return A Variable.
         */
        public Object getAssociatedObject() {
            return Variable.this;
        }

        /** Return the type of this Variable.
         */
        public Object getValue() {
            return getType();
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

            if (_declaredType == BaseType.UNKNOWN) {
                _varType = (Type)e;
            } else {
                // _declaredType is a StructuredType
                ((StructuredType)_varType).initialize((Type)e);
            }
        }

        /** Test if the type of this variable is fixed. The type is fixed if
         *  setTypeEquals() is called with an argument that is not
         *  BaseType.UNKNOWN, or the user has set a non-null expression or
         *  token into this variable.
         *  @return True if the type of this variable can be set;
         *   false otherwise.
         */
        public boolean isSettable() {
            return ( !_declaredType.isConstant());
        }

        /** Check whether the current value of this term is acceptable.
         *  This method delegates the check to the isTypeAcceptable()
         *  method of the outer class.
         *  @return True if the current value is acceptable.
         */
        public boolean isValueAcceptable() {
            return isTypeAcceptable();
        }

        /** Set the type of this variable.
         *  @param e a Type.
         *  @exception IllegalActionException If this type is not settable,
         *   or this type cannot be updated to the new type.
         */
        public void setValue(Object e) throws IllegalActionException {
            if ( !isSettable()) {
                throw new IllegalActionException("TypeTerm.setValue: The "
                        + "type is not settable.");
            }

            if ( !_declaredType.isSubstitutionInstance((Type)e)) {
                throw new IllegalActionException("Variable$TypeTerm"
                        + ".setValue: "
                        + "Cannot update the type of this variable to the "
                        + "new type."
                        + " Variable: " + Variable.this.getFullName()
                        + ", Variable type: " + _declaredType.toString()
                        + ", New type: " + e.toString());
            }

            if (_declaredType == BaseType.UNKNOWN) {
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
            return "(" + Variable.this.toString() + ", " + getType() + ")";
        }
    }

    /** Scope implementation with local caching. */
    protected class VariableScope extends ModelScope {
        
        /** Construct a scope consisting of the variables
         *  of the container of the the enclosing instance of
         *  Variable and its containers and their scope-extending
         *  attributes.
         */
        public VariableScope() {
            this(null);
        }
        
        /** Construct a scope consisting of the variables
         *  of the specified container its containers and their
         *  scope-extending attributes. If the argument is null,
         *  then use the container of the enclosing instance of
         *  Variable as the reference for the scope.
         */
        public VariableScope(NamedObj reference) {
            _reference = reference;
        }

        /** Look up and return the attribute with the specified name in the
         *  scope. Return null if such an attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.data.Token get(String name)
                throws IllegalActionException {
            if (_variablesDependentOn == null) {
                _variablesDependentOn = new HashMap();
            } else {
                // Variable might be cached.
                if (_variablesDependentOnVersion == workspace().getVersion()) {
                    // Cache is valid. Look up the variable.
                    Variable result = (Variable)_variablesDependentOn.get(name);
                    if (result != null) {
                        return result.getToken();
                    }
                } else {
                    // Cache is invalid.  Clear it.
                    _variablesDependentOn.clear();
                }
            }
            // Either cache is not valid, or the variable is not in the cache.
            _variablesDependentOnVersion = workspace().getVersion();
            
            NamedObj reference = _reference;
            if (_reference == null) {
                reference = (NamedObj)Variable.this.getContainer();
            }
            Variable result = getScopedVariable(
                    Variable.this,
                    reference,
                    name);

            if (result != null) {
                // If the variable is not in the cache, then we also
                // may not be a value listener for it.
                if (!_variablesDependentOn.containsValue(result)) {
                    result.addValueListener(Variable.this);
                    _variablesDependentOn.put(name, result);
                }
                return result.getToken();
            } else {
                return null;
            }
        }

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.data.type.Type getType(String name)
                throws IllegalActionException {
            NamedObj reference = _reference;
            if (_reference == null) {
                reference = (NamedObj)Variable.this.getContainer();
            }

            Variable result = getScopedVariable(
                    Variable.this,
                    reference,
                    name);
            if (result != null) {
                return result.getType();
            } else {
                return null;
            }
        }

        /** Look up and return the type term for the specified name
         *  in the scope. Return null if the name is not defined in this
         *  scope, or is a constant type.
         *  @return The InequalityTerm associated with the given name in
         *  the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.graph.InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            NamedObj reference = _reference;
            if (_reference == null) {
                reference = (NamedObj)Variable.this.getContainer();
            }

            Variable result = getScopedVariable(
                    Variable.this,
                    reference,
                    name);
            if (result != null) {
                return result.getTypeTerm();
            } else {
                return null;
            }
        }

        /** Return the list of identifiers within the scope.
         *  @return The list of variable names within the scope.
         */
        public Set identifierSet() {
            NamedObj reference = _reference;
            if (_reference == null) {
                reference = (NamedObj)Variable.this.getContainer();
            }
            return getAllScopedVariableNames(Variable.this, reference);
        }
        
        // Reference object for the scope.
        private NamedObj _reference;
    }
}

