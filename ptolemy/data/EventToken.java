/* Base class for data capsules.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.data;

import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;

//////////////////////////////////////////////////////////////////////////
//// EventToken
/**
A token representing a pure event.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
*/
public class EventToken extends Token {

    public EventToken() {
    }

    /** Override the base class method to check whether the value of this
     *  token is equal to that of the argument.
     *  Since this base token class does not have any state, this method
     *  returns true if the argument is an instance of Token, but not an
     *  instance of a subclass of Token or any other classes.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of Token, but not an
     *   instance of a subclass of Token or any other classes.
     */
    public boolean equals(Object object) {
        if (object.getClass() == getClass()) {
            return true;
        }
        return false;
    }

    /** Return the type of this token.
     *  @return BaseType.GENERAL
     */
    public Type getType() {
        return BaseType.EVENT;
    }

    /** Return a hash code value for this token. Since the equals() method
     *  in this base Token class returns true for all instances of Token,
     *  all instances of Token must have the same hash code. To achieve this,
     *  this method simply returns the value 0.
     *  @return The integer 0.
     */
    public int hashCode() {
        return 0;
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  This method should be overridden by derived classes.
     *  In this base class, return the String "present" to indicate
     *  that an event is present.
     *  @return The String "present".
     */
    public String toString() {
        return "present";
    }
}
