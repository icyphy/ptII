/* The CompositeActor is not schedulable under a certain algorithm.

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.actor;
import ptolemy.kernel.util.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// NotSchedulableException
/**
This is a special case of InvelidStateException that a CompositeActor
is not schedulable by a certain scheduler. The exception has an
Enumeration that contains the possiblly unschedulable actors in the
CompositeActor. The enumeration can be used by other algorithms
to further schedule, or by UI to present to the user.
@author Jie Liu
@version $Id$
@see InvalidStateException
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
     *  @param obj The object.
     *  @param detail The message.
     */
    public NotSchedulableException(Nameable obj, String detail) {
        super(obj, detail);
    }

    /** Constructs an Exception with a detail message that includes the
     *  names of the first two arguments plus the third argument string.
     *  @param obj1 The first object.
     *  @param obj2 The second object.
     *  @param detail The message.
     */
    public NotSchedulableException(Nameable obj1, Nameable obj2,
            String detail) {
        super(obj1, obj2, detail);
    }

    /** Constructs an Exception with a detail message that includes the
     *  name of the first argument, the second argument string, and
     *  an Enumeration of unschedulable actors.
     *  @param obj The object.
     *  @param detail The message.
     *  @param actors The unschedulable actors.
     */
    public NotSchedulableException(Nameable obj, String detail,
            Enumeration actors) {
        super(obj, detail);
        _actors= actors;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the unschedulable actors enumeration.
     *  @return the unschedulable actors enumeration.
     */
    public Enumeration problematicActors() {
        return _actors;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.
    private Enumeration _actors= null;
}
