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
import ptolemy.actor.util.*;

import java.util.NoSuchElementException;

//////////////////////////////////////////////////////////////////////////
//// ODReceiver
/**
A receiver which stores timed tokens using blocking reads/writes.


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
     */
    public synchronized Token get() {
        // System.out.println("\nCall to ODReceiver.get()");
        // System.out.println("Previous rcvrTime = " + getRcvrTime() );
        Workspace workspace = getContainer().workspace();
        Actor actor = (Actor)getContainer().getContainer();
        ODDirector director = (ODDirector)actor.getDirector();
        Token token = null;
        try {
            if( super.hasToken() ) {
                ODActor odactor = (ODActor)getContainer().getContainer();
                // System.out.println("rcvrTime = " + getRcvrTime() );
                // System.out.println("actor time = " + odactor.getCurrentTime() );
                if( getRcvrTime() <= odactor.getCurrentTime() ) {
                    if( odactor.hasMinRcvrTime() ) {
                        return super.get();
                    } else if( isSimultaneousIgnore() ) {
                        setSimultaneousIgnore(false);
                        return super.get();
                    }
                    System.out.println("First null");
                    return null;
                }
                // FIXME: Will this point ever be reached? 
                System.out.println("Second null");
                return null;
            } else {
                director.addReadBlock();
                while( !_terminate && !super.hasToken() ) {
                    notifyAll();
                    workspace.wait( this );
                }
            } 
            if( _terminate ) {
                director.removeReadBlock(); 
                new TerminateProcessException( getContainer(), "This "
                        + "receiver has been terminated during get().");
            } else {
                ODActor odactor = (ODActor)getContainer().getContainer();
                if( getRcvrTime() <= odactor.getCurrentTime() ) {
                    if( odactor.hasMinRcvrTime() ) {
                        token = super.get();
                    } else if( isSimultaneousIgnore() ) {
                        setSimultaneousIgnore(false);
                        return super.get();
                    }
                }
                director.removeReadBlock(); 
            }
            // FIXME: Will this point ever be reached?
            
        } catch(IllegalActionException e) {
	    System.out.println("ODReceiver.get() Exception");
            // Do nothing. This can't happen.
        }
        
        // This check is only for clarity.
        if( token == null ) {
            return null;
        }
        return token;
    }

    /** Throw an IllegalActionException because polling of queues 
     *  is not allowed.
     * @exception IllegalActionException Not thrown in this class.
     */
    public boolean hasRoom() throws IllegalActionException {
        throw new IllegalActionException( getContainer(), "This domain"
                + " does not allow polling of input queues.");
    }

    /** Throw an IllegalActionException because polling of queues 
     *  is not allowed.
     * @exception IllegalActionException Not thrown in this class.
     */
    public boolean hasToken() throws IllegalActionException {
        throw new IllegalActionException( getContainer(), "This domain"
                + " does not allow polling of output queues.");
    }

    /** Return true if the simultaneous pending event ignore flag is true.
     */
    public boolean isSimultaneousIgnore() {
        return _simulIgnoreFlag;
    }
    
    /** Do a blocking write to the queue. Block if the queue is full. 
     *  Associate the current time as the time stamp of the token. 
     * @param token The token to put on the queue.
     */
    public synchronized void put(Token token) {
        double currentTime;
        currentTime = ((ODActor)getContainer().getContainer()).getCurrentTime();
        put( token, 0.0 );
    }

    /** Do a blocking write to the queue. Block if the queue is full. 
     *  Associate the given time stamp with the token. 
     *  @param token The token to put on the queue.
     *  @param time The time stamp of the token.
     */
    public synchronized void put(Token token, double time) {
        // System.out.println("\nCall to ODReceiver.put()");
        // System.out.println("Previous queue size = " + getSize() );
        Workspace workspace = getContainer().workspace();
        ODActor actor = (ODActor)getContainer().getContainer();
        ODDirector director = (ODDirector)actor.getDirector();
        try {
            if( !super.hasRoom() ) {
                director.addWriteBlock();
                while( !_terminate && !super.hasRoom() ) {
                    notifyAll();
                    workspace.wait( this );
                }
            } else if( !_terminate ) {
                /* Moved to TimedQueueReceiver.java
                if( !super.hasToken() ) {
                    RcvrTimeTriple triple;
                    triple = new RcvrTimeTriple( this, time, getPriority() );
                    actor.updateRcvrTable( triple );
                }
                */
                super.put(token, time);
		return;
            } 
            
            if( _terminate ) {
                director.removeWriteBlock(); 
                new TerminateProcessException( getContainer(), "This "
                        + "receiver has been terminated during put().");
            } else {
                director.removeWriteBlock(); 
                /* Moved to TimedQueueReceiver.java
                if( !super.hasToken() ) {
                    RcvrTimeTriple triple;
                    triple = new RcvrTimeTriple( this, time, getPriority() );
                    actor.updateRcvrTable( triple );
                }
                */
                super.put(token, time);
            }
        } catch(IllegalActionException e) {
	    System.out.println("ODReceiver.put() Exception");
            // Do nothing. This won't happen.
        }
    }

    /** FIXME
     */
    public void setFinish() {
        ;
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
