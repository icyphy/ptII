/* An abstract base class for a schedule element.

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

@ProposedRating Yellow (vogel@eecs.berkeley.edu)
@AcceptedRating 
*/

package ptolemy.actor.sched;

import java.util.LinkedList;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// ScheduleElement
/**
This is an abstract base class for a schedule element. Instances of the
Schedule and Firing subclasses are used to construct a static schedule.
A schedule can be thought of as a structure that consists of an iteration
count and a list of schedule elements. A schedule element can contain
an actor, or it can contain another schedule. For a valid schedule,
all of the lowest-level schedule elements must contain an actor. It is
up to the scheduler to enforce this, however. The Schedule class is a 
schedule element that contains a schedule. The Firing class is a schedule 
element that contains an actor. Therefore,
the top-level schedule element must be an instance of Schedule, and all
of the lowest-level elements must each be an instance of Firing.
<p>
This base class implements the getIterationCount() and setIterationCount()
methods, which return and set the iteration count for this schedule element.
A default value of 1 is used for the iteration count.

@author Brian K. Vogel
@version $Id$
@see ptolemy.actor.sched.Firing
@see ptolemy.actor.sched.Schedule
*/

public abstract class ScheduleElement {

    /** Construct a schedule element with an iteration count of 1.
     */
    public ScheduleElement() {
	super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the actor invocation sequence of the schedule in the 
     *  form of a sequence of actors. For a valid schedule, all of the
     *  lowest-level nodes should be an instance of Firing. If the
     *  schedule is not valid, then the returned iterator will contain 
     *  null elements.
     *  
     * @return An iterator over a sequence of actors.
     */
    public abstract Iterator actorIterator();

    /** Return the actor invocation sequence in the form
     *  of a sequence of firings. For a valid schedule, all of the
     *  lowest-level nodes must be an instance of Firing. If not, then
     *  the returned iterator will contain null elements.
     *  
     *  @return An iterator over a sequence of firings.
     */
    public abstract Iterator firingIterator();

    /** Return the iteration count for this schedule. This method
     *  returns the iteration count that was set by
     *  setIterationCount(). If setIterationCount() was never invoked,
     *  then a value of one is returned.
     *  @return The iteration count for this schedule.
     */
    public int getIterationCount() {
	return _iterationCount;
    }

    /** Set the iteration count for this schedule. The
     *  getIterationCount() method will return the value set
     *  by this method. If this method is not invoked, a default 
     *  value of one will be used.
     *  @param count The iteration count for this schedule.
     */
    public void setIterationCount(int count) {
	_iterationCount = count;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The iteration count for this schedule element.
    private int _iterationCount = 1;
}
