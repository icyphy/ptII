/* A Particle that contains an integer

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
//// IntToken
/** 
A token that contains an integer, or more specifically a reference to an 
instance of an integer. The reference is never null, the default being 0.

@author Mudit Goel
@version $Id$
*/
public class IntToken extends ObjectToken {

    /** Construct a token with integer 0
     */	
    public IntToken() {
	_value = new Integer(0);
    }

    /** Construct a token with the specified integer
     */
    public IntToken(int value) {
        _value = new Integer(value);
    }

    /** Construct a token with the specified integer in the form of a string
     */
    public IntToken(String value) {
        if (value != null) {
            _value = new Integer(value);
        } else {
            _value = new Integer(0);
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Set the value of the token to the specified string which should refer
     *  to a valid integer. If the value is null, then the value is set to 0, 
     *  rather than null.
     */	
    public void fromString(String value) {
        if (value != null) {
            _value = new Integer(value);
        } else {
            _value = new Integer(0);
        }
    }

    /** Return the value of the token.
     * @returns A reference to an Integer
     */
    public Object getvalue() {
	return _value;
    }

    /** Set the value of the token to be a reference to the specified integer.
     *  If the argument is null, then the value is set to 0 rather than null.
     * @exceptions IllegalActionException Argument is not an integer.
     */
    public void setValue(Object value)
            throws IllegalActionException {
        if (value != null) {
            if (!(value instanceof Integer)) {
                throw new IllegalActionException(
                        "IntToken value must be an integer, not a "
                        + value.getClass().getName());
            }
            _value = value;
        } else {
            _value = new Integer(0);
        }
    }

}
