/* A receiver which stores timed tokens using blocking reads/writes.

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
A receiver which stores timed tokens using blocking reads/writes.

Synchronization Notes:
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
     */
    public ODReceiver(IOPort container) {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do a blocking read on the queue. When tokens are available, 
     *  determine if the next token on this queue has the unique oldest 
     *  rcvrTime associated with it. If so, return the token. Otherwise
     *  return null. If no tokens are available, either block until 
     *  data is available or until the system is terminated. Note: this
     *  method should return null only where it is explicitly shown.
     FIXME: What about addReadBlock and removeReadBlock? Are they
            called symmetrically?
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
	        if( getRcvrTime() <= actor.getCurrentTime() ) {
                    if( actor.hasMinRcvrTime() ) {
                        token = super.get();
                        notifyAll(); // Wake up threads waiting to write.
                        return token;
                    } else if( isSimultaneousIgnore() ) {
                        setSimultaneousIgnore(false);
                        token = super.get();
                        notifyAll(); // Wake up threads waiting to write.
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
	        if( getRcvrTime() <= actor.getCurrentTime() ) {
                    if( actor.hasMinRcvrTime() ) {
                        token = super.get();
                        notifyAll(); // Wake up threads waiting to write.
                    } else if( isSimultaneousIgnore() ) {
                        setSimultaneousIgnore(false);
                        token = super.get();
                        notifyAll(); // Wake up threads waiting to write.
                    }
		}
                director.removeReadBlock(); 
            }
            // FIXME: Will this point ever be reached?
        }
        
        return token;
    }

    /** Throw an IllegalActionException because polling of queues 
     *  is not allowed.
     */
    public boolean hasRoom() {
        return true;
        /*
        throw new IllegalActionException( getContainer(), "This domain"
                + " does not allow polling of input queues.");
        */
    }

    /** Throw an IllegalActionException because polling of queues 
     *  is not allowed.
     */
    public boolean hasToken() {
        // FIXME: This needs to be changed back to "return true;" after testing
        return super.hasToken();
        /*
        throw new IllegalActionException( getContainer(), "This domain"
                + " does not allow polling of output queues.");
        */
    }

    /** Return true if the simultaneous pending event ignore flag is true.
     FIXME: Make this package friendly
     */
    public boolean isSimultaneousIgnore() {
        return _simulIgnoreFlag;
    }
    
    /** Do a blocking write to the queue. Block if the queue is full. 
     *  Associate the current time as the time stamp of the token. 
     * @param token The token to put on the queue.
     */
    public void put(Token token) {
        double currentTime;
        currentTime = ((ODActor)getContainer().getContainer()).getCurrentTime();
        put( token, currentTime );
    }

    /** Do a blocking write to the queue. Block if the queue is full. 
     *  Associate the given time stamp with the token. 
     *  @param token The token to put on the queue.
     *  @param time The time stamp of the token.
     FIXME: What if receiver is full but we want to put a token with
            timestamp = -1 inside??
     */
    public void put(Token token, double time) {
        // System.out.println("\nCall to ODReceiver.put()");
        // System.out.println("Previous queue size = " + getSize() );
        
        // Cache values so that the synchronization hierarchy 
        // will not be violated
        Workspace workspace = getContainer().workspace();
        ODActor actor = (ODActor)getContainer().getContainer();
        ODDirector director = (ODDirector)actor.getDirector();
        
        synchronized(this) {
	  /*
	    if( time == -1.0 ) {
	        return;
	    }
	  */
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

    /** FIXME
     */
    public void setFinish() {
        _terminate = true;
        // notifyAll();
    }
    
    /** FIXME
     */
    public void setPause(boolean flag) {
        ;
    }
    
    /** Ignore the fact that there are simultaneous pending events
     *  if the the argument is true. Otherwise do not ignore. 
     */
    public void setSimultaneousIgnore(boolean flag) {
        _simulIgnoreFlag = flag;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods 		   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _simulIgnoreFlag = false;
    private boolean _terminate = false;

}
