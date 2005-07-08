/* An abstract base class for a schedule element.

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

 */
package ptolemy.actor.sched;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// ScheduleElement

/**
 This is an abstract base class for a schedule element. Instances of the
 Schedule and Firing subclasses are used to construct a static schedule.
 A schedule can be thought of as a structure that consists of an iteration
 count and a list of schedule elements. A schedule element can contain
 an actor, or it can contain another schedule. It is usually required that,
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
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (vogel)
 @Pt.AcceptedRating Yellow (chf)
 @see ptolemy.actor.sched.Firing
 @see ptolemy.actor.sched.Schedule
 */
public abstract class ScheduleElement {
    /** Construct a schedule element with an iteration count of 1 and
     *  with no parent schedule element. The constructor that takes
     *  a parameter should be used when constructing a schedule
     *  element that is contained by another schedule element.
     */
    public ScheduleElement() {
        super();
        _scheduleVersion = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the actor invocation sequence of the schedule in the
     *  form of a sequence of actors. All of the lowest-level nodes
     *  should be an instance of Firing. Otherwise, the returned
     *  iterator will contain null elements.
     *
     *  @return An iterator over a sequence of actors.
     */
    public abstract Iterator actorIterator();

    /** Return the actor invocation sequence in the form
     *  of a sequence of firings. All of the lowest-level nodes
     *  should be an instance of Firing. Otherwise, the returned
     *  iterator will contain null elements.
     *
     *  @return An iterator over a sequence of firings.
     */
    public abstract Iterator firingIterator();

    /** Return the iteration count for this schedule. This method
     *  returns the iteration count that was set by
     *  setIterationCount(). If setIterationCount() was never invoked,
     *  then a value of one is returned.
     *
     *  @return The iteration count for this schedule.
     *  @see #setIterationCount(int)
     */
    public int getIterationCount() {
        return _iterationCount;
    }

    /** Set the iteration count for this schedule. The
     *  getIterationCount() method will return the value set
     *  by this method. If this method is not invoked, a default
     *  value of one will be used.
     *
     *  @param count The iteration count for this schedule.
     *  @see #getIterationCount()
     */
    public void setIterationCount(int count) {
        _incrementVersion();
        _iterationCount = count;
    }

    /** Set the parent schedule element of this schedule element to
     *  the specified schedule element. If this schedule element is
     *  added to another schedule element (the parent), then the
     *  add() method of the parent will invoke this method.
     *  This association is used to notify the parent schedule
     *  element when changes are made to this schedule element.
     *
     *  @param parent The parent schedule element of this schedule
     *   element.
     */
    public void setParent(ScheduleElement parent) {
        _parent = parent;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the current version of this schedule element. The
     *  version changes whenever a structural change is made to
     *  this schedule element.
     *
     *  @return The current version of this schedule element.
     */
    protected long _getVersion() {
        return _scheduleVersion;
    }

    /** Increment the version of this schedule element and if this schedule
     *  element has a parent schedule, increment the version of the parent
     *  schedule as well. This method will therefore cause a version update
     *  to propagate up to all parent schedule elements. This method is
     *  called when a structure change is made to this schedule element, and
     *  is also called by the immediate children of this schedule element
     *  when they are modified.
     */
    protected void _incrementVersion() {
        _scheduleVersion++;

        if (_parent != null) {
            _parent._incrementVersion();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The parent schedule of this schedule. Null means this schedule
     *  has no parent.
     */
    protected ScheduleElement _parent = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The iteration count for this schedule element.
    private int _iterationCount = 1;

    // The current version of this schedule.
    private long _scheduleVersion;
}
