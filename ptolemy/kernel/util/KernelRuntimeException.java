/* A base class for runtime exceptions.

 Copyright (c) 1997-2001 The Regents of the University of California.
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

@ProposedRating Green (lmuliadi@eecs.berkeley.edu)
@AcceptedRating Green (bart@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

//////////////////////////////////////////////////////////////////////////
//// KernelRuntimeException
/**
Base class for runtime exceptions.  This class extends the basic
Java RuntimeException with a constructor that can take a Nameable as
an argument.

@author Edward A. Lee
@version $Id$
*/
public class KernelRuntimeException extends RuntimeException {

    /** Construct an exception with the given error message.
     *  @param message The message.
     */
    public KernelRuntimeException(String message) {
        _setMessage(message);
    }

    /** Construct an exception originating from the given object,
     *  with the given error message.
     *  @param object The originating object.
     *  @param message The message.
     */
    public KernelRuntimeException(Nameable object, String message) {
        String name;
        if (object == null) {
            name = new String("");;
        } else {
            try {
                name = object.getFullName();
            } catch (InvalidStateException ex) {
                name = object.getName();
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
     *  @param message The message.
     */
    protected void _setMessage(String message) {
        _message = message;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The error message.
    private String _message ;
}
