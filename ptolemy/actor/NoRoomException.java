/* Exception thrown on an attempt to put a token (via the put() method) 
   into a receiver that doesn't have room to accomodate one (i.e. 
   hasRoom() returns false).

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

@ProposedRating Red (lmuliadi@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// NoRoomException
/**
This exception is thrown on an attempt to put a token (via the 
put() method) into a receiver that doesn't have room to accomodate one 
(i.e. hasRoom() returns false).
@author Lukito Muliadi
@version $Id$
@see Receiver
*/
public class NoRoomException extends RuntimeException {

    /** Constructs an Exception with a detail message.
     *  @param detail The message.
     */
    public NoRoomException(String detail) {
        _setMessage(detail);
    }

    /** Constructs an Exception with a detail message and an object
     *  that originated the exception.
     */
    public NoRoomException(Nameable obj, String detail) {
        String name;
        if (obj == null) {
            name=new String("");;
        } else {
            try {
                name = obj.getFullName();
            } catch (InvalidStateException ex) {
                name = obj.getName();
            }
        }
        _setMessage(name + ": " + detail);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the detail message. */
    public String getMessage() {
        return _message;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Sets the error message to the specified string.
     *  @param msg The message.
     */
    protected void _setMessage(String msg) {
        _message = msg;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The detail message.
    private String _message ;
}












