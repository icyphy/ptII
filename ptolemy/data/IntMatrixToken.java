/* A token that contains a reference to an IntMatrix.

 Copyright (c) 1997 The Regents of the University of California.
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

package pt.data;

//////////////////////////////////////////////////////////////////////////
//// IntMatrixToken
/** 
A token that contains a reference to an IntMatrix.

@author Yuhong Xiong
$Id$
*/
public class IntMatrixToken extends MatrixToken {

    /** Construct a token with a null IntMatrix.
     */
    public IntMatrixToken() {
    }

    /** Construct a token with the specified IntMatrix.
     */
    public IntMatrixToken(IntMatrix value) {
	_value = value;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Make a deep copy of the token and the matrix it refers to.
     *  @see pt.data.DoubleMatrix#clone()
     *  @return An identical token.
     *  @exception CloneNotSupportedException May be thrown by derived classes.
     */
    public Object clone()
            throws CloneNotSupportedException {
        IntMatrixToken copy = (IntMatrixToken)super.clone();
        copy.setValue((IntMatrix)_value.clone());
	return copy;
    }

    /** Set the value of the token to be the specified matrix.
     */
    public void setValue(IntMatrix value) {
	_value = value;
    }

    /** Return the content in the token as a IntMatrix.
     */
    public IntMatrix intMatrix() {
	return _value;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                        private variables                         ////
    private IntMatrix _value = null;
}

