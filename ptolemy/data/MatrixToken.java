/* A token that contains a reference to a matrix.

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

package pt.data;

import pt.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// MatrixToken
/** 
Abstract base class for tokens that contain a reference to a matrix.
The derived classes should override the clone() method to do a deep copy
of the token.

@author Yuhong Xiong
$Id$
*/
public abstract class MatrixToken extends Token {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Return the content in the token as a ByteMatrix.
     *  In this base class, we just throw an exception.
     *  @exception IllegalActionException
     */
/*
    public ByteMatrix byteMatrix()
	    throws IllegalActionException {
	Class myclass = getClass();
        throw new IllegalActionException("Value in class "
              + myclass.getName() + " cannot be converted to a ByteMatrix.");
    }
*/

    /** Return the content in the token as a DoubleMatrix.
     *  In this base class, we just throw an exception.
     *  @exception IllegalActionException
     */
    public DoubleMatrix doubleMatrix()
            throws IllegalActionException {
        Class myclass = getClass();
        throw new IllegalActionException("Value in class "
              + myclass.getName() + " cannot be converted to a DoubleMatrix.");
    }
 
    /** Return the content in the token as a ComplexMatrix.
     *  In this base class, we just throw an exception.
     *  @exception IllegalActionException
     */
/*
    public ComplexMatrix complexMatrix()
            throws IllegalActionException {
        Class myclass = getClass();
        throw new IllegalActionException("Value in class "
              + myclass.getName() + " cannot be converted to a ComplexMatrix.");
    }
*/
 
    /** Return the content in the token as a FixMatrix.
     *  In this base class, we just throw an exception.
     *  @exception IllegalActionException
     */
/*
    public FixMatrix fixMatrix()
            throws IllegalActionException {
        Class myclass = getClass();
        throw new IllegalActionException("Value in class "
              + myclass.getName() + " cannot be converted to a FixMatrix.");
    }
*/
 
    /** Return the content in the token as a IntMatrix.
     *  In this base class, we just throw an exception.
     *  @exception IllegalActionException
     */
/*
    public IntMatrix intMatrix()
            throws IllegalActionException {
        Class myclass = getClass();
        throw new IllegalActionException("Value in class "
              + myclass.getName() + " cannot be converted to a IntMatrix.");
    }
*/
 
    /** Return the content in the token as a LongMatrix.
     *  In this base class, we just throw an exception.
     *  @exception IllegalActionException
     */
/*
    public LongMatrix longMatrix()
            throws IllegalActionException {
        Class myclass = getClass();
        throw new IllegalActionException("Value in class "
              + myclass.getName() + " cannot be converted to a LongMatrix.");
    }
*/
}

