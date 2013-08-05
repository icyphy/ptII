/* State of a type analyzer.

 Copyright (c) 2005-2013 The Regents of the University of California.
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
package ptolemy.backtrack.eclipse.ast;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Stack;

///////////////////////////////////////////////////////////////////
//// TypeAnalyzerState

/**
 The state of a type analyzer. As the type analyzer traverses an Eclipse
 Abstract Syntax Tree (AST), it records its state in an object of this
 class. This state is passed to the handlers that handle special events
 in the traversal. The handlers may then retrieve information about the
 current state of the analyzer.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TypeAnalyzerState {
    ///////////////////////////////////////////////////////////////////
    ////                        constructor                        ////

    /** Construct a state object for a type analyzer.
     *
     *  @param analyzer The type analyzer.
     */
    public TypeAnalyzerState(TypeAnalyzer analyzer) {
        _analyzer = analyzer;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a variable to the current scope.
     *
     *  @param name The name of the variable.
     *  @param type The type of the variable.
     */
    public void addVariable(String name, Type type) {
        _variableStack.peek().put(name, type);
    }

    /** Enter the scope of a block.
     *
     *  @see #leaveBlock()
     */
    public void enterBlock() {
        _previousClasses.push(new Hashtable<String, Class>());
    }

    /** Enter the scope of a class. The current class is set to the class
     *  entered and the last current class is pushed to the previous class
     *  stack.
     *
     *  @param c The class entered.
     *  @see #leaveClass()
     */
    public void enterClass(Class c) {
        if (_currentClass == null) {
            _previousClasses.push(null);
        } else {
            _previousClasses.push(new CurrentClassElement(_currentClass));
        }
        _anonymousCounts.push(Integer.valueOf(0));
        _currentClass = c;
        _loader.setCurrentClass(_currentClass, false);
    }

    /** Get the type analyzer that owns this state.
     *
     *  @return The type analyzer.
     */
    public TypeAnalyzer getAnalyzer() {
        return _analyzer;
    }

    /** Get the class loader.
     *
     *  @return The class loader used to load unresolved classes.
     *  @see #setClassLoader(LocalClassLoader)
     */
    public LocalClassLoader getClassLoader() {
        return _loader;
    }

    /** Get the set of cross-analyzed types.
     *
     *  @return The set of names of types to be cross-analyzed.
     */
    public Set<String> getCrossAnalyzedTypes() {
        return _crossAnalyzedTypes;
    }

    /** Get the current class (the class currently being inspected).
     *
     *  @return The current class. It may be <tt>null</tt> when there
     *   is no current class.
     *  @see #setCurrentClass(Class)
     */
    public Class getCurrentClass() {
        return _currentClass;
    }

    /** Get the previous class stack. The previous class stack stores
     *  all the entered but not exited class definitions as well as
     *  blocks, not including the current class. Each element in the
     *  stack is either of type {@link Class} or type {@link Hashtable}.
     *  The bottom element in the stack is always <tt>null</tt>.
     *  <p>
     *  An element of type {@link Hashtable} means the previous entity is
     *  a block, where classes can also be defined in it. In that case,
     *  simple class names are the keys of the table, and {@link Class}
     *  objects are its values.
     *
     *  @return The previous class stack.
     *  @see #setPreviousClasses(Stack)
     */
    public Stack<Hashtable<String, Class>> getPreviousClasses() {
        return _previousClasses;
    }

    /** Get the type of a variable with its name in the current scope
     *  and all the scopes enclosing the current scope. If it is not
     *  in the current scope, scopes enclosing the current scope are
     *  checked. The scopes can be a variable scope or a class scope.
     *  The effect is the same as <tt>getVariable(name, false)</tt>.
     *
     *  @param name The variable name.
     *  @return The type of the variable, or <tt>null</tt> if it is not
     *   found.
     *  @see #getVariable(String, boolean)
     */
    public Type getVariable(String name) {
        return getVariable(name, false);
    }

    /** Get the type of a variable with its name in the current scope
     *  and all the scopes enclosing the current scope. If it is not
     *  in the current scope, scopes enclosing the current scope are
     *  checked.
     *  <p>
     *  If <tt>variableOnly</tt> is true, only variable scopes are
     *  checked, but class scopes are ignored.
     *
     *  @param name The variable name.
     *  @param variablesOnly Whether to check variable scopes only.
     *  @return The type of the variable, or <tt>null</tt> if it is not
     *   found.
     *  @see #getVariable(String)
     */
    public Type getVariable(String name, boolean variablesOnly) {
        int i = _variableStack.size() - 1;

        if (i == -1) {
            return null;
        }

        Hashtable<String, Type> table = _variableStack.peek();

        while (!table.containsKey(name) && (i >= 1)) {
            i--;

            if (!variablesOnly || !_classScopes.contains(Integer.valueOf(i))) {
                table = _variableStack.get(i);
            }
        }

        return table.get(name);
    }

    /** Get the variable stack. The variable stack is a stack of scopes.
     *  Each element in this stack is of type {@link Hashtable}, with
     *  variable names as its keys and types of those variables as its
     *  values. The top element in this stack is considered the current
     *  scope. Other elements in it are scopes enclosing the current
     *  scope.
     *
     *  @return The variable stack.
     *  @see #setVariableStack(Stack)
     */
    public Stack<Hashtable<String, Type>> getVariableStack() {
        return _variableStack;
    }

    /** Return whether a name refers to a variable in the variable stack.
     *
     *  @param name The name.
     *  @return true if the name refers to a defined variable.
     */
    public boolean isVariable(String name) {
        int i = _variableStack.size() - 1;

        if (i == -1) {
            return false;
        }

        Hashtable<String, Type> table = _variableStack.peek();

        while (!table.containsKey(name) && (i >= 1)) {
            table = _variableStack.get(--i);
        }

        return table.containsKey(name)
                && !_classScopes.contains(Integer.valueOf(i));
    }

    /** Leave a block declaration.
     *
     *  @see #enterBlock()
     */
    public void leaveBlock() {
        _previousClasses.pop();
    }

    /** Leave a class declaration. The current class is set back to the
     *  last current class (the class on the top of the previous class
     *  stack).
     *
     *  @see #enterClass(Class)
     */
    public void leaveClass() {
        int i = _previousClasses.size() - 1;

        while ((i >= 0)
                && !(_previousClasses.get(i) instanceof CurrentClassElement)) {
            i--;
        }

        _currentClass = (i >= 0) ? ((CurrentClassElement) _previousClasses
                .get(i)).getCurrentClassElement() : null;
        _previousClasses.pop();
        _anonymousCounts.pop();
        _loader.setCurrentClass(_currentClass, false);
    }

    /** Get the next count of anonymous classes in the current class.
     *
     *  @return The count.
     */
    public int nextAnonymousCount() {
        int lastCount = _anonymousCounts.pop().intValue();
        _anonymousCounts.push(new Integer(++lastCount));
        return lastCount;
    }

    /** Get the next count of total anonymous classes.
     *
     *  @return The count.
     */
    public int nextTotalAnonymousCount() {
        return ++_totalAnonymousCount;
    }

    /** Set the class loader.
     *
     *  @param loader The class loader.
     *  @see #getClassLoader()
     */
    public void setClassLoader(LocalClassLoader loader) {
        _loader = loader;
    }

    /** Set the current scope to be a class scope (a scope opened by
     *  a class declaration).
     *
     *  @see #unsetClassScope()
     */
    public void setClassScope() {
        _classScopes.add(Integer.valueOf(_variableStack.size() - 1));
    }

    /** Get the current class (the class currently being inspected).
     *
     *  @param currentClass The current class, or <tt>null</tt> if there
     *   is no current class.
     *  @see #getCurrentClass()
     */
    public void setCurrentClass(Class currentClass) {
        _currentClass = currentClass;
    }

    /** Set the previous class stack. The previous class stack stores
     *  all the entered but not exited class definitions, not including
     *  the current class. Each element in the stack is of type {@link
     *  Class}. The bottom element in the stack is always <tt>null</tt>.
     *
     *  @param previousClasses The previous class stack.
     *  @see #getPreviousClasses()
     */
    public void setPreviousClasses(
            Stack<Hashtable<String, Class>> previousClasses) {
        _previousClasses = previousClasses;
    }

    /** Set the variable stack. The variable stack is a stack of scopes.
     *  Each element in this stack is of type {@link Hashtable}, with
     *  variable names as its keys and types of those variables as its
     *  values. The top element in this stack is considered the current
     *  scope. Other elements in it are scopes enclosing the current
     *  scope.
     *
     *  @param variableStack The variable stack.
     *  @see #getVariableStack()
     */
    public void setVariableStack(Stack<Hashtable<String, Type>> variableStack) {
        _variableStack = variableStack;
    }

    /** Unset the current scope as a class scope (a scope opened by a
     *  class declaration).
     *  <p>
     *  A class scope should be unset when removed from the scope stack.
     *
     *  @see #setClassScope()
     */
    public void unsetClassScope() {
        _classScopes.remove(Integer.valueOf(_variableStack.size() - 1));
    }

    /** A table to hold the current class, which is the only object in the
     *  table.
     *
     *  @author tfeng
     */
    public static class CurrentClassElement extends Hashtable<String, Class> {

        /** Construct a table with a single element (the current class object)
         *  in it.
         *
         *  @param currentClass The current class.
         */
        public CurrentClassElement(Class currentClass) {
            super.put(_EMPTY_NAME, currentClass);
        }

        /** Get the current class element recorded in this table.
         *
         *  @return The current class element.
         */
        public Class getCurrentClassElement() {
            return get(_EMPTY_NAME);
        }

        /** An empty name as the key for the current class in the tables.
         */
        private static final String _EMPTY_NAME = "";
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private fields                     ////

    /** The type analyzer that owns this state.
     */
    private TypeAnalyzer _analyzer;

    /** The stack of individual anonymous class counts.
     */
    private Stack<Integer> _anonymousCounts = new Stack<Integer>();

    /** The set of scopes that correspond to class declarations.
     */
    private Set<Integer> _classScopes = new HashSet<Integer>();

    /** The set of names of types to be cross-analyzed.
     */
    private Set<String> _crossAnalyzedTypes = new HashSet<String>();

    /** The class currently being analyzed, whose declaration is opened
     *  most recently. <tt>null</tt> only when the analyzer is analyzing
     *  the part of source code before any class definition ("package"
     *  and "import").
     */
    private Class _currentClass;

    /** The class loader used to load classes.
     */
    private LocalClassLoader _loader;

    /** The stack of previously opened class, which has not been closed
     *  yet.
     */
    private Stack<Hashtable<String, Class>> _previousClasses = new Stack<Hashtable<String, Class>>();

    /** The counter for anonymous classes.
     */
    private int _totalAnonymousCount = 0;

    /** The stack of currently opened scopes for variable
     *  declaration. Each element is a {@link Hashtable}. In each table,
     *  keys are variable names while values are {@link Type}'s of the
     *  corresponding variables.
     */
    private Stack<Hashtable<String, Type>> _variableStack = new Stack<Hashtable<String, Type>>();
}
