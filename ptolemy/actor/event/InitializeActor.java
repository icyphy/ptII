/* A request to initialize an actor.

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.actor.event;

import ptolemy.kernel.event.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.Actor;
import ptolemy.actor.Director;

//////////////////////////////////////////////////////////////////////////
//// InitializeActor
/**
A request to initialize an actor.  This class is used for certain
kinds of mutations where the actor can be created directly (not via
a queued mutation), but the initialization of the actor must be
deferred so that it executes at an appropriate time.  Note that
many domains will tolerate the creation of an actor at any time,
and as long as the actor is not connected to any pre-existing actor,
the director will ignore it.

@author  Edward A. Lee
@version $Id$
@see ptolemy.actor.Actor
*/
public class InitializeActor extends ChangeRequest {

    /** Construct a request with the specified originator and
     *  actor to be initialized.  The actor must also implement the
     *  Nameable interface or a ClassCastException will occur.
     *  @param originator The source of the change request.
     *  @param actor The actor.
     */
    public InitializeActor(Nameable originator, Actor actor) {
        super(originator, "Initialize " + ((Nameable)actor).getFullName());
        _actor = actor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the change by calling the initialize() method of the
     *  actor.  This method also notifies the director that scheduling
     *  and type resolution may be invalid.
     *  @exception ChangeFailedException If the initialize() method throws
     *   an exception.
     */
    public void execute() throws ChangeFailedException {
        try {
            _actor.initialize();
        } catch (KernelException ex) {
            throw new ChangeFailedException(this, ex);
        }
        Director director = _actor.getDirector();
        director.invalidateSchedule();
        director.invalidateResolvedTypes();
    }

    /** Get the actor.
     *  @return The actor to be initialized.
     */
    public Actor getActor() {
        return _actor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The actor to initialize.
    private Actor _actor;
}
