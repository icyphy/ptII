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
This class is the base class of all atomic actors supporting the 
communicating sequential processes(CSP) model of computation. This 
class provides the methods for controlling which branch of a 
conditional rendezvous construct gets followed.
<p>
FIXME: add longer description.

@author Neil Smyth
@version @(#)CSPActor.java	1.5 08/28/98
*/

public class CSPActor extends AtomicActor implements Runnable {
  
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
   *  @param name the actor's name
   *  @exception IllegalActionException If the entity cannot be contained
   *   by the proposed container.
   *  @exception NameDuplicationException Name coincides with
   *   an entity already in the container
   */	
  public CSPActor(CSPCompositeActor container, String name) 
       throws IllegalActionException, NameDuplicationException {
	 super(container, name);
  }

  ////////////////////////////////////////////////////////////////////////
  ////                         public methods                         ////

  /** Called by ConditionalSend and ConditionalReceive to check if 
   * the calling branch is the first branch to be ready to rendezvous.
   * If it is, it sets a private variable to its branch ID so that
   * subsequent calls to this method by other branches know that they
   * are not first.
   * @param branchNumber The ID assigned to the calling branch upon creation.
   * @return Boolean indicating whether or not the calling branch is the 
   *  first branch to try to rendezvous.
   */
  public boolean amIFirst(int branchNumber) {
    synchronized(_getBranchSucceededLock()) {
      if ((_branchTrying == -1) || (_branchTrying == branchNumber)) {
	//System.out.println(getName() + ": branch " + branchNumber + " got here first"); 
	if (_branchTrying != -1) {
	  //System.out.println("again!!");
	}
	// store branchNumber
	_branchTrying = branchNumber;
	return true;
      }
      return false; // check _
    }
  }

  /** Called by a conditional branch just before it dies.
   * @param branchNumber The ID assigned to the calling branch upon creation.
   */
  public void branchFailed(int branchNumber) {
    if (branchNumber == _successfulBranch) {
      System.out.println("ERROR: failed branch is same as successful branch!");
    }
    synchronized(_getBranchFailedLock()) {
      _branchesActive--; 
      //System.out.println(getName() + ": branch failed: " + branchNumber);
      if (_branchesActive == 0) {
	//System.out.println(getName() + ": Last branch finished, waking up chooseBranch");
      	_getBranchFailedLock().notifyAll(); 
      }
    }
  }

  /** Called by a conditional branch after a successful rendezvous. It
   * wakes up chooseBranch which then proceeds to terminate the 
   * remaining branches.
   * @param branchNumber The ID assigned to the calling branch upon creation.
   */
  public void branchSucceeded(int branchNumber) {
    synchronized(_getBranchSucceededLock() ) {      
      if (_branchTrying != branchNumber) {
	System.out.println("Error: trying not equal to success");
      }
      _successfulBranch = _branchTrying;
      _branchesActive--;
      // wakes up chooseBranch() which wakes up parent thread
      //System.out.println(getName() + ": branch succeeded: " + branchNumber + ", waking up chooseBranch");
      _getBranchSucceededLock().notifyAll(); 
    }
  }
  
  /** Called by ConditionalReceive after a succesful rendezvous. It
   * wakes up chooseBranch which then proceeds to terminate the 
   * remaining branches.
   * @param branch The ID assigned to the calling branch upon creation.
   * @param result The result of the rendezvous.
   */ 
  public void branchSucceeded(int branch, Token result) {
    synchronized(_getBranchSucceededLock() ) {
      _token = result;
      branchSucceeded(branch);
    }
  }

  /** The heart of the conditional rendezvous mechanism. It fires up 
   * the conditional branches passed in as a parameter, waits for one
   * to succeed, and then terminates the remining branches.
   * It returns after all the threads for the conditional branches
   * have finished.
   * @param branches The set of conditional branches involved.
   * @return The ID of the successful branch.
   */
  public int chooseBranch(ConditionalBranch[] branches) {
    try {
      synchronized(_getInternalLock()) {
	// reset the state that controls the conditional branches
	_resetConditionalState();

	//start the branches
	int priority = Thread.currentThread().getPriority() -1;
	_branchesActive = 0;
	for (int i = (branches.length -1); i>=0; i--) {
	  if (branches[i] != null) {
	    // start a thread with this branch
	    Thread t = new Thread((Runnable)branches[i]);
	    t.setPriority(priority);
	    t.start();
	    _branchesActive++;
	  }
	}

	// wait for a branch to suceed
	//System.out.println(getName() + ": wait for a branch to succeed...");
	synchronized(_getBranchSucceededLock()) {
	  while (_successfulBranch == -1) {
	    _getBranchSucceededLock().wait();
	  }
	}
	
	// Now terminate non-successful branches
	for (int i=0; i<branches.length; i++) {
	  // if a branch is null, indicates boolean preceeding communication 
	  // was false.
	  if ( (i!= _successfulBranch) && (branches[i] != null) ) {
	    // to terminate a branch, need to set flag, acquire lock 
	    // on receiver & wake it up 
	    Receiver rec = branches[i].getReceiver();	  
	    synchronized(rec) {
	      branches[i].setAlive(false);
	      rec.notifyAll();
	    }	  
	  }
	}
	
	// when there are no more active branches, branchFailed should 
	// issue a notify on the succeededLock
	synchronized(_getBranchFailedLock()) {
	  while (_branchesActive !=0) {  
	    _getBranchFailedLock().wait();
	  }
	}
	
	if (_branchesActive != 0) { // counter indicating # active branches
	  System.out.println("Error: exiting chooseBranch while still active branches!");
	}
      }
    } catch (Exception ex) {
      System.out.println("Actor interrupted" + ex.getMessage() + ex.getClass().getName());
    }
    //System.out.println(getName() + ": returning from chooseBranch\n");
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
	newobj._branchSucceededLock = new Object();
	newobj._branchFailedLock = new Object();
	newobj._internalLock = new Object();
	newobj._myThread = null;
        newobj._successfulBranch = -1;
	newobj._token = null;
	return newobj;
    }

  /** Start the thread this actor is to be run is.
   */
  public void fire() 
         throws CloneNotSupportedException, IllegalActionException {
	   _myThread.start();
	   System.out.println("Started thread for actor: " + getName());
  }

  /** The token returned by a sucssful ConditionalReceive is stored in 
   * the CSPActor.
   * @return The Token from the last successful conditional receive.
   */
  public Token getToken() {
    return _token;
  }


  /** In CSP, the initilaize method creates the thread to run the actor in.
   *  The thread is started in the fire method.
   *  FIXME: I've added a hack here so that all the ports have getReceivers 
   *  called on them before the threads are started. This is to make 
   *  sure all the receivers are created _before_ the simulation is started.
   */
  public void initialize() 
       throws CloneNotSupportedException, IllegalActionException {
           CSPDirector director = (CSPDirector)getDirector();
	 _myThread = new Thread( director.getProcessGroup(), this, getName());
         System.out.println("Created thread for " + getName() + ", not started");
	 director.actorStarted();
	 Enumeration inputports = inputPorts();
	 while (inputports.hasMoreElements()) {
	   IOPort inport = (IOPort)inputports.nextElement();
	   inport.getReceivers();
	   System.out.println("Created receivers in: " + inport.getName());
	 }
	 
  }

  /** Release the calling branches status as the first branch to
   * try to rendezvous. The branch was obviously not able to complete
   * the rendezvous because the other side of the rendezvous was also
   * conditional and it could not claim the staus of being the first
   * branch. Thus allow another branch the chance to proceed with a
   * rendezvous.
   * @param branchNumber The ID assigned to the branch upon creation.
   */
  public void releaseFirst(int branchNumber) {
    synchronized(_getBranchSucceededLock()) {
      if (branchNumber == _branchTrying) {
	//System.out.println(getName() + ": releasing First : " + branchNumber);
	_branchTrying = -1;
	return;
      }
    }
    System.out.println("Error: branch releaseing first without possessing it! :" + _branchTrying + " & " + branchNumber);
  }
  
  /** Wrap the code form derived actors so that do not have to duplicate 
   *  the code to catch the TerminatedException and to notify the 
   *  director that the process has stopped.
   *  It calls the protected method _run in the derived class.
   */
  public void run() {
    try {
      _run(); //what would be a better name for this method?
      ((CSPDirector)getDirector()).terminateSimulation();
    } catch (TerminateProcessException ex) {
      System.out.println("Actor terminated by exception: " + getName());
    } catch (Exception ex) {
      System.out.println("Exception caught in run method of CSPActor: " + ex.getClass().getName() + ", : " + ex.getMessage() + ": " + getName());
      // FIXME: should nor catch general exception
    } finally {
      // put this here so that always gets called as thread finishes
      // NICE, as do not have to rely on programmer!! 
      //System.out.println("Actor finished: " + getName());
      ((CSPDirector)getDirector()).actorStopped();
    }
  }

  /** Override the base class to ensure that the proposed container
     *  is an instance of CSPCompositeActor or null. If it is, call the
     *  base class setContainer() method. A null argument will remove
     *  the actor from its container.
     *
     *  @param entity The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace, or
     *   if the argument is not a CSPCompositeActor or null.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof CSPCompositeActor) && (container != null)) {
            throw new IllegalActionException(container, this,
                    "CSPActor can only be contained by instances of " +
                    "CSPCompositeActor.");
        }
        super.setContainer(container);
    }

  /** Called by CSPDirector when the simulation is terminated. This method 
   *  sets a flag in all receivers that the simulation has terminated. It
   *  then issues a notifyAll on each receiver so that a TerminatedException 
   *  can be thrown to any processes waiting to rendezvous.
   */
  public void terminate() throws IllegalActionException {
    Enumeration inports = inputPorts();
    while (inports.hasMoreElements()) {
      IOPort port = (IOPort)inports.nextElement();
      if (port.isInput()) {
	Receiver[][] receivers = port.getReceivers();
	for (int i=0; i < receivers.length; i++) {
	  if (receivers[i].length > 1) {
	  System.out.println("Error: more than one receiver on CSP input channel");
	  }
	  CSPReceiver rec = (CSPReceiver)receivers[i][0];
	  System.out.println("Terminating receiver: " + rec.toString());
	  
	  rec.setSimulationTerminated();
	  synchronized(rec) {
	    rec.notifyAll();
	  }
	}
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////
  ////                         protected methods                      ////

  /** in this base class this does nothing. It should be overridden by 
   *  derived classes to do the actor specific work.
   */
  protected void _run() {
    System.out.println("_run() method in CSPActor should be overridden in derived classes!");
  }

  ////////////////////////////////////////////////////////////////////////
  ////                         private methods                        ////

  /* Internal lock used to wake up chooseBranch after the termination 
   * of the last conditional branch. 
   */
  private Object _getBranchFailedLock() {
    return _branchFailedLock;
  }
  
  /* Internal lock used to wake up chooseBranch that one of the 
   * conditional branches has successfully rendezvoused.
   */
  private Object _getBranchSucceededLock() {
    return _branchSucceededLock;
  }
  
  private Object _getInternalLock() {
    return _internalLock;
  }

  /* Resets the internal state controlling the execution of a conditional
   * branching construct (CIF or CDO). It is only called by chooseBranch
   * so that it starts with a consistant state each time.
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

  // Contains the ID of the branch curently trying to rendezvous. It 
  // is -1 if no branch is currently trying.
  private int _branchTrying = -1;

  // Contains the umber of conditioanl branches that are still alive.
  private int _branchesActive = 0;
  
  // This lock is only used internally by the actor. A notifyAll 
  // is only issued on it when the last active branch dies. It is 
  // only waited upon in the chooseBranch method, which will thus 
  // only return after all the conditional branches, that were created 
  // to perform the conditional rendezvous, have died.
  private Object _branchFailedLock = new Object();

  private Object _branchSucceededLock = new Object();

  // This lock is only used internally by the actor. It is used to
  // avoid having to synchronize on the actor itself.
  private Object _internalLock = new Object();

  // Contains the ID of the branch that successfully rendezvoused.
  private int _successfulBranch = -1;

  // Stores the result of a successful conditional receive.
  private Token _token; 

  // Thread this actor is to be run in.
  private Thread _myThread;
}
  
