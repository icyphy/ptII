/* A clock source that can notify an ExecEventListener of ExecEvents.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.dde.demo.LocalZeno;

import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.gui.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// ListenClock
/**
A ListenClock is a clock source that can notify an ExecEventListener
of ExecEvents. In particular, the listener will be notified each time the
prefire(), postfire() and wrapup() methods of this actor are
invoked. Such notification is enabled by adding an ExecEventListener
to this actor's listener list via the addListeners() method. Listeners
can be removed via the removeListeners() method. ExecEventListeners
are currently implemented to serve as conduits between Ptolemy II and
the Diva graphical user interface.

@author John S. Davis II
@version $Id$
@see ptolemy.actor.gui.ExecEvent
@see ptolemy.actor.gui.ExecEventListener
*/

public class ListenClock extends Clock {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be
     *   contained by the proposed container.
     *  @exception NameDuplicationException If the container
     *   already has an actor with this name.
     */
    public ListenClock(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an ExecEventListener to this actor's list of
     *  listeners. If the specified listener already exists
     *  in this actor's list, then allow both instances to
     *  separately remain on the list.
     * @param listener The specified ExecEventListener.
     */
    public void addListeners(ExecEventListener listener) {
        if( _listenerList == null ) {
            _listenerList = new LinkedList();
        }
        _listenerList.addLast(listener);
    }

    /** Notify all ExecEventListeners on this actor's
     *  listener list that the specified event was
     *  generated.
     * @param event The specified ExecEvent.
     */
    public void generateEvents(ExecEvent event) {
        if( _listenerList == null ) {
            return;
        }
        Iterator listeners = _listenerList.iterator();
        while( listeners.hasNext() ) {
            ExecEventListener listener =
                (ExecEventListener)listeners.next();
            listener.stateChanged(event);
        }
    }

    /** Generate an ExecEvent with a state value of 1, cause the
     *  calling thread to sleep for 100 milliseconds and then call
     *  the superclass prefire() method.
     * @exception IllegalActionException If there is an
     *  interruption while the calling thread sleeps.
     */
    public boolean prefire() throws IllegalActionException {
	generateEvents( new ExecEvent( this, 1 ) );
	try {
	    Thread.sleep(100);
	} catch(InterruptedException e) {
            throw new InternalErrorException( "Error with "
            	    + "sleeping thread in prefire");
	}
	return super.prefire();
    }

    /** Generate an ExecEvent with a state value of 2. Return the
     *  value of the postfire method of this actor's superclass.
     *  Return true if this actor is enabled to call fire(); return
     *  false otherwise.
     * @return True if this actor is enabled to call fire(); return
     *  false otherwise.
     * @exception IllegalActionException If there is an exception
     *  with the thread activity of this method.
     */
    public boolean postfire() throws IllegalActionException {
	generateEvents( new ExecEvent( this, 2 ) );
	try {
	    Thread.sleep(100);
	} catch(InterruptedException e) {
            throw new InternalErrorException( "Error with "
            	    + "sleeping thread in postfire");
	}
	return super.postfire();
    }

    /** Generate an ExecEvent with a state value of 3, cause the
     *  calling thread to sleep for 100 milliseconds and then call
     *  the superclass wrapup() method.
     * @exception IllegalActionException If there is an exception
     *  in the execution of the wrapup method of this actor's
     *  superclass.
     */
    public void wrapup() throws IllegalActionException {
	generateEvents( new ExecEvent( this, 3 ) );
	super.wrapup();
    }

    /** Remove one instance of the specified ExecEventListener
     *  from this actor's list of listeners.
     * @param listener The specified ExecEventListener.
     */
    public void removeListeners(ExecEventListener listener) {
        if( _listenerList == null ) {
            return;
        }
        _listenerList.remove(listener);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private LinkedList _listenerList;

}
