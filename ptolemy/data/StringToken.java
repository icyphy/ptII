/* A particle that contains a string.

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

//////////////////////////////////////////////////////////////////////////
//// StringToken
/** 
A token that contains a string, or more specifically, a reference
to an instance of String.  The reference is never null, although it may
be an empty string ("").
Note that when this token is cloned, the clone will refer to exactly
the same String object.  However, a String object in Java is immutable,
so there is no risk when two tokens refer to the same string that
one of the strings will be changed.

@author Edward A. Lee
@version $Id$
*/
public class StringToken extends ObjectToken {

    /** Contruct a token with an empty string.
     */	
    public StringToken() {
        this("");
    }

    /** Contruct a token with the specified string.
     */	
    public StringToken(String value) {
        if (value != null) {
            _value = value;
        } else {
            _value = new String("");
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** This method is here for the parser */
    public Token add(Token a, Token b) throws Exception {
        String val1 = a.toString();
        String val2 = b.toString();
        return new StringToken(val1 + val2);
    }

    /** This method is here for the parser */
    public Token subtract(Token a, Token b) throws Exception {
        System.out.println("invalid operation on string objects, sub");
        throw new Exception();
    }

    /** This method is here for the parser */
    public Token multiply(Token a, Token b) throws Exception {
        System.out.println("invalid operation on string objects, mult");
        throw new Exception();
    }

    /** This method is here for the parser */
    public Token divide(Token a, Token b) throws Exception {
        System.out.println("invalid operation on string objects, divide");
        throw new Exception();
    }

    /** Set the value of the token to the specified string.
     *  If the argument is null, then the value is set to an empty string
     *  rather than null.
     */	
    public void fromString(String init) {
        if (init != null) {
            _value = init;
        } else {
            _value = new String("");
        }
    }

    /** Return the value of the token.
     * @returns A reference to a String.
     */	
    public Object getValue() {
        return _value;
    }

    /** Set the value of the token to be a reference to the specified string.
     *  If the argument is null, then the value is set to an empty string
     *  rather than null.
     *  @exceptions IllegalActionException Argument is not a String.
     */	
    public void setValue(Object value) 
            throws IllegalActionException {
        if (value != null) {
            if (!(value instanceof String)) {
                throw new IllegalActionException(
                        "StringToken value must be a String, not a "
                        + value.getClass().getName());
            }
            _value = value;
        } else {
            _value = new String("");
        }
    }

    /** Return the string description of the object.  If there is no such
     *  object, then return a description of the token.
     */	
    public String toString() {
        return (String)_value;
    }
}
