/* Exception thrown on an attempt to perform an action that would result in an
   inconsistent or contradictory data structure if it were allowed to
   complete.

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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.kvm.kernel.util;

//////////////////////////////////////////////////////////////////////////
//// IllegalActionException
/**
Thrown on an attempt to perform an action that would result in an
inconsistent or contradictory data structure if it were allowed to
complete. E.g., attempt to remove a port from an entity when the
port does not belong to the entity. Another example would be an
attempt to add an item with no name to a named list.

@author Edward A. Lee
@version $Id$
*/
public class IllegalActionException extends KernelException {

    /** Constructs an Exception with a detail message.
     *  @param detail The message.
     */
    public IllegalActionException(String detail) {
        this(null, null, detail);
    }

    /** Constructs an Exception with a detail message that is only the
     *  name of the argument.
     *  @param obj The object.
     */
    public IllegalActionException(Nameable obj) {
        this(null, obj, null);
    }

    /** Constructs an Exception with a detail message that includes the
     *  name of the first argument.
     *  @param obj The object.
     *  @param detail The message.
     */
    public IllegalActionException(Nameable obj, String detail) {
        this(obj, null, detail);
    }

    /** Constructs an Exception with a detail message that consists of
     *  only the names of the obj1 and obj2 arguments.
     *  @param obj1 The first object.
     *  @param obj2 The second object.
     */
    public IllegalActionException(Nameable obj1, Nameable obj2)  {
        this(obj1, obj2, null);
    }

    /** Constructs an Exception with a detail message that includes the
     * names of the obj1 and obj2 arguments.
     *  @param obj1 The first object.
     *  @param obj2 The second object.
     *  @param detail The message.
     */
    public IllegalActionException(Nameable obj1, Nameable obj2,
            String detail) {
        super(obj1, obj2, detail);
    }
}
