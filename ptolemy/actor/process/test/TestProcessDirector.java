/* An atomic actor for testing Process Director.

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

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (davisj@eecs.berkeley.edu)

*/

package ptolemy.actor.process.test;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// TestProcessDirector
/**
A TestProcessDirector is a simple atomic actor that is used for testing the
actor package constructs for Processes. It overrides the action methods to
return false in the postfire after the first invocation of fire method.

@author John S. Davis II
@version $Id$
*/
public class TestProcessDirector extends ProcessDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public TestProcessDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public TestProcessDirector(Workspace workspace) {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     *  @exception IllegalActionException If the name contains a period,
     *   or if the director is not compatible with the specified container.
     */
    public TestProcessDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Record the firing and force postfire to return false.
     */
    public void fire() {
    }

    /** Implementations of this method must be synchronized.
     * @param internal True if internal read block.
     */
    protected synchronized void _actorBlocked(ProcessReceiver rcvr) {
    	_blockedInternalRcvrList.addFirst(rcvr);
        _actorsBlocked++;
        notifyAll();
    }

    /** Implementations of this method must be synchronized.
     * @param internal True if internal read block.
     */
    protected synchronized void _actorBlocked(LinkedList blockedRcvrs) {
        Iterator rcvrs = blockedRcvrs.iterator();
        ProcessReceiver rcvr = null;
        while( rcvrs.hasNext() ) {
            rcvr = (ProcessReceiver)rcvrs.next();
            _blockedInternalRcvrList.addFirst( rcvr ) ;
    	}
        /*
    	_blockedInternalRcvrList.addAll(
        	Collections.synchronizedCollection(rcvrs));
        */
        _actorsBlocked++;
        notifyAll();
    }

    /** Implementations of this method must be synchronized.
     * @param internal True if internal read block.
     */
    protected synchronized void _actorUnBlocked(ProcessReceiver rcvr) {
    	_blockedInternalRcvrList.remove(rcvr);
        while( _blockedInternalRcvrList.contains(rcvr) ) {
    	    _blockedInternalRcvrList.remove(rcvr);
        }
        _actorsBlocked--;
        notifyAll();
    }

    /** Implementations of this method must be synchronized.
     * @param internal True if internal read block.
     */
    protected synchronized void _actorUnBlocked(LinkedList blockedRcvrs) {
        Iterator rcvrs = blockedRcvrs.iterator();
        ProcessReceiver rcvr = null;
        while( rcvrs.hasNext() ) {
            _blockedInternalRcvrList.remove(rcvr);
            while( _blockedInternalRcvrList.contains(rcvr) ) {
    		_blockedInternalRcvrList.remove(rcvr);
            }
        }
        _actorsBlocked--;
        notifyAll();
    }

    /** 
     */
    protected synchronized void _branchCntlrBlocked() {
    }

    /** 
     */
    protected synchronized void _branchCntlrUnBlocked() {
    }

    /** Record the invocation, then return true if fire was never called.
     *  Else return false.
    public boolean postfire() {
        super.postfire();
        return _notdone;
    }
     */

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    private LinkedList _blockedInternalRcvrList = new LinkedList();
    private LinkedList _blockedExternalRcvrList = new LinkedList();
    
    private int _actorsBlocked = 0;

}
