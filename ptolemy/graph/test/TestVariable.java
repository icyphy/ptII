/** A variable InequalityTerm.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

@author Yuhong Xiong
$Id$
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
	InequalityTerm[] variable = new InequalityTerm[1];
	variable[0] = this;
	return variable;
    }

    /** Return true.
     *  @return true
     */
    public boolean isSettable() {
	return true;
    }
 
    /** Set the value of this variable to the specified String.
     *  @param e a String
     *  @exception IllegalActionException not thrown
     */
    public void setValue(Object e)
            throws IllegalActionException {
	_value = (String)e;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    private String _value = null;
}

