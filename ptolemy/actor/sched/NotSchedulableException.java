/* The CompositeActor is not schedulable under a certain algorithm.

Copyright (c) 1998-2005 The Regents of the University of California.
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

saving the actors as an enumeration, but this value was not
used anywhere so I pulled out that code.
*/
package ptolemy.actor.sched;

import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.Nameable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;


//////////////////////////////////////////////////////////////////////////
//// NotSchedulableException

/**
   This is a special case of the InvalidStateException such that a
   CompositeActor is not schedulable by a certain scheduler.
   @author Jie Liu, Christopher Hylands
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Red (liuj)
   @Pt.AcceptedRating Red (cxh) This class was
   @see ptolemy.kernel.util.InvalidStateException
*/
public class NotSchedulableException extends InvalidStateException {
    /** Constructs an Exception with only a detail message.
     *  @param detail The message.
     */
    public NotSchedulableException(String detail) {
        super(detail);
    }

    /** Constructs an Exception with a detail message that includes the
     *  name of the first argument and the second argument string.
     *  @param nameable The object.
     *  @param detail The message.
     */
    public NotSchedulableException(Nameable nameable, String detail) {
        super(nameable, detail);
    }

    /** Constructs an Exception with a detail message that includes the
     *  names of the first two arguments plus the third argument string.
     *  @param nameable1 The first object.
     *  @param nameable2 The second object.
     *  @param detail The message.
     */
    public NotSchedulableException(Nameable nameable1, Nameable nameable2,
            String detail) {
        super(nameable1, nameable2, detail);
    }

    /** Constructs an Exception with a detail message that includes the
     *  names of an enumeration of nameables and the detail argument.
     *  @deprecated Use
     *    NotSchedulableException(Collection, Throwable, String) instead.
     *  @param actors The unschedulable actors.
     *  @param detail The message.
     */
    public NotSchedulableException(Enumeration actors, String detail) {
        this(_list(actors), null, detail);
    }

    /** Constructs an Exception with a detail message that includes the
     *  names of a Collection of nameables, the causing Throwable
     *  and the detail argument.
     *  @param actors The unschedulable actors.
     *  @param cause The cause.
     *  @param detail The message.
     */
    public NotSchedulableException(Collection actors, Throwable cause,
            String detail) {
        super(actors, cause, detail);
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
