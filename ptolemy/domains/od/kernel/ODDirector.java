/* 

 Copyright (c) 1998 The Regents of the University of California.
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
import ptolemy.actor.*;
import ptolemy.actor.process.*;

//////////////////////////////////////////////////////////////////////////
//// ODDirector 
/** 

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
@version @(#)ODDirector.java	1.3	11/16/98
*/
public class ODDirector extends ProcessDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public ODDirector() {
        super();
    }

    /** Construct a director in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this object.
     */
    public ODDirector(String name) {
        super(name);
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public ODDirector(Workspace workspace, String name) {
        super(workspace, name);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     */
    public void initialize() throws IllegalActionException {
        // System.out.println("ODDirector.initialize()");
        super.initialize();
        System.out.println("Active Count = " + _getActiveActorsCount());
    }
    
    /** 
     */
    public synchronized void addReadBlock() {
        _readBlocks++;
	notifyAll();
        // System.out.println(_readBlocks + " actors are blocked on reads.");
    }
    
    /** 
     */
    public synchronized void addWriteBlock() {
        _writeBlocks++;
	notifyAll();
        // System.out.println(_writeBlocks + " actors are blocked on writes.");
    }

    /* FIXME
     */
    public boolean postfire() throws IllegalActionException {
        // System.out.println("ODDirector.postfire() = "+_notdone);
	return _notdone;
    }
    
    /** Execute all deeply contained actors of the container of this
     *  ODDirector and resolve deadlocked actors. 
     *
     * @exception IllegalActionException If any called method of the
     *  container or one of the deeply contained actors throws it.
     */
    public void fire() throws IllegalActionException {
        // System.out.println("ODDirector.fire()");
        
        while( true ) {
            // System.out.println("Director will wait until deadlock");
            workspace().wait(this);
            // System.out.println("Awakened - now checking for deadlock");
            if( isDeadlocked() ) {
                resolveDeadlock();
                if( isDeadlocked() ) {
		    // System.out.println("End of ODDirector.fire()");
                    _notdone = false;
                    return;
                }
            }
        }
    }
    
    /** FIXME
     */
    public boolean hasMutation() {
        boolean dummy = false;
        return dummy;
    }
    
    /** FIXME
     */
    public synchronized boolean isDeadlocked() {
        if( _getActiveActorsCount() == _readBlocks + _writeBlocks ) {
	  // System.out.println("All actors blocked - Deadlock!");
            return true;
        }
        return false;
    }
    
    /** Return a new receiver of a type compatible with this director.
     *  @return A new ODReceiver.
     */
    public Receiver newReceiver() {
        return new ODReceiver();
    }

    /** 
     */
    public synchronized void removeReadBlock() {
        if( _readBlocks > 0 ) {
            _readBlocks--;
        }
        // System.out.println(_readBlocks + " actors are blocked on reads.");
    }
    
    /** 
     */
    public synchronized void removeWriteBlock() {
        if( _writeBlocks > 0 ) {
            _writeBlocks--;
        }
        // System.out.println(_writeBlocks + " actors are blocked on writes.");
    }
    
    /** FIXME
     */
    public void resolveDeadlock() {
        // System.out.println("*** Deadlock Needs To Be Resolved!!!");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////
    
    protected void _performMutations() {
        ;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    private int _readBlocks = 0;
    private int _writeBlocks = 0;

}















