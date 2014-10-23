/* Parent class of guarded communication branches.

 Copyright (c) 1998-2014 The Regents of the University of California.
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


 */
package ptolemy.domains.csp.kernel;

import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.data.Token;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.Debuggable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;

///////////////////////////////////////////////////////////////////
//// ConditionalBranch

/**
 Base class for classes representing guarded communication that occurs
 either conditionally (one statement from a group is executed) or as a multiway
 rendezvous (all statements from a group are executed).
 Concrete subclasses are expected to implement the Runnable interface,
 and the "execution" of the communication is in the run() method.
 A guarded communication statement is of the form
 <P>
 <CENTER>guard; communication => statements </CENTER>
 <P>
 If the guard is true, or absent which implies true, then the branch
 is enabled. If a branch is not enabled, then this it does not participate
 in the group (equivalently, it could not be created or put in the group).
 A group is formed and executed by calling chooseBranch() in a ConditionalBranchController
 or MultiwayBranchController.
 <p>
 Guarded communication statements of the conditional sort are used to perform
 two forms of conditional communication constructs from classical
 CSP: "conditional if" (CIF) and
 "conditional do" (CDO). These constructs are analogous to,
 but different from, the common <I>if</I> and <I>do</I> statements.
 Each guarded communication statement is one branch of a CIF or CDO.
 <p>
 A CDO has the form
 <P>
 CDO {
 <br>G1; C1 => S1;
 <br>[]
 <br>G2; C2 => S2;
 <br>[]
 <br>...
 <br>}
 <P>
 The G1, G2 etc. represent the guards. The C1, C2 etc. represent the
 communication associated with that branch, and may be either a send()
 or a get(). The S1, S2 etc. represent the blocks of statements
 associated with that branch. They are executed if that branch is
 successful. The "[]" hints at the fact that the guards are all evaluated
 in parallel (as opposed to sequentially in a common <I>if</I> statement).
 <p>
 While at least one of the branches is enabled, the construct continues
 to evaluate and execute one of the enabled branches. If more than one
 branch is enabled, the first branch to be able to rendezvous succeeds
 and its statements are executed. Note that this construct is
 nondeterministic as it may be  a race condition that determines
 which branch is successful. The CIF is similar to the CDO except that
 it is only evaluated once.
 <p>
 The communication part of a guarded communication statement can be
 either a send() or a get(). There are thus two subclasses of this
 class, each representing a guarded communication statement for one of
 the communication primitives. The subclasses are ConditionalSend and
 ConditionalReceive.
 <p>
 If more than one branch is enabled, each enabled branch is executed
 in a separate thread.
 <p>
 Conditional branches are designed to be used once. Upon instantiation,
 they are given the guard, the port and channel over which to communicate,
 and the identification number of the branch according to the controller.
 The port and the channel together define the CSPReceiver with which to
 rendezvous. The ConditionalBranchController, that controls this branch,
 is assumed to be contained by the container of the port.
 <p>
 @author  Neil Smyth and Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (nsmyth)
 @Pt.AcceptedRating Green (kienhuis)
 */
public abstract class ConditionalBranch implements Debuggable {

    /** Create a guarded communication statement. This class contains
     *  all of the information necessary to carry out a guarded
     *  communication statement, with the exception of the type of
     *  communication. The receiver is set in the subclass as it
     *  is subject to communication specific tests.
     *  @param guard The guard for the guarded communication statement
     *   represented by this object.
     *  @param port The IOPort that contains the channel to
     *   try an communicate through.
     *  @param branchID The identification number assigned to this branch
     *   upon creation by the CSPActor.
     *  @exception IllegalActionException If the actor that contains
     *   the port is not of type CSPActor.
     */
    public ConditionalBranch(boolean guard, IOPort port, int branchID)
            throws IllegalActionException {
        this(guard, port, branchID, null);
    }

    /** Create a guarded communication statement. This class contains
     *  all of the information necessary to carry out a guarded
     *  communication statement, with the exception of the type of
     *  communication. The receiver is set in the subclass as it
     *  is subject to communication specific tests.
     *  This constructor allows actors which do not implement the
     *  BranchActor interface access to CSP functionality
     *  by passing their own ConditionalBranchController.
     *  @param guard The guard for the guarded communication statement
     *   represented by this object.
     *  @param port The IOPort that contains the channel to
     *   try an communicate through.
     *  @param branchID The identification number assigned to this branch
     *   upon creation by the CSPActor.
     *  @param controller The controller associated with this branch, or
     *   null to use the one provided by the container of the port.
     *  @exception IllegalActionException If the actor that contains
     *   the port is not of type CSPActor, or if no controller is
     *   provided, and the actor is not an instance of BranchActor.
     */
    public ConditionalBranch(boolean guard, IOPort port, int branchID,
            ConditionalBranchController controller)
                    throws IllegalActionException {
        _branchID = branchID;
        _guard = guard;
        _controller = controller;
        _port = port;
        if (_controller == null) {
            Nameable portContainer = port.getContainer();
            if (!(portContainer instanceof BranchActor)) {
                throw new IllegalActionException(port,
                        "A conditional branch can only be created"
                                + "with a port contained by BranchActor.");
            }
            _controller = ((BranchActor) portContainer).getBranchController();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a debug listener.
     *  If the listener is already in the list, do not add it again.
     *  @param listener The listener to which to send debug messages.
     *  @see #removeDebugListener(DebugListener)
     */
    @Override
    public void addDebugListener(DebugListener listener) {
        if (_debugListeners == null) {
            _debugListeners = new LinkedList();
        }
        if (_debugListeners.contains(listener)) {
            return;
        } else {
            _debugListeners.add(listener);
        }
        _debugging = true;
    }

    /** Returns the guard for this guarded communication statement.
     *  If it is true the branch is said to be enabled.
     *  @return True if the branch is  enabled.
     */
    public boolean getGuard() {
        return _guard;
    }

    /** Returns the identification number of this branch(according to its
     *  controller).
     *  @return The identification number of this branch.
     */
    public int getID() {
        return _branchID;
    }

    /** Return the controller that manges conditional rendezvous for this
     *  branch when performing a CIF or CDO.
     *  @return The controller that manages conditional rendezvous for
     *  this branch.
     */
    public AbstractBranchController getController() {
        return _controller;
    }

    /** Return the port associated with this conditional branch.
     *  @return The port specified in the constructor.
     */
    public IOPort getPort() {
        return _port;
    }

    /** Return an array with all the receivers that
     *  this branch is trying to rendezvous with.
     *  In this base class, this is an array with one element,
     *  the receiver returned by getReceiver(). However, in the
     *  ConditionalSend derived class, the array may contain more
     *  than one receiver.
     *  @return An array of receivers that this branch is trying to rendezvous with.
     */
    public Receiver[] getReceivers() {
        return _receivers;
    }

    /** Return the token contained by this branch. For a ConditionalSend
     *  it is set upon creation, and set to null after the rendezvous.
     *  For a ConditionalReceive it is set after the rendezvous has
     *  occurred, and is null before that.
     *  @return The token contained by this branch.
     */
    public Token getToken() {
        return _token;
    }

    /** Boolean indicating if this branch is still alive. If it is false, it
     *  indicates another conditional branch was able to rendezvous before
     *  this branch, and this branch should stop trying to rendezvous with
     *  its receiver and terminate. If it is true, the branch should
     *  continue trying to rendezvous.
     *  @return True if this branch is still alive.
     */
    public boolean isAlive() {
        return _alive;
    }

    /** Unregister a debug listener.  If the specified listener has not
     *  been previously registered, then do nothing.
     *  @param listener The listener to remove from the list of listeners
     *   to which debug messages are sent.
     *  @see #addDebugListener(DebugListener)
     */
    @Override
    public void removeDebugListener(DebugListener listener) {
        if (_debugListeners == null) {
            return;
        }
        _debugListeners.remove(listener);

        if (_debugListeners.size() == 0) {
            _debugging = false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      package friendly methods             ////

    /** Set a flag indicating this branch should fail.
     *  @param value Boolean indicating whether this branch is still alive.
     */
    protected void _setAlive(boolean value) {
        _alive = value;
    }

    /** Set the receivers that this branch is trying to rendezvous with.
     *  This method should only be called from derived classes.
     *  For a conditional receiver, the argument should be an array
     *  with only one receiver. For a conditional send, the argument
     *  may have more than one receiver, in which case a multi-way
     *  rendezvous is being specified.
     *  @param receivers The instances of CSPReceiver that
     *   this branch is trying to rendezvous with.
     */
    protected void _setReceivers(Receiver[] receivers) {
        _receivers = receivers;
    }

    /** Set the token contained by this branch. For a ConditionalSend
     *  it is set upon creation, and set to null after the rendezvous.
     *  For a ConditionalReceive it is set after the rendezvous has
     *  occurred, and is null before that.
     *  @param token The token to be contained by this branch.
     */
    protected void _setToken(Token token) {
        _token = token;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Send a debug message to all debug listeners that have registered.
     *  By convention, messages should not include a newline at the end.
     *  The newline will be added by the listener, if appropriate.
     *  @param message The message.
     */
    protected final void _debug(String message) {
        if (_debugging) {
            Iterator listeners = _debugListeners.iterator();
            while (listeners.hasNext()) {
                ((DebugListener) listeners.next()).message(message);
            }
        }
    }

    /** Return true if this conditional branch is ready to rendezvous.
     *  @return True if the conditional branch is ready to rendezvous.
     */
    protected abstract boolean _isReady();

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Flag that is true if there are debug listeners. */
    protected boolean _debugging = false;

    /** The guard for this guarded communication statement. */
    protected boolean _guard;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Has another branch successfully rendezvoused? If so, then _alive
     *  is set to false. Otherwise, this branch still can potentially
     * rendezvous. _alive remains true until it is no longer possible
     * for this branch to successfully rendezvous.
     */
    private boolean _alive = true;

    /** The identification number of this branch
     * (according to its controller).
     */
    private int _branchID;

    /** The controller of this thread is trying to perform a conditional
     *  rendezvous for.
     */
    private AbstractBranchController _controller;

    /** The list of DebugListeners registered with this object. */
    private LinkedList _debugListeners = null;

    /** The port specified in the constructor. */
    private IOPort _port;

    /** The receivers that this thread is trying to rendezvous with. */
    private Receiver[] _receivers;

    /** The Token transferred in a rendezvous. */
    private Token _token;
}
