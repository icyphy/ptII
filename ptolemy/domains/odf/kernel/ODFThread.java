/* An ODFThread controls an actors according to ODF semantics.

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

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.domains.odf.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// ODFThread
/**
An ODFThread controls an actors according to ODF semantics. The primary 
purpose of an ODFThread is to control the iteration methods of an 
executing actor and to maintain the actor's local notion of time 
according to ODF semantics. An ODFThread has two unique functionalities 
for accomplishing this goal. First an ODFThread instantiates a TimeKeeper 
object. A TimeKeeper object manages a given actor's local notion of time 
according to ODF semantics. The second task of an ODFThread is to notify 
directly connected actors when the actor controlled by the thread is 
ending execution. 

@author John S. Davis II
@version $Id$
@see ptolemy.domains.odf.kernel.TimeKeeper
*/
public class ODFThread extends ProcessThread {

    /** Construct a thread to be used to execute the iteration 
     *  methods of an ODFActor. This increases the count of 
     *  active actors in the director.
     * @param actor The ODFActor that needs to be executed.
     * @param director The director of this actor.
     */
    public ODFThread(Actor actor, ProcessDirector director) 
            throws IllegalActionException {
        super(actor, director);
	_timeKeeper = new TimeKeeper(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the time keeper that keeps time for the actor that this
     *  thread controls.
     * @return The TimeKeeper of the actor that this thread controls.
     */
    public TimeKeeper getTimeKeeper() {
	return _timeKeeper;
    }

    /** Notify directly connected actors that the actor controlled by 
     *  this thread is ending execution. "Directly connected actors"
     *  are those that are connected to the actor controlled by this
     *  thread via output ports of this thread's actor. Send events 
     *  with time stamps of -1.0 to these "downstream" actors.
     */
    public synchronized void noticeOfTermination() { 
        Actor actor = (Actor)getActor();
	/*
        ODFActor actor = (ODFActor)getActor();
	System.out.println(((NamedObj)actor).getName()+
                ": calling noticeOfTermination()");
	*/
	Enumeration outputPorts = actor.outputPorts();
	if( outputPorts != null ) {
	    while( outputPorts.hasMoreElements() ) {
	        IOPort port = (IOPort)outputPorts.nextElement();
                Receiver rcvrs[][] = (Receiver[][])port.getRemoteReceivers();
                if( rcvrs == null ) {
	            break;
	        }
                for (int i = 0; i < rcvrs.length; i++) {
                    for (int j = 0; j < rcvrs[i].length; j++) {
			try {
                            ((ODFReceiver) rcvrs[i][j]).put(null, -1.0);
			} catch( TerminateProcessException e ) {
			    // Do nothing since we are ending
			}
		    }
                }
            }
	}
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
    ////                   package friendly methods		   ////

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    private TimeKeeper _timeKeeper = null;

}








