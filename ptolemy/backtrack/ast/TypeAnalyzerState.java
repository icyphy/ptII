/* State of a type analyzer.

Copyright (c) 1997-2004 The Regents of the University of California.
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

package ptolemy.backtrack.ast;

import java.util.Hashtable;
import java.util.Stack;

//////////////////////////////////////////////////////////////////////////
//// TypeAnalyzerState
/**
 *
 *
 *  @author Thomas Feng
 *  @version $Id$
 *  @since Ptolemy II 4.1
 *  @Pt.ProposedRating Red (tfeng)
 */
public class TypeAnalyzerState {

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /**
     *  @return Returns the variableStack.
     */
    public LocalClassLoader getClassLoader() {
        return _loader;
    }

    /**
     *  @param loader The variableStack to set.
     */
    public void setClassLoader(LocalClassLoader loader) {
        _loader = loader;
    }

    /**
     *  @return Returns the variableStack.
     */
    public Class getCurrentClass() {
        return _currentClass;
    }

    /**
     *  @param currentClass The variableStack to set.
     */
    public void setCurrentClass(Class currentClass) {
        _currentClass = currentClass;
    }

    /**
     *  @return Returns the variableStack.
     */
    public Stack getPreviousClasses() {
        return _previousClasses;
    }

    /**
     *  @param previousClasses The variableStack to set.
     */
    public void setPreviousClasses(Stack previousClasses) {
        _previousClasses = previousClasses;
    }

    public void enterClass(Class c) {
        _previousClasses.push(_currentClass);
        _currentClass = c;
        _loader.setCurrentClass(_currentClass, false);
    }

    public void leaveClass() {
        _currentClass = (Class)_previousClasses.pop();
        _loader.setCurrentClass(_currentClass, false);
    }

    /**
     *
     *  @param name
     *  @param type
     */
    public void addVariable(String name, Type type) {
        Hashtable table = (Hashtable)_variableStack.peek();
        table.put(name, type);
    }

    /**
     *
     *  @param name
     *  @return
     */
    public Type getVariable(String name) {
        int i = _variableStack.size() - 1;
        if (i == -1)
            return null;

        Hashtable table = (Hashtable)_variableStack.peek();
        while (!table.containsKey(name) && i >= 1)
            table = (Hashtable)_variableStack.get(--i);

        return (Type)table.get(name);
    }

    /**
     *  @return Returns the variableStack.
     */
    public Stack getVariableStack() {
        return _variableStack;
    }

    /**
     *  @param variableStack The variableStack to set.
     */
    public void setVariableStack(Stack variableStack) {
        _variableStack = variableStack;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private fields                     ////

    /** The stack of currently opened scopes for variable
     *  declaration. Each element is a {@link Hashtable}. In each table,
     *  keys are variable names while values are {@link Type}'s of the
     *  corresponding variables.
     */
    private Stack _variableStack = new Stack();

    /** The class loader used to load classes.
     */
    private LocalClassLoader _loader;

    /** The class currently being analyzed, whose declaration is opened
     *  most recently. <tt>null</tt> only when the analyzer is analyzing
     *  the part of source code before any class definition ("package"
     *  and "import").
     */
    private Class _currentClass;

    /** The stack of previously opened class, which has not been closed
     *  yet.
     */
    private Stack _previousClasses = new Stack();

}
