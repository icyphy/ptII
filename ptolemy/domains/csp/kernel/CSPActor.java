/* Parent class of all atomic CSP actors.

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

@ProposedRating Red (nsmyth@eecs.berkeley.edu)
@AcceptedRating none

*/

package ptolemy.domains.csp.kernel;

import ptolemy.data.Token;
import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// CSPActor
/**
This class is the base class of all atomic actors using the 
non-deterministic communication and timed features of  the communicating 
sequential processes(CSP) domain. 
<p>
The model of computation used in this domain extends the original CSP 
model of computation in two ways. First it allows non-deterministic 
communication using both sends and receives. The original model only
allowed non-deterministic receives. Second, we have added a centralised 
notion of time. The original proposal was untimed. Neither of these 
extensions are new, but it is worth noting the differences between 
the model we use and the original model.
<p>
This class provides the methods for controlling which branch of a
conditional rendezvous construct gets followed. It controls the branches
performing the conditional rendezvous in three steps. First it starts them.
Then it waits for one branch to succeed, after which it wakes up and
terminates the remaining branches. When the last conditional branch
thread has finished it allows the actor thread to continue.
<p>
Time is supported by two methods, delay() and delay(double). If the simulation
is timed. the first method just pauses the actor until the next time 
artificial deadlock is reached and time is advanced. The second method 
pauses the actor until time is advanced the argument time from the 
current simulation time. Thus time is centralised in that it is 
controlled by the director controlling this actor, and that 
each actor can only deal with delta (as opposed to absolute) time.
<p>
FIXME: should a bunch of the methods in this class be protected? In
Particular, actorBlocked, actor Stopped etc?

@author Neil Smyth
@version $Id$
*/

public class CSPActor extends AtomicActor {

    /** Construct a CSPActor in the default workspace with an empty string
     *  as its name.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public CSPActor() {
        super();
    }

    /** Construct a CSPActor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *
     *  @param workspace The workspace that will list the entity.
     */
    public CSPActor(Workspace workspace) {
        super(workspace);
    }

    /** Construct a CSPActor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The CompositeActor that contains this actor.
     *  @param name the actor's name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException Name coincides with
     *   an entity already in the container.
     */
    public CSPActor(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
                super(container, name);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Called by ConditionalSend and ConditionalReceive to check if
     *  the calling branch is the first branch to be ready to rendezvous.
     *  If it is, it sets a private variable to its branch ID so that
     *  subsequent calls to this method by other branches know that they
     *  are not first.
     *  @param branchNumber The ID assigned to the calling branch
     *   upon creation.
     *  @return Boolean indicating whether or not the calling branch is the
     *   first branch to try to rendezvous.
     */
    public boolean amIFirst(int branchNumber) {
        synchronized(_getInternalLock()) {
            if ((_branchTrying == -1) || (_branchTrying == branchNumber)) {
                //System.out.println(getName() + ": branch " + branchNumber + " got here first");
                // store branchNumber
                _branchTrying = branchNumber;
                return true;
            }
            return false;
        }
    }

    /** Called by a conditional branch just before it dies.
     *  @param branchNumber The ID assigned to the calling branch
     *   upon creation.
     */
    public void branchFailed(int branchNumber) {
        if (_successfulBranch == branchNumber) {
            // simulation must have been terminated.
            _successfulBranch = -1;
        }
        synchronized(_getInternalLock()) {
            _branchesActive--;
            //System.out.println(getName() + ": branch failed: " + branchNumber);
            if (_branchesActive == 0) {
                //System.out.println(getName() + ": Last branch finished, waking up chooseBranch");
                _getInternalLock().notifyAll();
            }
        }
    }

    /** Called by a conditional branch after a successful rendezvous. It
     *  wakes up chooseBranch which then proceeds to terminate the
     *  remaining branches.
     *  @param branchNumber The ID assigned to the calling branch
     *   upon creation.
     */
    public void branchSucceeded(int branchNumber) {
        synchronized(_getInternalLock() ) {
            if (_branchTrying != branchNumber) {
                System.out.println("Error: trying not equal to success");
            }
            _successfulBranch = _branchTrying;
            _branchesActive--;
            // wakes up chooseBranch() which wakes up parent thread
            //System.out.println(getName() + ": branch succeeded: " + branchNumber + ", waking up chooseBranch");
            _getInternalLock().notifyAll();
        }
    }

    /** Called by ConditionalReceive after a successful rendezvous. It
     *  wakes up chooseBranch which then proceeds to terminate the
     *  remaining branches.
     *  @param branch The ID assigned to the calling branch upon creation.
     *  @param result The result of the rendezvous.
     */
    public void branchSucceeded(int branch, Token result) {
        synchronized(_getInternalLock() ) {
            _token = result;
            branchSucceeded(branch);
        }
    }

    /** The heart of the conditional rendezvous mechanism. It fires up
     *  the conditional branches passed in as a parameter, waits for one
     *  to succeed, and then terminates the remining branches.
     *  It returns after all the threads for the conditional branches
     *  have finished.
     *  FIXME: need to work out id to return if no branches were created 
     *  and when the conditional construct was terminated prematurely.
     *  @param branches The set of conditional branches involved.
     *  @return The ID of the successful branch.
     */
    public int chooseBranch(ConditionalBranch[] branches) {
        try {
            synchronized(_getInternalLock()) {
                // reset the state that controls the conditional branches
                _resetConditionalState();

                //start the branches
                int priority = Thread.currentThread().getPriority() -1;
                _branchesActive = 0;
                for (int i = 0; i< branches.length; i++) {
                    if (branches[i] != null) {
                        // start a thread with this branch
                        Thread thread = new Thread((Runnable)branches[i]);
                        thread.setPriority(priority);
                        thread.start();
                        _branchesActive++;
                    }
                }
                if (_branchesActive == 0) {
                    //FIXME: this test should go into the actor, or 
                    // should we return a flag?
                    System.out.println("No branches to create, returning");
                }
                //FIXME: we could perhaps get a performance gain by 
                //testing if only one branch was started here?
  
                // wait for a branch to succeed
                while ((_successfulBranch == -1) && (_branchesActive > 0)) {
                    // FIXME: is it possible to have -1 active Branches?
                    _getInternalLock().wait();
                }

                // Now terminate non-successful branches
                for (int i = 0; i<branches.length; i++) {
                    // if a branch is null, indicates boolean
                    // preceding communication was false.
                    if ( (i!= _successfulBranch) && (branches[i] != null) ) {
                        // to terminate a branch, need to set flag, acquire
                        // lock on receiver & wake it up
                        Receiver rec = branches[i].getReceiver();
                        synchronized(rec) {
                            branches[i].setAlive(false);
                            rec.notifyAll();
                        }
                    }
                }

                // when there are no more active branches, branchFailed should
                // issue a notifyAll on the internalLock
                while (_branchesActive != 0) {
                    _getInternalLock().wait();
                }
                // counter indicating # active branches, should be zero
                if (_branchesActive != 0) {
                    System.out.println("Error: exiting chooseBranch while still active branches!");
                }
            }
        } catch (InterruptedException ex) {
            System.out.println(getName() + " interrupted in chooseBranch.");
        }
        if (_successfulBranch == -1) {
            // Conditional construct was terminated prematurely
            String msg = "CSPActor: exiting conditional branching due to";
            throw new TerminateProcessException(this, msg + " termination");
        }
        return _successfulBranch;
    }

    /** Clone this actor into the specified workspace. The new actor is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new actor with the same ports as the original, but
     *  no connections and no container.  A container must be set before
     *  much can be done with the actor.
     *
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new CSPActor.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        CSPActor newobj = (CSPActor)super.clone(ws);
        newobj._branchesActive = 0;
	newobj._branchTrying = -1;
        newobj._internalLock = new Object();
        newobj._successfulBranch = -1;
	newobj._token = null;
	return newobj;
    }

    /** Delay until the director advances time. If the simulation is 
    *  not timed do nothing.
    *  FIXME: time not implemented yet.
    */
    public void delay() {
        return;
    }

    /** Delay the director advances time delta amount. If the simulation is 
    *  not timed do nothing.
    *  FIXME: time not implemented yet.
    *  @param The delta time to delay this actor by.
    */
    public void delay(double delta) {
        return;
    }

    /** The token returned by a successful ConditionalReceive is stored in
     *  the CSPActor.
     *  @return The Token from the last successful conditional receive.
     */
    public Token getToken() {
        return _token;
    }

    /** Default implementation for CSPActors is to return false. If an 
     *  actor wishes to go for more than one iteration it should 
     *  override this method to return true.
     *  @return Boolean indicating if another iteration can occur.
     */
    public boolean postfire() {
        return false;
    }

    /** Release the calling branches status as the first branch to
     *  try to rendezvous. The branch was obviously not able to complete
     *  the rendezvous because the other side of the rendezvous was also
     *  conditional and the other side could not claim the status of
     *  being the first branch. Thus allow another branch the chance
     *  to proceed with a rendezvous.
     *  @param branchNumber The ID assigned to the branch upon creation.
     */
    public void releaseFirst(int branchNumber) {
        synchronized(_getInternalLock()) {
            if (branchNumber == _branchTrying) {
                //System.out.println(getName() + ": releasing First : " + branchNumber);
                _branchTrying = -1;
                return;
            }
        }
        System.out.println("Error: branch releasing first without possessing it! :" + _branchTrying + " & " + branchNumber);
    }

    /** Defauly implementation for actors inheriting from this 
     *class. It simply prints out a message that the actor is wrapping up.
     */
     public void wrapup() {
         System.out.println("CSPActor: " + getName() + " wrapping up.");
     }

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////

    /* Internal lock used to for controlling conditional rendezvous
     * constructs.
     */
    private Object _getInternalLock() {
        return _internalLock;
    }

    /* Resets the internal state controlling the execution of a conditional
     * branching construct (CIF or CDO). It is only called by chooseBranch
     * so that it starts with a consistent state each time.
     */
    private void _resetConditionalState() {
        //System.out.println("reseting conditional state in: " +getName());
        synchronized(_getInternalLock()) {
            _branchTrying = -1;
            _successfulBranch = -1;
            _branchesActive = 0;
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Contains the ID of the branch currently trying to rendezvous. It
    // is -1 if no branch is currently trying.
    private int _branchTrying = -1;

    // Contains the umber of conditional branches that are still alive.
    private int _branchesActive = 0;

    // This lock is only used internally by the actor. It is used to
    // avoid having to synchronize on the actor itself. The chooseBranch
    // method waits on it so it knows when a branch has succeeded and when
    // the last branch it created has died.
    private Object _internalLock = new Object();

    // Flag indicating the simulation has been terminated
    // private boolean _simulationTerminated = false;

    // Contains the ID of the branch that successfully rendezvoused.
    private int _successfulBranch = -1;

    // Stores the result of a successful conditional receive.
    private Token _token;
}
