/* Some object or set of objects has a state that is not permitted.

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

@ProposedRating Green (cxh@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// InvalidStateException
/**
Some object or set of objects has a state that is not
permitted. E.g., a NamedObj has a null name. Or a topology has
inconsistent or contradictory information in it, e.g. an entity
contains a port that has a different entity as it container. Our
design should make it impossible for this exception to ever occur,
so occurrence is a bug.

@author Edward A. Lee, Jie Liu
@version $Id$
*/
public class InvalidStateException extends KernelRuntimeException {


    /** Construct an exception with only a detail message.
     *  @param detail The message.
     */
    public InvalidStateException(String detail) {
        this(null, null, null, detail);
    }

    /** Construct an exception with a detail message that includes the
     *  name of the first argument, the cause and the third argument string.
     *  @param object The nameable object involved in the exception
     *  @param cause The cause of this exception.
     *  @param detail The message.
     */
    public InvalidStateException(Nameable object,
            Throwable cause, String detail) {
        super(object, null, cause, detail);
    }

    /** Construct an exception with a detail message that includes the
     *  name of the first argument and the second argument string.
     *  @param object The object.
     *  @param detail The message.
     */
    public InvalidStateException(Nameable object, String detail) {
        this(object, null, null, detail);
    }

    /** Construct an exception with a detail message that includes the
     *  names of the first two arguments plus the third argument string.
     *  @param object1 The first object.
     *  @param object2 The second object.
     *  @param detail The message.
     */
    public InvalidStateException(Nameable object1, Nameable object2,
            String detail) {
        this(object1, object2, null, detail);
    }

    /** Construct an exception with a detail message that includes the
     *  names of the first two arguments plus the third argument string.
     *  @param object1 The first object.
     *  @param object2 The second object.
     *  @param cause The cause of this exception.
     *  @param detail The message.
     */
    public InvalidStateException(Nameable object1, Nameable object2,
            Throwable cause, String detail) {
        super(object1, object2, cause, detail);
    }

    /** Construct an exception with a detail message that includes the
     *  names of an enumeration of nameable object plus the argument string.
     *  @param objects The enumeration of Nameable objects
     *  @param detail The message.
     */
    public InvalidStateException(Enumeration objects, String detail) {
        this(objects, null, detail);
    }

    /** Construct an exception with a detail message that includes the
     *  names of an enumeration of nameable object, the detail message
     *  of the cause plus the argument string.  If one or more of the
     *  parameters are null, then the detail message is adjusted
     *  accordingly.
     *
     *  @param objects The enumeration of Nameable objects
     *  @param cause The cause of this exception.
     *  @param detail The message.
     */
    public InvalidStateException(Enumeration objects,
				  Throwable cause, String detail) {
        String prefix = "";
        String name;
        while(objects.hasMoreElements()) {
            Object object = objects.nextElement();
            if (object instanceof Nameable) {
                name = KernelException.getFullName((Nameable)object);
            } else {
                name = "<Object of class " +
                    object.getClass().getName() + ">";
            }
            prefix += name + ", ";
        }
        if (prefix.length() >= 2 ) {
            // Remove the trailing ", " which was added processing the
            // last element of the list.
            prefix = prefix.substring(0, prefix.length()-2);
        }
        _setMessage(KernelException.generateMessage(prefix, cause, detail));
        _setCause(cause);
    }

    /** Constructs an exception with a detail message that includes the
     *  names of a list of nameable objects plus the argument string.
     *  @param objects The List of Nameable objects
     *  @param detail The message.
     */
    public InvalidStateException(List objects, String detail) {
        this(Collections.enumeration(objects), null, detail);
    }
}
