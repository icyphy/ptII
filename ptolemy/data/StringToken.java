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

@author Edward A. Lee, Neil Smyth
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

    /** Add the value of the argument Token to this Token. Type resolution
     *  also occurs here, with the returned Token type chosen to achieve
     *  a lossless conversion.
     * FIXME: what do do about long in the next six methods?
     *  @param a The token to add to this Token
     *  @exception Thrown if the passed token is not of a type that can be 
     *   added to this Tokens value in a lossless fashion.
     */
    public Token add(Token a) throws IllegalActionException {
        String result = toString() + a.toString();
        return new StringToken(result);
    }
     
   
    /** Test the values of this Token and the argument Token for equality.
     *  Type resolution also occurs here, with the returned Token type 
     *  chosen to achieve a lossless conversion. 
     *  @param a The token to divide this Token by
     *  @exception Thrown if the passed token is not of a type that can be 
     *   compared this Tokens value.
     */
    public BooleanToken equality(Token a) throws IllegalActionException {
        if (a instanceof StringToken) {
            if ( _value.compareTo(a.toString()) == 0) {
                return new BooleanToken(true);
            } else {
                return new BooleanToken(false);
            } 
       } else {
            String str = "supported between " + this.getClass().getName();
            str = str + " and " + a.getClass().getName();
            throw new IllegalActionException("equality method not " + str);
        }
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

   /**  Get the value of the String currently contained in this token.
    *   @return The value currently contained.
    */
    public String getValue() {
        return _value;
    }


    /** Set the value of the token to be a reference to the specified string.
     *  If the argument is null, then the value is set to an empty string
     *  rather than null.
     *  @exceptions IllegalActionException Argument is not a String.
     */	
    public void setValue(String value) 
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
     *  @return The String contained in this token.
     */	
    public String toString() {
        return (String)_value;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                        private variables                         ////
    private String _value;
}
