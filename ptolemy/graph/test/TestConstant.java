/** A constant InequalityTerm.

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
//// TestConstant
/**
A constant InequalityTerm.
This class is for testing inequality related classes.
The value of this InequalityTerm is a String set in the constructor.
This term has name, which is used for printing test result.

@author Yuhong Xiong
@version $Id$
*/

public class TestConstant implements InequalityTerm {

    /** Construct a constant InequalityTerm with a String value.
     *  @param value A String
     */
    public TestConstant(String value) {
	_value = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do nothing.
     */
    public void fixValue() {}

    /** Return the string value.
     *  @return A String
     */
    public Object getAssociatedObject() {
	return _value;
    }

    /** Return the information of this term. The information is a
     *  String of the form: <name>(constant)_<value>
     *  @return A String
     */
    public String getInfo() {
        return _name + "(constant)_" + getValue();
    }

    /** Return the constant String value of this term.
     *  @return a String
     */
    public Object getValue() {
	return _value;
    }

    /** Return an array of size zero.
     *  @return an array of InequalityTerms
     */
    public InequalityTerm[] getVariables() {
	return new InequalityTerm[0];
    }

    /** Return false.
     *  @return false
     */
    public boolean isSettable() {
	return false;
    }

    /** Throw an Exception.
     *  @exception IllegalActionException Always thrown since this term is a
     *   constant.
     */
    public void initialize(Object e)
	    throws IllegalActionException {
	throw new IllegalActionException("TestConstant.initialize: This term "
		+ "is a constant.");
    }

    /** Check whether the current value of this term is acceptable,
     *  and return true if it is.  In this class, a value is always
     *  acceptable.
     *  @return True.
     */
    public boolean isValueAcceptable() {
        return true;
    }

    /** Set the name of this constant. If the specified String is null,
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

    /** Throw an Exception.
     *  @param e an Object. Ignored by this method.
     *  @exception IllegalActionException always thrown.
     */
    public void setValue(Object e)
            throws IllegalActionException {
	throw new IllegalActionException("TestConstant.setValue: This term " +
		"is a constant.");
    }

    /** Override the base class to describe the constant.
     *  @return A string describing the constant
     */
    public String toString() {
        return getClass().getName() + getInfo();
    }

    /** Do nothing.
     */
    public void unfixValue() {}

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private String _name = "";
    private String _value = null;
}
