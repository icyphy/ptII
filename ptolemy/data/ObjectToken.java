/* A token that contains a reference to an arbitrary object.

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

package pt.data;

import pt.kernel.*;
import pt.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ObjectToken
/** 
A token that contains a reference to an arbitrary object.
Note that when this token is cloned, the clone will refer to exactly
the same object.  Thus, care must be exercised to ensure that actors do
not modify that object in a nondeterministic way, unless such nondeterminism
is acceptable.  Note further that there is no way to implement fromString()
for an arbitrary object, so we accept the base class implementation,
which triggers an exception.

@author Edward A. Lee
@version $Id$
*/
public class ObjectToken extends Token {

    /** Contruct an empty token.
     */	
    public ObjectToken() {
        super();
    }

    /** Contruct a token with a reference to the specified object.
     *  @exceptions IllegalActionException Argument is not of the appropriate
     *   type (may be thrown by derived classes, but is not thrown here).
     */	
    public ObjectToken(Object value)
            throws IllegalActionException {
        setValue(value);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Return the value of the token, a reference to an object.
     	FIXME: this method should only be in leaf classes
    public Object getValue() {
        return _value;
    }
    */

 /** Return the value of the token, a reference to an object.
     	FIXME: this method should only be in leaf classes */
    public Object getObject() {
        return _value;
    }
    

    /** Set the value of the token to be a reference to the specified object.
     *  @exceptions IllegalActionException Argument is not of the appropriate
     *   type (may be thrown by derived classes, but is not thrown here).
     */	
    public void setValue(Object value)
            throws IllegalActionException {
        _value = value;
    }

    /** Return the string description of the object.  If there is no such
     *  object, then return a description of the token.
     */	
    public String toString() {
        if (_value != null) {
            return _value.toString();
        } else {
            return super.toString();
        }
    }

    /////////////////////////////////////////////////////////////////////////
    ////                        protected variables                      ////

    // This is protected to allow access in derived classes only.
    protected Object _value = null;
}
