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
import ptolemy.actor.TerminateProcessException;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import collections.LinkedList;
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
Time is supported by two methods, delay() and delay(double). These methods 
do nothing if the simulation is untimed. If the simulation
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

    /** Increase the count of conditional branches that are blocked.
     *  Check if all the conditional branches are blocked, and if so 
     *  register this actor as being blocked.
     */
    public void branchBlocked() {
        //System.out.println(getName() + ": branchBlocked.");
        synchronized(_getInternalLock()) {
            _branchesBlocked++;
            // System.out.println(getName() + ": number of blocked branches is " +
                    //   _branchesBlocked + ", alive is " + _branchesActive);
            if (_branchesBlocked == _branchesStarted) {
                System.out.println(getName() + ": all branches are blocked.");
                // Note: acquiring a second lock, need to be careful.
                ((CSPDirector)getDirector()).actorBlocked();
            }
        }
    }

    /** Called by a conditional branch just before it dies.
     *  @param branchNumber The ID assigned to the calling branch
     *   upon creation.
     */
    public void branchFailed(int branchNumber) {
        if (_successfulBranch == branchNumber) {
            // simulation must have finished.
            _successfulBranch = -1;
        }
        synchronized(_getInternalLock()) {
            _branchesActive--;
            System.out.println(getName() + ": branch failed: " +
                      branchNumber);
            if (_branchesActive == 0) {
                System.out.println(getName() + ": Last branch finished, " +
                       "waking up chooseBranch");
                _getInternalLock().notifyAll();
            }
        }
    }

    /** Called by ConditionalSend after a successful rendezvous. It
     *  wakes up chooseBranch which then proceeds to terminate the
     *  remaining branches.
     *  @param branchNumber The ID assigned to the calling branch
     *   upon creation.
     */
    public void branchSucceeded(int branchNumber) {
        synchronized(_getInternalLock() ) {
            if (_branchTrying != branchNumber) {
                System.out.println(getName() + ": branchSucceeded called " +
                        "with a branch id not equal to the id of the branch" +
                        " registered as trying.");
            }
            _successfulBranch = _branchTrying;
            _branchesActive--;
            // wakes up chooseBranch() which wakes up parent thread
            System.out.println(getName() + ": branch succeeded: " +
                      branchNumber + ", waking up chooseBranch");
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

    /** Decrease the count of conditional branches that are blocked.
     *  Check if all the conditional branches were previously blocked, 
     *  and if so register this actor with the director as being unblocked.
     */
    public void branchUnblocked() {
        // System.out.println(getName() + ": branchUnBlocked.");
         synchronized(_getInternalLock()) {
            //System.out.println(getName() + ": UUnumber of blocked branches " +
                    //     "is " + _branchesBlocked + ", alive is " + _branchesActive);
            if (_branchesBlocked == _branchesStarted) {
                System.out.println(getName() + ": all branches WERE blocked.");
                // Note: acquiring a second lock, need to be careful.
                ((CSPDirector)getDirector()).actorUnblocked();
            }
            _branchesBlocked--;
        }
    }

    /** Determine which branch suceeds in rendezvousing. This method is 
     *  central to nondeterministic rendezvous. It is passed in an array
     *  of branches, each element of which represents one of the 
     *  conditional rendezvous branches. If the entry in the array  is 
     *  null, this means that the guard preceeding the communication 
     *  command was false. It returns the id of the successful branch, 
     *  or -1 if all of the entries in the array are null.
     *  <p>
     *  If more than one entries in the array are non-null, a thread 
     *  is created and started for each of the non-null branches. 
     *  These threads each try to rendezvous until one succeeds. After
     *  a thread succeeds the other threads are killed, and the id of 
     *  the successful branch is returned.
     *  <p>
     *  If exactly one of the entries in the arry is non-null, then 
     *  the communication is perfromed direclt and the id of this branch 
     *  is returned.
     *  @param branches The set of conditional branches involved.
     *  @return The ID of the successful branch, or -1 if all of the 
     *   entries in the branch array are null.
     */
    public int chooseBranch(ConditionalBranch[] branches) {
        try {
            synchronized(_getInternalLock()) {
                // reset the state that controls the conditional branches
                _resetConditionalState();

                // Create the threads for the branches.
                _threadList = new LinkedList();
                ConditionalBranch onlyBranch = null;
                for (int i = 0; i< branches.length; i++) {
                    // If an entry in the array is null it means the guard 
                    // preceeding the conditional communication was false.
                    if (branches[i] != null) {
                        // Create a thread for this branch
                        Nameable act = (Nameable)branches[i].getParent();
                        String name = act.getName() + branches[i].getID();
                        Thread t = new Thread((Runnable)branches[i], name);
                        _threadList.insertFirst(t);
                        onlyBranch = branches[i];
                    }
                }
            
                // Three cases: 1) No guards were true so return -1
                // 2) Only one guard was true so perform the rendezvous 
                // directly, 3) More than one guard was true, so start 
                // the thread for each branch and wait for one of them 
                // to rendezvous.
                int num = _threadList.size();

                if (num == 0) {
                    // The guards preceeding all the conditional 
                    // communications were false, so no branches to create.
                    System.out.println("No branches to create, returning");
                    return _successfulBranch; // will be -1
                } else if (num == 1) {
                    // Only one guard was true, so perform simple rendezvous.
                    System.out.println("Only one branch to create...\n");
                    if (onlyBranch instanceof ConditionalSend) {
                        Token t = ((ConditionalSend)onlyBranch)._token;
                        onlyBranch.getReceiver().put(t);
                        return onlyBranch.getID();
                    } else {
                        // branch is a ConditionalReceive
                        _token = onlyBranch.getReceiver().get();
                        return onlyBranch.getID();
                    }
                } else {
                    // Have a proper conditional communication.
                    // Start the threads for each branch.
                    Enumeration threads = _threadList.elements();
                    while (threads.hasMoreElements()) {
                        Thread thread = (Thread)threads.nextElement();
                        thread.start();
                        _branchesActive++;
                    }
                    _branchesStarted = _branchesActive;
                    System.out.println(_branchesStarted + " branches created..\n");
                    // wait for a branch to succeed
                    while ((_successfulBranch == -1) && 
                            (_branchesActive > 0)) {
                        _getInternalLock().wait();
                    }
                }
            }
            // If get to here have more than one conditional branch.
            System.out.println("Now terminating non successful branches...");
            
            LinkedList tmp = new LinkedList();
            
            // Now terminate non-successful branches
            for (int i = 0; i < branches.length; i++) {
                // if a branch is null, indicates boolean
                // preceding communication was false.
                if ( (i!= _successfulBranch) && (branches[i] != null) ) {
                    // to terminate a branch, need to set flag 
                    // on receiver & wake it up
                    Receiver rec = branches[i].getReceiver();
                    tmp.insertFirst(rec);
                    branches[i].setAlive(false);
                }
            }
            // Now wake up all the receivers.
            NotifyThread obj = new NotifyThread(tmp);
            synchronized(obj) {
                (new Thread(obj)).start();
                obj.wait();
            }
            // when there are no more active branches, branchFailed
            // should issue a notifyAll on the internalLock
            synchronized(_getInternalLock()) {
                while (_branchesActive != 0) {
                    _getInternalLock().wait();
                }
                // counter indicating # active branches, should be zero
                if (_branchesActive != 0) {
                    throw new InvalidStateException(getName() +
                    ": chooseBranch() is exiting with branches" +
                    " still active.");
                }
            }
        } catch (InterruptedException ex) {
            // FIXME: what should happen here?
            throw new TerminateProcessException(this, "CSPActor.chooseBranch" +
            " interrupted.");
        }
        if (_successfulBranch == -1) {
            // Conditional construct was ended prematurely
            throw new TerminateProcessException(this, "CSPActor: exiting " +
            "conditional branching due to TerminateProcessException.");
        }
        _threadList = null;
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
        newobj._branchesBlocked = 0;
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
     *  conditional and could not claim the status of
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
        throw new InvalidStateException(getName() + ": Error: branch " +
               "releasing first without possessing it! :" + _branchTrying + 
               " & " + branchNumber);
    }

    /** Terminate abruptly any threads created by this actor. Note that 
     *  this method does not allow the threads to terminate gracefully.
     */   
    public void terminate() {
        synchronized(_getInternalLock()) {
            System.out.println("Terminating actor: " + getName());
            // Now stop any threads created by this director.
             if (_threadList != null) {
                 Enumeration threads = _threadList.elements();
                 while (threads.hasMoreElements()) {
                     Thread next = (Thread)threads.nextElement();
                     if (next.isAlive()) {
                         next.stop();
                     }
                 }
             }
        }
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
            _branchesActive = 0;
            _branchesBlocked = 0;
            _branchesStarted = 0;
            _branchTrying = -1;
            _successfulBranch = -1;         
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Contains the number of conditional branches that are still alive.
    private int _branchesActive = 0;

    // Contains the number of conditional branches that are blocked 
    // trying to rendezvous.
    private int _branchesBlocked = 0;

    // Contains the number of branches that were actually started for 
    // the most recent conditional rendezvous.
    private int _branchesStarted = 0;

    // Contains the ID of the branch currently trying to rendezvous. It
    // is -1 if no branch is currently trying.
    private int _branchTrying = -1;

    // This lock is only used internally by the actor. It is used to
    // avoid having to synchronize on the actor itself. The chooseBranch
    // method waits on it so it knows when a branch has succeeded and when
    // the last branch it created has died.
    private Object _internalLock = new Object();

    // Flag indicating the simulation has been terminated
    // private boolean _simulationTerminated = false;

    // Contains the ID of the branch that successfully rendezvoused.
    private int _successfulBranch = -1;

    // Threads created by this actor to perfrom a conditional rendezvous.
    // Need to keep a list of them in case the simulation is 
    // terminated abruptly.
    private LinkedList _threadList = null;

    // Stores the result of a successful conditional receive.
    private Token _token;
}
