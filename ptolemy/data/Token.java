/* Abstract base class for data capsules.

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

package pt.kernel;

//////////////////////////////////////////////////////////////////////////
//// Token
/** 
Abstract base class for data capsules.
This class declares that tokens are cloneable and promotes the
protected clone() method of the Object base class to public.
In addition, it defines interfaces to initialize the token from
a string and to return a description of the token as a string.
Not all derived classes are required to implement these methods,
so the default implementation here triggers an exception.

@author Edward A. Lee
@version $Id$
@see java.lang.Object
*/
public abstract class Token implements Cloneable {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Promote the clone method of the base class Object to public.
     *  @see java.lang.Object#clone()
     *  @return An identical token.
     *  @exception CloneNotSupportedException May be thrown by derived classes.
     */	
    public Object clone() 
            throws CloneNotSupportedException {
        return super.clone();
    }

    /** Initialize the value of the token from the given string.
     *  In this base class, we just throw an exception.
     *  @exception IllegalActionException Initialization of this token
     *   from a string is not supported.
     */	
    public void fromString(String init) 
            throws IllegalActionException {
                // FIXME: Should throw a new exception: FormatException
        Class myclass = getClass();
        throw new IllegalActionException("Tokens of class "
               + myclass.getName() + " cannot be initialized from a string.");
    }

    /** Return a description of the token as a string.
     *  In this base class, we return the fully qualified class name.
     */	
    public String toString() {
        return getClass().getName();
    }
}
