/* A receiver which stores time stamped tokens using blocking reads/writes.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

@ProposedRating Red (davisj@eecs.berkeley.edu)

*/

package ptolemy.domains.od.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;
import ptolemy.actor.util.*;

import java.util.NoSuchElementException;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// ODReceiver
/**
A receiver which stores time stamped tokens using blocking reads/writes.
A "time stamped token" is a token that has a time stamp associated with it.
Time stamps are explained further in 
         ptolemy/domains/od/kernel/TimedQueueReceiver.java.
An ODReceiver stores time stamped tokens by enforcing a blocking read and
blocking write format. Time stamped tokens are appended to the queue with 
either of the put() methods, both of which block on a write if the queue 
is full. Time stamped tokens are removed from the queue with the get() 
method which blocks on a read if the queue is empty. If a process blocks on 
a read or a write, the director is informed. Blocks are removed (and the 
director is informed) if the conditions which led to them no longer apply. 

Since ODReceiver is derived from TimedQueueReceiver, it inherits both the
lastTime and rcvrTime flags. ODReceiver sets these flags in a manner 
similar to TimedQueueReceiver. The key difference between ODReceiver and
TimedQueueReceiver is that get() and put() block as described above.

An additional flag in an ODReceiver is the "simultaneousIgnore" flag, the
use of which is described in the following. In general, an actor containing 
an ODReceiver will get a token from the receiver if the receiver has a 
uniquely minimum rcvrTime flag value with respect to the other receivers of 
the containing actor. If a receiver has a minimum rcvrTime w.r.t. all 
receivers of the containing actor, then get() will return a non-null token 
only if simultaneousIgnore is true.

NOTE:
This class (and indeed the OD MoC) does not place emphasis on blocking 
writes and generally is designed for systems in which memory will be 
abundant. Hence, queue capacities are assumed to be infinite. In particular, 
it is assumed that if we want to put a token with a time stamp of -1.0 into 
a queue, then space will be available. This assumption (which is valid given 
infinite queue capacities) allows us to avoid certain difficult deadlock 
situations.


*** 
Synchronization Notes:
*** 
This domain observes a hierarchy of synchronization locks. When multiple 
synchronization locks are required, they must be obtained in an order that 
is consistent with this hierarchy. Adherence to this hierarchical ordering
ensures that deadlock can not occur due to circular lock dependencies.

The following synchronization hierarchy is utilized: 

	1. read/write access on the workspace 
        2. synchronization on the receiver 
        3. synchronization on the director 
        4. synchronization on the actor
        5. (other) synchronization on the workspace
        
We say that lock #1 is at the highest level in the hierarchy and lock #5
is at the lowest level.

As an example, a method that synchronizes on a receiver can not contain 
read/write access on the workspace; such accesses must occur outside of 
the receiver synchronization. Similarly, a method which synchronizes on a 
director must not synchronize on the receiver or contain read/write 
accesses on the workspace; it can contain synchronizations on actors or
the workspace. 

The justification of the chosen ordering of this hierarchy is based on
the access a method has to the fields of its object versus the fields of
other objects. The more (less) a method focuses on the internal state of 
its object and non-synchronized methods of external objects, the lower 
(higher) the method is placed in the synchronization hierarchy. In the 
case of read/write access on the workspace, the corresponding methods, 
i.e, getReadAccess() and getWriteAccess(), access the current thread 
running in the JVM. This external access deems these methods as being at 
the top of the hierarchy. All other synchronizations on the workspace only 
focus on the internal state of the workspace and hence are at the bottom 
of the synchronization hierarchy.


@author John S. Davis II
@version @(#)ODReceiver.java	1.16	11/18/98
*/
public class ODReceiver extends TimedQueueReceiver 
        implements ProcessReceiver {

    /** Construct an empty receiver with no container.
     */
    public ODReceiver() {
        super();
    }

    /** Construct an empty receiver with the specified container.
     * @param container The IOPort which contains this receiver.
     */
    public ODReceiver(IOPort container) {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do a blocking read on the queue. If at any point during this
     *  method this receiver is scheduled for termination, then throw 
     *  a TerminateProcessException which will cease activity for the 
     *  actor that contains this receiver. If no token is available, 
     *  then inform the director that this receiver is blocking on a 
     *  read and wait until a token becomes available. When a token 
     *  becomes available, determine if this queue has the unique oldest 
     *  rcvrTime with respect to all of the receivers contained by the 
     *  actor that contains this receiver. If so, return the token. If 
     *  the rcvrTime is not a unique minimum but simultaneousIgnore is 
     *  true, then return the token; otherwise return null. 
     * @return Token The oldest token on this queue.
     */
    public Token get() {
        // System.out.println("\nCall to ODReceiver.get()");
        // System.out.println("Previous rcvrTime = " + getRcvrTime() );
       
       
        // Cache values so that the synchronization hierarchy 
        // will not be violated
        Workspace workspace = getContainer().workspace();
        ODActor actor = (ODActor)getContainer().getContainer();
        ODDirector director = (ODDirector)actor.getDirector(); 
        ODIOPort myPort = (ODIOPort)getContainer();
        String name = actor.getName(); 
        Token token = null; 
        
        synchronized(this) {
            if( _terminate ) {
	        /*
	        System.out.println("Get called during terminate");
                director.addReadBlock(); 
		notifyAll(); 
		workspace.wait( this );
	        */
                throw new TerminateProcessException( getContainer(), "This "
                        + "receiver has been terminated during get().");
            }
            if( super.hasToken() && !_terminate ) {
                /*
                if( name.equals("printer") ) {
                    System.out.println("\t"+name+" entered hasToken() block");
                    System.out.println("\trcvrTime = "+getRcvrTime());
                    System.out.println("\tactor time = " 
                            +actor.getCurrentTime());
                }
                */
	        // FIXME: What if current time = -1.0??
	        if( getRcvrTime() <= actor.getNextTime() ) {
		// if( getRcvrTime() <= actor.getCurrentTime() ) {
                    if( actor.hasMinRcvrTime() ) {
                        token = super.get();
                        notifyAll(); 
                        return token;
                    } else if( isSimultaneousIgnore() ) {
                        setSimultaneousIgnore(false);
                        token = super.get();
                        notifyAll(); 
                        return token;
                    }
                    /*
                    if( name.equals("printer") ) {
                        System.out.println(name+" reached first null");
                    }
                    */
                    return null;
		}
		// Return null in case rcvrTime is no longer the lowest.
		return null;
            } else if( !_terminate ) {
                director.addReadBlock();
                while( !_terminate && !super.hasToken() ) {
                    /*
                    System.out.println("Actor(" 
                            + ((ComponentEntity)actor).getName() + 
                            ") about to block in Port("
                            + myPort.getName() 
                            + ") ODReceiver.get()");
                    */
                    
                    notifyAll();
                    workspace.wait( this );
                    /*
                    System.out.println("Actor: " 
                            + ((ComponentEntity)actor).getName() + 
                            " awakened after blocking in ODReceiver.get()");
                    */
                }
            } 
            if( _terminate ) {
                director.removeReadBlock(); 
                throw new TerminateProcessException( getContainer(), "This "
                        + "receiver has been terminated during get().");
            } else {
	        if( getRcvrTime() <= actor.getNextTime() ) {
		// if( getRcvrTime() <= actor.getCurrentTime() ) {
                    if( actor.hasMinRcvrTime() ) {
                        token = super.get();
                        notifyAll(); 
                    } else if( isSimultaneousIgnore() ) {
                        setSimultaneousIgnore(false);
                        token = super.get();
                        notifyAll(); 
                    }
		}
                director.removeReadBlock(); 
            }
            // FIXME: Will this point ever be reached?
        }
        
        return token;
    }

    /** FIXME: Is this necessary?
     *  Throw an IllegalActionException because polling of queues 
     *  is not allowed.
    public boolean hasRoom() {
        return true;
        // throw new IllegalActionException( getContainer(), "This domain"
          //      + " does not allow polling of input queues.");
    }
     */

    /** FIXME: Is this necessary?
     *  Throw an IllegalActionException because polling of queues 
     *  is not allowed.
    public boolean hasToken() {
        // FIXME: This needs to be changed back to "return true;" after testing
        return super.hasToken();
        // throw new IllegalActionException( getContainer(), "This domain"
              //  + " does not allow polling of output queues.");
    }
     */

    /** Do a blocking write on the queue. If at any point during this 
     *  method this receiver is scheduled for termination, then throw 
     *  a TerminateProcessException which will cease activity for the 
     *  actor that contains this receiver. Set the time stamp to the
     *  lastTime of this receiver. If the time stamp of the token is 
     *  greater than the completionTime of this receiver, then set the 
     *  time stamp to -1.0 and the token to null. If the queue is full, 
     *  then inform the director that this receiver is blocking on a 
     *  write and wait until room becomes available. When room becomes 
     *  available, put the token and time stamp in the queue and inform 
     *  the director that the block no longer exists. 
     * @param token The token to put on the queue.
     */
    public void put(Token token) {
        /* 
        double currentTime;
        currentTime = 
	        ((ODActor)getContainer().getContainer()).getCurrentTime();
        put( token, currentTime );
        */ 

        put( token, getLastTime() );
    }

    /** Do a blocking write on the queue. If at any point during this 
     *  method this receiver is scheduled for termination, then throw 
     *  a TerminateProcessException which will cease activity for the 
     *  actor that contains this receiver. If the specified time stamp 
     *  of the token is greater than the completionTime of this receiver,
     *  then set the time stamp to -1.0 and the token to null. If 
     *  the queue is full, then inform the director that this receiver 
     *  is blocking on a write and wait until room becomes available. 
     *  When room becomes available, put the token and time stamp in the 
     *  queue and inform the director that the block no longer exists. 
     * @param token The token to put on the queue.
     * @param time The time stamp associated with the token.
     */
    public void put(Token token, double time) {
        // System.out.println("\nCall to ODReceiver.put()");
        // System.out.println("Previous queue size = " + getSize() );
        
        // Cache values so that the synchronization hierarchy 
        // will not be violated
        Workspace workspace = getContainer().workspace();
        ODActor actor = (ODActor)getContainer().getContainer();
        ODDirector director = (ODDirector)actor.getDirector();

	/*
	String myName = ((ComponentEntity)actor).getName();
	if( token != null && myName.equals("printer") ) {
	    if( (time==19.5) || (time==20.0) || (time==20.5) ) {
	        StringToken sToken = (StringToken)token;
	        String sValue = sToken.stringValue();
	        System.out.println(sValue+" has been placed in "
	        +((ComponentEntity)actor).getName()+"'s ODReceiver. "
                +"The time is: "+time);
	    }
	}
	*/
        
        synchronized(this) {
	    if( _terminate ) {
	        throw new TerminateProcessException( getContainer(), "This " 
		        + "receiver has been terminated during get().");
	    }
	    if( time > getCompletionTime() && getCompletionTime() != -5.0 ) {
	        time = -1.0;
		token = null;
	    }
            if( !super.hasRoom() ) {
	        // No Room - Must Block.
                director.addWriteBlock();
                while( !_terminate && !super.hasRoom() ) {
                    /*
                    System.out.println("Actor: " 
                            + ((ComponentEntity)actor).getName() + 
                            " about to block in ODReceiver.get()");
                    System.out.println("Actor(" 
                            + ((ComponentEntity)actor).getName() + 
                            ") about to block in Port("
                            + getContainer().getName() 
                            + ") ODReceiver.put()");
                    */
                    notifyAll();
                    workspace.wait( this );
                    /*
                    System.out.println("Actor: " 
                            + ((ComponentEntity)actor).getName() + 
                            " awakened after blocking in ODReceiver.get()");
                    */
                }
	    } else if( !_terminate ) {
	        // Never Blocked.
                super.put(token, time);
                notifyAll(); 
                /*
                System.out.println("Actor(" 
                        + ((ComponentEntity)actor).getName() + 
                        ") notifiedAll after a ODReceiver.super.put()"
                        + "\n\treceiver hasToken() = "+super.hasToken() );
                */
		return;
		/*
            } else {
                super.put(token, time);
		*/
	    }
            
            if( _terminate ) {
	        // Woke up from block. Terminate.
                director.removeWriteBlock(); 
                // super.put(token, time);
                new TerminateProcessException( getContainer(), "" );
            } else {
	        // Woke up from block. Do not terminate.
                director.removeWriteBlock(); 
                super.put(token, time);
                notifyAll();
		return;
            }
        }
    }

    /** Schedule this receiver to terminate.
     */
    public void setFinish() {
        _terminate = true;
        // FIXME: Hmmm...
	// notifyAll();
    }
    
    /** Set the pause flag of this receiver.
     * @param flag The boolean pause flag of this receiver.
     */
    public void setPause(boolean flag) {
        ;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                  package friendly methods 		   ////

    /** Return true if the simultaneousIgnore flag is set to true. See
     *  class comments or get() method comments for an explanation of 
     *  the use of this flag. 
     * @return boolean The value of the simultaneousIgnore flag.
     * @see get()
     */
    boolean isSimultaneousIgnore() {
        return _simulIgnoreFlag;
    }
    
    /** Set the value of the simultaneousIgnore flag to the specified
     *  boolean value. See class comments or get() method comments for 
     *  an explanation of the use of this flag. 
     * @param flag The value of the simultaneousIgnore flag.
     * @see get()
     */
    void setSimultaneousIgnore(boolean flag) {
        _simulIgnoreFlag = flag;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods 		   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _simulIgnoreFlag = false;
    private boolean _terminate = false;

}




