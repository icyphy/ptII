/* A base class for tokens that contain a reference to a scalar object.

 Copyright (c) 1997- The Regents of the University of California.
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

package ptolemy.data;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// ScalarToken
/**
A token that contains a reference to an arbitrary scalar object.
It provides interface for type conversion among different scalar types.

@author Yuhong Xiong, Mudit Goel
@version $Id$
*/
public abstract class ScalarToken extends Token {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Return the value in the token as a byte.
      * In this base class, we just throw an exception.
      * @exception IllegalActionException thrown in this base class.
      */
    public byte byteValue()
	    throws IllegalActionException {
	Class myclass = getClass();
	throw new IllegalActionException("Value in class "
		+ myclass.getName() + " cannot be converted to a byte" +
		"without loss of information.");
    }

    // Return the value in the token as a Complex.
    // In this base class, we just throw an exception.
    // @exception IllegalActionException thrown in this base class.
    //
    // FIXME: restore this method after the Complex class is available.
//    public Complex complexValue()
//	    throws IllegalActionException {
//	Class myclass = getClass();
//	throw new IllegalActionException("Value in class "
//		+ myclass.getName() + " cannot be converted to a Complex" +
//		"without loss of information.");
//    }

    /** Return the value in the token as a double.
      * In this base class, we just throw an exception.
      * @exception IllegalActionException thrown in this base class.
      */
    public double doubleValue()
	    throws IllegalActionException {
	Class myclass = getClass();
	throw new IllegalActionException("Value in class "
		+ myclass.getName() + " cannot be converted to a double" +
		"without loss of information.");
    }

    // Return the value in the token as a Fix.
    // In this base class, we just throw an exception.
    // @exception IllegalActionException thrown in this base class.
    //
    // FIXME: restore this method after the Fix class is available.
//    public Fix fixValue()
//	    throws IllegalActionException {
//	Class myclass = getClass();
//	throw new IllegalActionException("Value in class "
//		+ myclass.getName() + " cannot be converted to a Fix" +
//		"without loss of information.");
//    }

    /** Return the value in the token as an int.
      * In this base class, we just throw an exception.
      * @exception IllegalActionException thrown in this base class.
      */
    public int intValue()
	    throws IllegalActionException {
	Class myclass = getClass();
	throw new IllegalActionException("Value in class "
		+ myclass.getName() + " cannot be converted to an int" +
		"without loss of information.");
    }

    /** Return the value in the token as a long integer.
      * In this base class, we just throw an exception.
      * @exception IllegalActionException thrown in this base class.
      */
    public long longValue()
	    throws IllegalActionException {
	Class myclass = getClass();
	throw new IllegalActionException("Value in class "
	      + myclass.getName() + " cannot be converted to a long integer" +
		"without loss of information.");
    }
}
