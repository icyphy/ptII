/* Some object or set of objects has a state that is not permitted.

Copyright (c) 1997-2005 The Regents of the University of California.
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
package ptolemy.kernel.util;

import java.util.ArrayList;
import java.util.Collection;
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
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (cxh)
   @Pt.AcceptedRating Green (cxh)
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
    public InvalidStateException(Nameable object, Throwable cause, String detail) {
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
     *
     *  @deprecated Use InvalidStateException(Collection, String) instead.
     *  @param objects The enumeration of Nameable objects
     *  @param detail The message.
     */
    public InvalidStateException(Enumeration objects, String detail) {
        this(_list(objects), null, detail);
    }

    /** Constructs an exception with a detail message that includes the
     *  names of a collection of nameable objects plus the argument string.
     *  @param objects The Collection of Nameable objects
     *  @param detail The message.
     */
    public InvalidStateException(Collection objects, String detail) {
        this(objects, null, detail);
    }

    /** Constructs an exception with a detail message that includes the
     *  names of a collection of nameable objects plus the argument string.
     *  @param objects The Collection of Nameable objects
     *  @param cause The cause of this exception.
     *  @param detail The message.
     */
    public InvalidStateException(Collection objects, Throwable cause,
            String detail) {
        super(objects, cause, detail);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Convert from an Enumeration to a List.
    //
    // JDK1.4 has a Collections.list(Enumeration) method
    // that would be good to use.
    // For suggestions about converting from Enumerations to Lists,
    // see
    // http://java.sun.com/docs/books/tutorial/collections/interoperability/compatibility.html
    private static List _list(Enumeration objects) {
        List list = new ArrayList();

        while (objects.hasMoreElements()) {
            list.add(objects.nextElement());
        }

        return list;
    }
}
