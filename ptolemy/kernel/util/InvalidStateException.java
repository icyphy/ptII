/* Some object or set of objects has a state that in theory is not permitted.

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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// InvalidStateException
/**
Some object or set of objects has a state that in theory is not
permitted. E.g., a NamedObj has a null name. Or a topology has
inconsistent or contradictory information in it, e.g. an entity
contains a port that has a different entity as it container. Our
design should make it impossible for this exception to ever occur,
so occurrence is a bug.

@author Edward A. Lee, Jie Liu
@version $Id$
*/
public class InvalidStateException extends KernelRuntimeException {


    /** Constructs an Exception with only a detail message.
     *  @param detail The message.
     */
    public InvalidStateException(String detail) {
        this(null, null, null, detail);
    }

    /** Constructs an Exception with a detail message that includes the
     *  name of the first argument and the second argument string.
     *  @param obj The object.
     *  @param detail The message.
     */
    public InvalidStateException(Nameable obj, String detail) {
        this(obj, null, null, detail);
    }

    /** Constructs an Exception with a detail message that includes the
     *  names of the first two arguments plus the third argument string.
     *  @param obj1 The first object.
     *  @param obj2 The second object.
     *  @param detail The message.
     */
    public InvalidStateException(Nameable obj1, Nameable obj2,
            String detail) {
        this(obj1, obj2, null, detail);
    }

    /** Constructs an Exception with a detail message that includes the
     *  names of the first two arguments plus the third argument string.
     *  @param obj1 The first object.
     *  @param obj2 The second object.
     *  @param detail The message.
     */
    public InvalidStateException(Nameable obj1, Nameable obj2,
            Throwable cause, String detail) {
        super(obj1, obj2, cause, detail);
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
     *  @deprecated This method is weird
     *  @param objects The enumeration of Nameable objects
     *  @param cause The cause of this exception.
     *  @param detail The message.
     */
    public InvalidStateException(Enumeration objects,
				  Throwable cause, String detail) {
        String prefix = "";
        String name;
        while(objects.hasMoreElements()) {
            Object obj = objects.nextElement();
            if (obj instanceof Nameable) {
                name = KernelException._getFullName((Nameable)obj);
            } else {
                name = "<Object of class " +
                    (obj.getClass()).getName() + ">";
            }
            prefix += name + ", ";
        }
        prefix = prefix.substring(0, prefix.length()-2);
	_cause = cause;
        _setMessage(KernelException._generateMessage(prefix, _cause, detail));
    }

    /** Constructs an exception with a detail message that includes the
     *  names of a list of nameable objects plus the argument string.
     *  @param objects The enumeration of Nameable objects
     *  @param detail The message.
     */
    public InvalidStateException(List objects, String detail) {
        this(Collections.enumeration(objects), null, detail);
    }
}
