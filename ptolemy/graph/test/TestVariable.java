/** A variable InequalityTerm.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

package ptolemy.graph.test;

import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// TestVariable
/**
A variable InequalityTerm.
This class is for testing inequality related classes.
The value of this InequalityTerm is a String.
This term has name, which is used for printing test result.

@author Yuhong Xiong
@version $Id$
*/

public class TestVariable implements InequalityTerm {

    /** Construct a variable InequalityTerm with a null initial value.
     */
    public TestVariable() {
    }

    /** Construct a variable InequalityTerm with the specified
     *  initial value.
     *  @param value A String
     */
    public TestVariable(String value) {
	_value = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Disallow the value of this term to be set.
     */
    public void fixValue() {
	_valueFixed = true;
    }

    /** Return the string value.
     *  @return A String
     */
    public Object getAssociatedObject() {
	return _value;
    }

    /** Return the information of this term. The information is a
     *  String of the form: <name>(variable)_<value>
     *  @return A String
     */
    public String getInfo() {
        return _name + "(variable)_" + getValue();
    }

    /** Return the String value of this term.
     *  @return a String
     */
    public Object getValue() {
	return _value;
    }

    /** Return an array of size one. The element of the array is
     *  the this reference.
     *  @return an array of InequalityTerms
     */
    public InequalityTerm[] getVariables() {
	if (isSettable()) {
	    InequalityTerm[] variable = new InequalityTerm[1];
	    variable[0] = this;
	    return variable;
	} else {
	    return new InequalityTerm[0];
	}
    }

    /** Set the value of this variable to the specified String.
     *  @param e a String
     *  @exception IllegalActionException not thrown
     */
    public void initialize(Object e)
            throws IllegalActionException {
	if (isSettable()) {
	    _value = (String)e;
	} else {
	    throw new IllegalActionException("TestVariable.initialize: " +
		"This term is not settable.");
	}
    }

    /** Return true.
     *  @return true
     */
    public boolean isSettable() {
	return !_valueFixed;
    }

    /** Check whether the current value of this term is acceptable,
     *  and return true if it is.  In this class, a value is always
     *  acceptable.
     *  @return True.
     */
    public boolean isValueAcceptable() {
        return true;
    }

    /** Set the name of this variable. If the specified String is null,
     *  Set the name to an empty String.
     *  @param A String
     */
    public void setName(String name) {
	if (name != null) {
	    _name = name;
	} else {
	    _name = "";
	}
    }

    /** Set the value of this variable to the specified String.
     *  @param e a String
     *  @exception IllegalActionException not thrown
     */
    public void setValue(Object e)
            throws IllegalActionException {
	if (isSettable()) {
	    _value = (String)e;
	} else {
	    throw new IllegalActionException("TestVariable.isSettable: " +
		"value is not settable.");
	}
    }


    /** Override the base class to describe the variable.
     *  @return A string describing the variable.
     */
    public String toString() {
        return getClass().getName() + getInfo();
    }

    /** Allow the value of this term to be changed.
     */
    public void unfixValue() {
	_valueFixed = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private String _name = "";
    private String _value = null;
    private boolean _valueFixed = false;
}
