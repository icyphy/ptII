/* Exception thrown on an attempt to add a named object to a collection that
   requires unique names, and finding that there already is an object by that
   name in the collection.

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


//////////////////////////////////////////////////////////////////////////
//// NameDuplicationException

/** Thrown on an attempt to add a named object to a collection that
    requires unique names, and finding that there already is an object
    by that name in the collection.
    Constructors are provided that take 1 or 2 Nameable references
    plus an arbitrary String.  The constructors are robust in that null
    references are ignored.  The preferred constructors are those that
    take two named objects (the container and the would-be containee),
    or two named objects and an arbitrary string (which can be used to
    provide additional information about the error).

    <p>This class has no constructors that take a Throwable cause because
    no such constructors have been needed, but in principle, such constructors
    could be added if needed.

    @author John S. Davis II, Edward A. Lee, Christopher Hylands
    @version $Id$
    @since Ptolemy II 0.2
    @Pt.ProposedRating Green (cxh)
    @Pt.AcceptedRating Green (cxh)
*/
public class NameDuplicationException extends KernelException {
    /** Construct an exception with a detail message that includes the
     *  name of the first argument.  If one or more of the parameters
     *  are null, then the message of the exception is adjusted
     *  accordingly.
     *  @param container The would be container.
     *  @param detail The message.
     */
    public NameDuplicationException(Nameable container, String detail) {
        super(container, null, detail);
    }

    /** Construct an exception with a message that includes the
     *  name of the would be containee and the would be container.
     *  If one or more of the parameters are null, then the
     *  message of the exception is adjusted accordingly.
     *  @param wouldBeContainee The would be containee.
     *  @param container The would be container.
     */
    public NameDuplicationException(Nameable container,
        Nameable wouldBeContainee) {
        this(container, wouldBeContainee, null);
    }

    /** Construct an exception with a detail message that includes the
     *  name of the would be containee and the would be container plus
     *  the third argument string.
     *  If one or more of the parameters are null, then the
     *  message of the exception is adjusted accordingly.
     *  @param wouldBeContainee The would be containee.
     *  @param container The would be container.
     *  @param detail A message.
     */
    public NameDuplicationException(Nameable container,
        Nameable wouldBeContainee, String detail) {
        if (getFullName(container).equals("")) {
            // Note that if wouldBeContainee is null, then we get
            // the 'Attempt to insert object named "" into a'.
            // Note that if wouldBeContainee is the empty string, then we get
            // the 'Attempt to insert object named "<Unnamed Object>" into a'.
            _setMessage("Attempt to insert object named \""
                + getName(wouldBeContainee)
                + "\" into a container that already contains"
                + " an object with that name."
                + ((detail == null) ? "" : (" " + detail)));
        } else {
            _setMessage("Attempt to insert object named \""
                + getName(wouldBeContainee) + "\" into container named \""
                + getFullName(container)
                + "\", which already contains an object with that name."
                + ((detail == null) ? "" : (" " + detail)));
        }
    }
}
