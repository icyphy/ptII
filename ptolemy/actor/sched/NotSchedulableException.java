/* The CompositeActor is not schedulable under a certain algorithm.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu) 3/2/98
*/

package ptolemy.actor.sched;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.util.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// NotSchedulableException
/**
This is a special case of the InvalidStateException such that a
CompositeActor is not schedulable by a certain scheduler.
This class has the same constructors as its supper class, but it also
has an Enumeration that contains the unschedulable
actors in the CompositeActor. The enumeration can be used by other
algorithms to do further scheduling, or by the UI to present to the users.
@author Jie Liu
@version $Id$
@see ptolemy.kernel.util.InvalidStateException
*/
public class NotSchedulableException extends InvalidStateException {

    /** Constructs an Exception with only a detail message.
     *  The unschedulable actors are set to null.
     *  @param detail The message.
     */
    public NotSchedulableException(String detail) {
        super(detail);
        _unschedulableActors = null;
    }

    /** Constructs an Exception with a detail message that includes the
     *  name of the first argument and the second argument string.
     *  The unschedulable actors are set to null.
     *  @param obj The object.
     *  @param detail The message.
     */
    public NotSchedulableException(Nameable obj, String detail) {
        super(obj, detail);
        _unschedulableActors = null;
    }

    /** Constructs an Exception with a detail message that includes the
     *  names of the first two arguments plus the third argument string.
     *  The unschedulable actors are set to null.
     *  @param obj1 The first object.
     *  @param obj2 The second object.
     *  @param detail The message.
     */
    public NotSchedulableException(Nameable obj1, Nameable obj2,
            String detail) {
        super(obj1, obj2, detail);
        _unschedulableActors = null;
    }

    /** Constructs an Exception with a detail message that includes the
     *  names of an enumeration of nameable plus the an argument string. An
     *  addition Enumeration of unschedulable actors is also set. This
     *  enumeration may be used by the UI to illustrate the unschedulable
     *  actors, or by the directors to perform some actions to correct
     *  the error.
     *  @param obj The object.
     *  @param detail The message.
     *  @param actors The unschedulable actors.
     */
    public NotSchedulableException(Enumeration actors, String detail) {
        super(actors, detail);
        _unschedulableActors = actors;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the unschedulable actors enumeration.
     *  @return the unschedulable actors enumeration.
     */
    public Enumeration getUnschedulableActors() {
        return _unschedulableActors;
    }

    /** Return true if the unschedulable actors has been set.
     *  @return True if the unschedulable actors has been set.
     */
    public boolean hasUnschedulableActors() {
        return _unschedulableActors != null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The Enumeration of actors that are not schedulable.
    private Enumeration _unschedulableActors = null;
}
