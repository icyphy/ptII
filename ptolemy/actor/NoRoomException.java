/* A class for indicating that a receiver failed to accept a token.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Yellow (lmuliadi@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// NoRoomException
/**
This exception is thrown when an attempt is made to put a token
into a receiver that doesn't have room to accommodate one.
To avoid this exception, code should use the hasRoom() method in the
Receiver interface to determine if there is room for a token.

@author Lukito Muliadi
@version $Id$
@see Receiver
*/
public class NoRoomException extends RuntimeException {

    /** Construct a NoRoomException with the given error message.
     *  @param message The message.
     */
    public NoRoomException(String message) {
        _setMessage(message);
    }

    /** Construct a NoRoomException originating from the given object,
     *  with the given error message.
     *  @param obj The originating object.
     *  @param message The message.
     */
    public NoRoomException(Nameable obj, String message) {
        String name;
        if (obj == null) {
            name = new String("");;
        } else {
            try {
                name = obj.getFullName();
            } catch (InvalidStateException ex) {
                name = obj.getName();
            }
        }
        _setMessage(name + ": " + message);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the error message. */
    public String getMessage() {
        return _message;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set the error message to the given string.
     *  @param msg The message.
     */
    protected void _setMessage(String message) {
        _message = message;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The error message.
    private String _message ;
}
