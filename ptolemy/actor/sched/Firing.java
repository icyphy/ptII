/* A schedule element that contains a reference to an actor.

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
@AcceptedRating Yellow (chf@eecs.berkeley.edu)
*/

package ptolemy.actor.sched;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;

import ptolemy.actor.*;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// Firing
/**
This class is a schedule element that contains a reference to an actor. 
This class is used together with Schedule to construct a static schedule.
This class contains a reference to an actor, and is used to represent an
actor term
of a schedule loop. The setActor() method is used to create the reference
to an actor. The getActor() method will return a reference to this actor.

@author Brian K. Vogel
@version $Id$
@see ptolemy.actor.sched.Schedule
@see ptolemy.actor.sched.ScheduleElement
*/

public class Firing extends ScheduleElement {
    /** Construct a firing with a default iteration count equal to one.
     *  
     */
    public Firing() {
	super();
	_firing = new LinkedList();
	_firing.add(this);
	_actorInvocations = new LinkedList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the actor invocation sequence of the schedule in the 
     *  form of a sequence of actors. For a valid schedule, all of the
     *  lowest-level nodes should be an instance of Firing. If the
     *  schedule is not valid, then the returned iterator will contain 
     *  null elements.
     *  <p>
     *  Note that the behavior of an iterator is unspecified if the
     *  underlying schedule structure is modified while the iterator
     *  is active.
     *  
     * @return An iterator over a sequence of actors.
     */
    public Iterator actorIterator() {
	// Update the list of actors, if necessary.
	_updateActorInvocations();
	return _actorInvocations.iterator();
    }

    /** Return the actor invocation sequence in the form
     *  of a sequence of firings. 
     *  Since this ScheduleElement is a Firing, the
     *  iterator returned will contain exactly one Firing (this Firing).
     *  <p>
     *  Note that the behavior of an iterator is unspecified if the
     *  underlying schedule structure is modified while the iterator
     *  is active.
     *  
     *  @return An iterator over a sequence of firings.
     */
    public Iterator firingIterator() {
	return _firing.iterator();	
    }

    /** Get the actor associated with this Firing. The setActor()
     *  method is used to set the actor that this method returns.
     *  If setActor() was never called, then throw an exception.
     *
     * @return The actor associated with this Firing.
     */
    public Executable getActor() {
	return _actor;
    }

    /** Set the actor associated with this firing. This actor will
     *  then be returned when the getActor() method is invoked. If this
     *  firing already contains a reference to an actor, then the
     *  reference will overwritten.
     *
     * @param actor The actor to associate with this firing.
     */
    public void setActor(Executable actor) {
	_actor  = actor;
	_actorInvocationsValid = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods               ////

    /** Update the list of actor invocations, if necessary. The list 
     *  needs to be updated when setActor() has been invoked since 
     *  the last invocation of actorIterator().
     *
     */
    private void _updateActorInvocations() {
	if (_actorInvocationsValid == false) {
	    // Need to update the list.
	    // Remove all actors from the list
	    while (_actorInvocations.isEmpty() == false) {
		_actorInvocations.remove(0);
	    }
	    // Add the new actors to the list.
	    for (int i = 0; i < getIterationCount(); i++) {
		_actorInvocations.add(getActor());
	    }
	} 
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables               ////

    // The actor associated with this firing.
    private Executable _actor;

    // The list containing this firing as the only element.
    private List _firing;

    // The list containing the actor invocation sequence corresponding
    // to this firing.
    private List _actorInvocations;

    private boolean _actorInvocationsValid = false;
}
