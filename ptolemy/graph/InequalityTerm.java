/** An interface for a term in an inequality over a CPO.

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

package pt.graph;
import pt.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// InequalityTerm
/**
An interface for a term in an inequality over an underlining CPO.
The classes implementing this interface model constants, variables,
or functions.

@author Yuhong Xiong
$Id$
@see CPO
*/

public interface InequalityTerm {
    /** Sets the value of this term to the specified CPO element.
     *  @param e an object representing an element in the
     *   underlining CPO.
     *  @exception IllegalActionException this term is not a variable.
     *  @exception IllegalArgumentException The specified object
     *   is not an element in the CPO.
     */
    public void set(Object e)
            throws IllegalActionException;

    /** Checks if this term can be set to a constant.  Only a variable
     *  can be set, constants and functions cannot.
     *  @returns <code>true</code> if this term is a variable;
     *   <code>false</code> otherwise.
     */
    public boolean settable();
 
    /** Returns the value of this term.  If this term is a constant,
     *  that constant is returned; if this term is a variable, the
     *  current value of that variable is returned; if this term
     *  is a function, the value of the function based on the current
     *  value of variables in the function is returned.
     *  @return an object representing an element in the underlining CPO.
     */
    public Object value();
}

