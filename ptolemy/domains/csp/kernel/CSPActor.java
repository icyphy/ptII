/* Parent class of all atomic CSP actors.

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

@ProposedRating Green (nsmyth@eecs.berkeley.edu)
@AcceptedRating Yellow (liuj@eecs.berkeley.edu)

*/

package ptolemy.domains.csp.kernel;


import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;
import ptolemy.data.Token;

//////////////////////////////////////////////////////////////////////////
//// CSPActor
/**
   This class is the base class of all atomic actors using the
   non-deterministic communication and timed features of  the communicating
   sequential processes(CSP) domain.
   <p>
   Two conditional communication constructs are available: "Conditional if"
   (CIF) and "Conditional do" (CDO). The constructs are analogous to,
   but different from, the common <I>if</I> and <I>do</I> statements. The
   steps involved in using both of these are
   <BR>(1) create the branches involved and assign an identification number
   to each branch.
   <BR>(2) call the chooseBranch() method, which invokes the chooseBranch()
   method of the controller to determine which branch should succeed.
   <BR>(3) execute the statements associated with the successful branch.
   <P>
   Each branch is either an instance of ConditionalSend or ConditionalReceive,
   depending on the communication in the branch. Please see these classes for
   details on <I>guarded communication statements</I>, which they represent.
   The identification number assigned to each branch only needs to identify
   the branch uniquely for one sequence of the steps above. A good example
   of how to use a CDO is the code in the actor CSPBuffer, in the
   ptolemy.domains.csp.lib package. One significant difference between a
   CDO (or CIF) and a common <I>do</I> (<I>if</I>) is that all the branches
   are evaluated in parallel, as opposed to sequentially.

   <p>The chooseBranch() method takes an array of the branches as an
   argument, and simply passes the branches to the chooseBranch() method
   of the controller to decide which branch is successful. The successful
   branch is the branch that succeeds with its communication. See the
   chooseBranch() method of ConditionalBranchController for details
   about how the successful branch is chosen.

   <p>Time is supported by the method delay(double). This delays the
   process until time has advanced the argument time from the current
   model time.  If this method is called with a zero argument, then
   the process continues immediately. As far as each process is
   concerned, time can only increase while the process is blocked
   trying to rendezvous or when it is delayed. A process can be aware
   of the current model time, but it should only affect the model time
   through delays. Thus time is centralized in that it is advanced by
   the director controlling the process represented by this actor.

   <p>A process can also choose to delay its execution until the next
   occasion a deadlock occurs by calling _waitForDeadlock(). The
   process resumes at the same model time at which it delayed. This is
   useful if a process wishes to delay itself until some changes to
   the topology have been carried out.

   <p> The model of computation used in this domain extends the
   original CSP, as proposed by Hoare in 1978, model of computation in
   two ways.  First it allows non-deterministic communication using
   both sends and receives. The original model only allowed
   non-deterministic receives.  Second, a centralized notion of time
   has been added. The original proposal was untimed. Neither of these
   extensions are new, but it is worth noting the differences between
   the model used here and the original model. If an actor wishes to
   use either non-deterministic rendezvous or time, it must derive
   from this class. Otherwise deriving from AtomicActor is sufficient.

   <p>
@author Neil Smyth, Bilung Lee
@version $Id$
@see ConditionalBranch
@see ConditionalReceive
@see ConditionalSend
*/

public class CSPActor extends TypedAtomicActor
    implements ConditionalBranchActor {

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
     * @param container The TypedCompositeActor that contains this actor.
     * @param name The actor's name.
     * @exception IllegalActionException If the entity cannot be contained
     *  by the proposed container.
     * @exception NameDuplicationException If the name argument coincides
     *  with an entity already in the container.
     */
    public CSPActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	_conditionalBranchController =
	    new ConditionalBranchController(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Determine which branch succeeds with a rendezvous. Pass
     *  the branches to ConditionalBranchController to decide.
     *  <p>
     *  @param branches The set of conditional branches involved.
     *  @return The ID of the successful branch, or -1 if none of the
     *   branches were enabled.
     */
    public int chooseBranch(ConditionalBranch[] branches) {
        return _conditionalBranchController.chooseBranch(branches);
    }

    /** Clone this actor into the specified workspace. The new actor is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new actor with the same number of ports (cloned)
     *  as the original, but no connections and no container.
     *  A container must be set before much can be done with the actor.
     *  <p>
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new CSPActor.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        CSPActor newobj = (CSPActor)super.clone(ws);
        newobj._delayed = false;
        newobj._conditionalBranchController =
	    new ConditionalBranchController(newobj);
	return newobj;
    }

    /** Delay this actor. The actor resumes executing when the
     *  director advances the current model time to
     *  "getCurrentTime() + delta". If the actor tries to
     *  delay for a negative time, an exception is thrown. A delay
     *  of zero time has no effect and this method returns immediately.
     *  @param delta The time to delay this actor for from the current
     *   time.
     *  @exception IllegalActionException If the argument is negative.
     *  @exception ProcessTerminationException If the director requests
     *   termination by calling the protected method _cancelDelay().
     */
    public void delay(double delta) throws IllegalActionException {
        try {
            synchronized(_internalLock) {
	        if (delta == 0.0) {
		    return;
		} else if (delta < 0.0) {
		    throw new IllegalActionException(this,
                            "delay() called with a negative argument: "
                            + delta);
		} else {
		    _delayed = true;
		    ((CSPDirector)getDirector())._actorDelayed(delta, this);
		    while(_delayed) {
		        _internalLock.wait();
		    }
                    if (_cancelDelay) {
                        throw new TerminateProcessException("delay cancelled");
                    }
		}
	    }
        } catch (InterruptedException ex) {
            throw new TerminateProcessException("CSPActor interrupted " +
                    "while delayed." );
        }
    }

    /** Return the conditional branch control of this actor.
     */
    public ConditionalBranchController getConditionalBranchController() {
	return _conditionalBranchController;
    }

    /** Initialize the state of the actor.
     *  @throws IllegalActionException Not thrown in this class, but might
     *   be in a derived class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _delayed = false;
        _cancelDelay = false;
    }

    /** Return false. If an actor wishes to continue for more than
     *  one iteration it should override this method to return true.
     *  @return True if another iteration can occur.
     */
    public boolean postfire() {
        return false;
    }

    /** Terminate abruptly any threads created by this actor. Note that
     *  this method does not allow the threads to terminate gracefully.
     */
    public void terminate() {
        synchronized(_internalLock) {
	    _conditionalBranchController.terminate();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** If the actor is delayed, then cancel the delay.
     *  Some time after this method is called, the thread that called
     *  delay() will be restarted and the delay() method will throw
     *  a TerminateProcessException.
     */
    protected void _cancelDelay() {
        synchronized(_internalLock) {
            if (_delayed) {
                _cancelDelay = true;
                _delayed = false;
                _internalLock.notifyAll();
            }
        }
    }

    /** Resume a delayed actor. This method is only called by CSPDirector
     *  after time has sufficiently advanced.
     */
    protected void _continue() {
        if (_delayed == false) {
            throw new InvalidStateException("CSPActor._continue() " +
                    "called on an actor that was not delayed: " + getName());
        }
        // NOTE: perhaps this notifyAll() should be called in another
        // thread?  However, the internal lock is private, so it seems
        // that if this class is correctly written, that is not necessary.
        synchronized(_internalLock) {
            _delayed = false;
            _internalLock.notifyAll();
        }
    }

    /** Wait for deadlock to occur. The current model time will not
     *  advance while this actor is delayed. This method may be useful if
     *  an actor wishes to delay itself until some topology changes
     *  have been carried out.
     */
    protected void _waitForDeadlock() {
        try {
	    synchronized(_internalLock) {
	        _delayed = true;
		((CSPDirector)getDirector())._actorDelayed(0.0, this);
		while(_delayed) {
		    _internalLock.wait();
		}
	    }
	} catch (InterruptedException ex) {
            throw new TerminateProcessException("CSPActor interrupted " +
                    "while waiting for deadlock." );
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Flag that causes the delay() method to abort with an exception.
    private boolean _cancelDelay = false;

    // This object is in charge of all conditional rendezvous issues.
    private ConditionalBranchController _conditionalBranchController = null;

    // Flag indicating this actor is delayed. It needs to be accessible
    // by the director.
    private boolean _delayed = false;

    // This lock is only used internally by the actor. It is used to
    // control a delayed actor.
    private Object _internalLock = new Object();
}
