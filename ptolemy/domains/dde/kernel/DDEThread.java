/* A DDEThread controls an actor according to DDE semantics.

 Copyright (c) 1997-1999 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTelelIAL DAMAGES
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

@ProposedRating Yellow (davisj@eecs.berkeley.edu)
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)

*/

package ptolemy.domains.dde.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;

import java.util.Hashtable;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// DDEThread
/**
A DDEThread controls an actor according to DDE semantics. The
primary purpose of a DDEThread is to control the iteration
methods of an executing actor and to maintain the actor's local
notion of time according to DDE semantics. A DDEThread has two
unique functionalities for accomplishing this goal. First a
DDEThread instantiates a TimeKeeper object. A TimeKeeper manages
a given actor's local notion of time according to DDE semantics.
The second task of a DDEThread is to notify directly connected
actors when the actor controlled by the thread is ending execution.

@author John S. Davis II
@version $Id$
@see ptolemy.domains.dde.kernel.TimeKeeper
*/
public class DDEThread extends ProcessThread {

    /** Construct a thread to be used to execute the iteration
     *  methods of a DDEActor. This increases the count of
     *  active actors in the director.
     * @param actor The DDEActor that will be executed.
     * @param director The director of this actor.
     */
    public DDEThread(Actor actor, ProcessDirector director)
            throws IllegalActionException {
        super(actor, director);
	_timeKeeper = new TimeKeeper(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the time keeper that keeps time for the actor that
     *  this thread controls.
     * @return The TimeKeeper of the actor that this thread
     *  controls.
     */
    public TimeKeeper getTimeKeeper() {
	return _timeKeeper;
    }

    /** Notify output-connected actors that the actor controlled
     *  by this thread is ending execution. <I>Output-connected
     *  actors</I> are those that are connected to the actor
     *  controlled by this thread via output ports of this thread's
     *  actor. Send events with time stamps of
     *  PrioritizedTimedQueue.INACTIVE to these "downstream" actors.
     * @see ptolemy.domains.dde.kernel.PrioritizedTimedQueue
     */
    public synchronized void noticeOfTermination() {
        Actor actor = (Actor)getActor();
	Iterator outputPorts = actor.outputPortList().iterator();
	double endTime = PrioritizedTimedQueue.INACTIVE;
	if( outputPorts != null ) {
	    while( outputPorts.hasNext() ) {
	        IOPort port = (IOPort)outputPorts.next();
                Receiver rcvrs[][] =
                    (Receiver[][])port.getRemoteReceivers();
                if( rcvrs == null ) {
	            break;
	        }
                for (int i = 0; i < rcvrs.length; i++) {
                    for (int j = 0; j < rcvrs[i].length; j++) {
			try {
			    if( ((DDEReceiver)rcvrs[i][j]).getRcvrTime()
				    != endTime ) {
				((DDEReceiver) rcvrs[i][j]).put(null,
                                	endTime);
			    }
			} catch( TerminateProcessException e ) {
			    // Do nothing since we are ending
			}
		    }
                }
            }
	}
    }

    /** Start this thread and initialize the time keeper to a future
     *  time if specified in the director's initial time table. Use
     *  this method to facilitate any calls to DDEDirector.fireAt()
     *  that occur prior to the creation of this thread. If fireAt()
     *  was called for time 'T' with respect to the actor that this
     *  thread controls, then set the current time of this threads
     *  TimeKeeper to time 'T.'
     *  <P>
     *  NOTE: This method assumes an implementation of fireAt() that
     *  would be more appropriately named <I>continueAt()</I>.
     */
    public void start() {
	Actor actor = getActor();
	DDEDirector director = (DDEDirector)actor.getDirector();
	Hashtable table = director.getInitialTimeTable();
	if( table != null ) {
	    Double dTime = (Double)table.get(actor);
	    if( dTime != null ) {
		double time = dTime.doubleValue();
		_timeKeeper.setCurrentTime( time );
	    }
	}
	super.start();
    }

    /** End the execution of the actor under the control of this
     *  thread. Notify all actors connected to this actor that
     *  this actor is preparing to cease execution.
     *  @exception IllegalActionException if an error occurs while
     *  ending execution of the actor under the control of this
     *  thread.
     */
    public void wrapup() throws IllegalActionException {
	noticeOfTermination();
	super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private TimeKeeper _timeKeeper = null;

}
