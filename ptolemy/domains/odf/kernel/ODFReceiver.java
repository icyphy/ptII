/* A receiver which stores time stamped tokens using blocking reads/writes.

 Copyright (c) 1997-1999 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.domains.odf.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;
import ptolemy.actor.util.*;

import java.util.NoSuchElementException;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// ODFReceiver
/**
A receiver which stores time stamped tokens using blocking reads/writes.
A "time stamped token" is a token that has a time stamp associated with it.
An ODFReceiver stores time stamped tokens by enforcing a blocking read and
blocking write format. Time stamped tokens are appended to the queue with 
either of the put() methods, both of which block on a write if the queue 
is full. Time stamped tokens are removed from the queue with the get() 
method which blocks on a read if the queue is empty. If a process blocks on 
a read or a write, the director is informed. Blocks are removed (and the 
director is informed) if the conditions which led to them no longer apply. 
<P>
Since ODFReceiver is derived from TimedQueueReceiver, it inherits both the
lastTime and rcvrTime flags. ODFReceiver sets these flags in a manner 
similar to TimedQueueReceiver. The key difference between ODFReceiver and
TimedQueueReceiver is that get() and put() block as described above.
<P>
An additional flag in an ODFReceiver is the "simultaneousIgnore" flag, the
use of which is described in the following. In general, an actor containing 
an ODFReceiver will get a token from the receiver if the receiver has a 
uniquely minimum rcvrTime flag value with respect to the other receivers of 
the containing actor. If a receiver has a minimum rcvrTime w.r.t. all 
receivers of the containing actor, then get() will return a non-null token 
only if simultaneousIgnore is true.
<P>
NOTE:<BR>
This class (and indeed the ODF MoC) does not place emphasis on blocking 
writes and generally is designed for systems in which memory will be 
abundant. Hence, queue capacities are assumed to be infinite. In particular, 
it is assumed that if we want to put a token with a time stamp of -1.0 into 
a queue, then space will be available. This assumption (which is valid given 
infinite queue capacities) allows us to avoid certain difficult deadlock 
situations.


@author John S. Davis II
@version @(#)ODFReceiver.java	1.16	11/18/98
@see ptolemy.domains.odf.kernel.TimedQueueReceiver
*/
public class ODFReceiver extends TimedQueueReceiver 
        implements ProcessReceiver {

    /** Construct an empty receiver with no container.
     */
    public ODFReceiver() {
        super();
    }

    /** Construct an empty receiver with the specified container.
     * @param container The IOPort that contains this receiver.
     */
    public ODFReceiver(IOPort container) {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do a blocking read on the queue. If no token is available, 
     *  then inform the director that this receiver is blocking on 
     *  a read and wait until a token becomes available. When a 
     *  token becomes available, determine if this queue has the 
     *  unique oldest rcvrTime with respect to all of the receivers 
     *  contained by the actor that contains this receiver. If so, 
     *  return the token. If the rcvrTime is a non-unique minimum but 
     *  simultaneousIgnore is true, then return the token; otherwise 
     *  return null. If at any point during this method this receiver 
     *  is scheduled for termination, then throw a 
     *  TerminateProcessException to cease execution of the actor 
     *  that contains this receiver. 
     * @return Token The oldest token on this queue.
     * @exception NoTokenException If this method is called while 
     *  hasToken() returns false.
     */
    public Token get() {
        Workspace workspace = getContainer().workspace();
        ODFDirector director = (ODFDirector)
	        ((Actor)getContainer().getContainer()).getDirector();
	ODFThread thread = (ODFThread)Thread.currentThread();

	synchronized(this) {
	    if( _hasToken(workspace, director, thread) ) {
		Token token = super.get();
		notifyAll();
		return token;
	    } else {
		throw new NoTokenException(getContainer(), "No tokens "
			+ "available in the ODF receiver");
	    }
	}
    }

    /** Return true if the get() method of this receiver will return a
     *  non-null token. If this receiver has a rcvrTime that is not less 
     *  than or equal to the rcvrTime of all receivers contained by the 
     *  actor that contains this receiver then return false. If this 
     *  receiver has a rcvrTime that is equal to the minimum rcvrTime of 
     *  all receivers contained by the actor that contains this receiver
     *  and at least one receiver has a rcvrTime equal to that of this
     *  receiver, then return false if this receiver has a lower priority
     *  when compared to all receivers sharing its rcvrTime. Otherwise, 
     *  block until this receiver contains a token. If at any point during 
     *  this method this receiver is scheduled for termination, then throw 
     *  a TerminateProcessException to cease execution of the actor that 
     *  contains this receiver. 
     */
    public boolean hasToken() {
	Workspace workspace = getContainer().workspace();
	ODFThread thread = (ODFThread)Thread.currentThread();
        ODFDirector director = (ODFDirector)
	        ((Actor)getContainer().getContainer()).getDirector();

	return _hasToken( workspace, director, thread );
    }

    /**
     */
    private synchronized boolean _hasToken(Workspace workspace, 
	    ODFDirector director, ODFThread thread ) {
	// FIXME: synchronized not needed.
	synchronized(this) {
	    if( getRcvrTime() > thread.getNextTime() && !_terminate ) {
		if( getContainer().getContainer().getName().equals("printer") ) {
		    /*
		    System.out.println("Printer: rcvrTime = "
			    + getRcvrTime() + "  . Thread time = "
			    + thread.getNextTime());
		    */
		}
		return false;
	    } else if( !thread.hasMinRcvrTime() && !_terminate ) {
		if( getContainer().getContainer().getName().equals("printer") ) {
		    /*
		    System.out.println("Printer: No minimum receiver time.");
		    System.out.println("Printer: rcvrTime = "
			    + getRcvrTime() + "  . Thread time = "
			    + thread.getNextTime());
		    // thread.printRcvrList();
		    */
		}
		RcvrTimeTriple triple; 
		triple = thread.getHighestPriorityTriple();
		if( this != triple.getReceiver() ) {
		    if( getContainer().getContainer().getName().equals("printer") ) {
			/*
		        System.out.println("Printer: Not highest priority "
				+ "triple.");
		        System.out.println("Printer: rcvrTime = "
			        + getRcvrTime() + "  . Thread time = "
			        + thread.getNextTime());
			*/
		    }
		    return false;
		}
	    }
	    if( super.hasToken() && !_terminate ) {
		return true;
	    }
	    director.addReadBlock();
	    while( !super.hasToken() && !_terminate ) {
		if( getContainer().getContainer().getName().equals("printer") ) {
		    // System.out.println("Printer: Preparing to block.");
		}
		notifyAll(); 
		workspace.wait( this );
	    }
	    if( _terminate ) {
		director.removeReadBlock();
		throw new TerminateProcessException( getContainer(),
			"This receiver has been terminated during "
			+ "hasToken()");
	    } else {
		director.removeReadBlock();
		return _hasToken(workspace, director, thread);
	    }
	}
    }

    /** Reset local flags.
     */
    public void reset() {
        _simulIgnoreFlag = false; 
	_terminate = false;
    }

    /** FIXME
     */
    public void put(double time) {
	Token token = null;
        put( token, time );
    }

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
        System.out.println( ((NamedObj)getContainer().getContainer()).getName()
	        + ": call to ODFReceiver.put(Token)");
	*/
	Thread thread = Thread.currentThread(); 
        /* 
        double currentTime;
        currentTime = 
	        ((ODFActor)getContainer().getContainer()).getCurrentTime();
        put( token, currentTime );
        */ 
	if( thread instanceof ODFThread ) {
	    double time = ((ODFThread)thread).getOutputTime();
            put( token, time );
	} else {
            put( token, getLastTime() );
	}
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
	/*
        System.out.println( ((NamedObj)getContainer().getContainer()).getName()
	        + ": call to ODFReceiver.put(Token,"
                +time+")");
	*/
        // System.out.println("Previous queue size = " + getSize() );
        
        // Cache values so that the synchronization hierarchy 
        // will not be violated
        Workspace workspace = getContainer().workspace();
        ODFActor actor = (ODFActor)getContainer().getContainer();
        // ODFDirector director = (ODFDirector)actor.getDirector();
        ODFDirector director = 
                (ODFDirector)((Actor)getContainer().getContainer()).getDirector();
	Thread thread = Thread.currentThread(); 

	String myName = ((ComponentEntity)actor).getName();
	/*
	if( token != null && myName.equals("printer") ) {
	        StringToken sToken = (StringToken)token;
	        String sValue = sToken.stringValue();
	        System.out.println(sValue+" has been placed in "
	        +((ComponentEntity)actor).getName()+"'s ODFReceiver. "
                +"The time is: "+time);
	}
	*/
        
        synchronized(this) {
	    if( _terminate ) {
	        throw new TerminateProcessException( getContainer(), "This " 
		        + "receiver has been terminated during get().");
	    } if( time > getCompletionTime() && getCompletionTime() != -5.0 ) {
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
                            " about to block in ODFReceiver.get()");
                    System.out.println("Actor(" 
                            + ((ComponentEntity)actor).getName() + 
                            ") about to block in Port("
                            + getContainer().getName() 
                            + ") ODFReceiver.put()");
                    */
                    notifyAll();
                    workspace.wait( this );
                    /*
                    System.out.println("Actor: " 
                            + ((ComponentEntity)actor).getName() + 
                            " awakened after blocking in ODFReceiver.get()");
                    */
                }
	    } else if( !_terminate ) {
	        // Never Blocked.
                super.put(token, time);
                notifyAll(); 
		if( thread instanceof ODFThread ) {
		    ((ODFThread)thread).sendOutNullTokens();
		}
                /*
                System.out.println("Actor(" 
                        + ((ComponentEntity)actor).getName() + 
                        ") notifiedAll after a ODFReceiver.super.put()"
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
		if( thread instanceof ODFThread ) {
		    ((ODFThread)thread).sendOutNullTokens();
		}
		return;
            }
        }
    }

    /** Schedule this receiver to terminate.
     * FIXME: Synchronize this.
     */
    public synchronized void requestFinish() {
	System.out.println(getContainer().getContainer().getName() 
		+ " called requestFinish()");
        _terminate = true;
	notifyAll();
    }
    
    /** Set the pause flag of this receiver.
     * @param flag The boolean pause flag of this receiver.
     */
    public void requestPause(boolean flag) {
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


	/* FROM _hasToken
	boolean retValue = false;
	Workspace workspace = getContainer().workspace();
	ODFThread thread = getThread();
        ODFDirector director = (ODFDirector)
	        ((Actor)getContainer().getContainer()).getDirector();
	synchronized(this) {
	    if(_terminate) {
		throw new TerminateProcessException( getContainer(),
			"This receiver has been terminated during "
			+ "hasToken()");
	    }

	    // Token available; check for minimum time
	    if( super.hasToken() && !_terminate ) {
		// FIXME: What if current time = -1.0?
		if( getRcvrTime() <= thread.getNextTime() ) {
		    if( thread.hasMinRcvrTime() ) {
			return true;
		    } else {
			RcvrTimeTriple triple;
			triple = thread.getHighestPriorityTriple();
			if( this == triple.getReceiver() ) {
			    return true;
			} 
			return false;
		    }
		}
		return false;
	    } else if( !_terminate ) {
		director.addReadBlock();
		while( !_terminate & !super.hasToken() ) {
		    notifyAll();
		    workspace.wait( this );
		}
	    }
	    if( _terminate ) {
		director.removeReadBlock();
		throw new TerminateProcessException( getContainer(),
			"This receiver has been terminated during "
			+ "hasToken()");
	    } else {
		if( getRcvrTime() <= thread.getNextTime() ) {
		    if( thread.hasMinRcvrTime() ) {
			retValue = true;
		    } else {
			RcvrTimeTriple triple;
			triple = thread.getHighestPriorityTriple();
			if( this == triple.getReceiver() ) {
			    retValue = true;
			} else {
			    retValue = false;
			}
		    }
		}
	    }
	    director.removeReadBlock();
	    return retValue;
	}
    }
	*/

	/* FROM get()
        // ODFActor actor = (ODFActor)getContainer().getContainer();
        // ODFDirector director = (ODFDirector)actor.getDirector(); 
        ODFIOPort myPort = (ODFIOPort)getContainer();
        String name = getContainer().getName(); 
        // actor.getName(); 
        Token token = null; 
        synchronized(this) {
            if( _terminate ) {
                throw new TerminateProcessException( getContainer(), "This "
                        + "receiver has been terminated during get().");
            }
            if( super.hasToken() && !_terminate ) {
	        // FIXME: What if current time = -1.0??
	        if( getRcvrTime() <= thread.getNextTime() ) {
		    // if( getRcvrTime() <= actor.getNextTime() ) {
                    if( thread.hasMinRcvrTime() ) {
			// if( actor.hasMinRcvrTime() ) {
                        token = super.get();
                        notifyAll(); 
                        return token;
                    } else if( isSimultaneousIgnore() ) {
                        setSimultaneousIgnore(false);
                        token = super.get();
                        notifyAll(); 
                        return token;
                    }
                    return null;
		}
		// Return null in case rcvrTime is no longer the lowest.
		return null;
            } else if( !_terminate ) {
                director.addReadBlock();
                while( !_terminate && !super.hasToken() ) {
                    notifyAll();
                    workspace.wait( this );
                }
            } 
            if( _terminate ) {
                director.removeReadBlock(); 
                throw new TerminateProcessException( getContainer(), "This "
                        + "receiver has been terminated during get().");
            } else {
	        if( getRcvrTime() <= thread.getNextTime() ) {
		    // if( getRcvrTime() <= actor.getNextTime() ) {
                    if( thread.hasMinRcvrTime() ) {
			// if( actor.hasMinRcvrTime() ) {
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
        }
        
        return token;
	*/


