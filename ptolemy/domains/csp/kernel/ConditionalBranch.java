/* Parent class of guarded communication branches.

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
@AcceptedRating Green (kienhuis@eecs.berkeley.edu)

*/

package ptolemy.domains.csp.kernel;

import ptolemy.actor.*;
import ptolemy.data.Token;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// ConditionalBranch
/**
Base class for classes representing guarded communication statements. A
guarded communication statement is of the form
<P>
      <CENTER>guard; communication => statements </CENTER>
<P>
If the guard is true, or absent which implies true, then the branch
is enabled. Guarded communication statements are used to perform
both forms of conditional communication constructs: "conditional if" (CIF)
and "conditional do" (CDO). These constructs are analogous to,
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
in a separate thread. For rendezvous, the receiver is the key
synchronization point.
<p>
Conditional branches are designed to be used once. Upon instantiation,
they are given the guard, the port and channel over which to communicate,
and the identification number of the branch according to the controller.
The port and the channel together define the CSPReceiver with which to
rendezvous. The ConditionalBranchController, that controls this branch,
is assumed to be contained by the container of the port.
<p>
@author  Neil Smyth
@version $Id$
*/

public abstract class ConditionalBranch {

    /** Create a guarded communication statement. This class contains
     *  all of the information necessary to carry out a guarded
     *  communication statement, with the exception of the type of
     *  communication. The receiver is set in the subclass as it
     *  is subject to communication specific tests.
     *  @param guard The guard for the guarded communication statement
     *   represented by this object.
     *  @param port The IOPort that contains the channel to
     *   try an communicate through.
     *  @param branch The identification number assigned to this branch
     *   upon creation by the CSPActor.
     *  @exception IllegalActionException If the actor that contains
     *   the port is not of type CSPActor.
     */
    public ConditionalBranch(boolean guard, IOPort port, int branchID)
            throws IllegalActionException {
        Nameable tmp = port.getContainer();
        if (!(tmp instanceof ConditionalBranchActor)) {
            throw new IllegalActionException(port,
		    "A conditional branch can only be created" +
		    "with a port contained by ConditionalBranchActor");
        }
        _branchID = branchID;
        _guard = guard;
        _controller =
	    ((ConditionalBranchActor)tmp).getConditionalBranchController();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
    public ConditionalBranchController getController() {
        return _controller;
    }

    /** Return the CSPReceiver this branch is trying to rendezvous with.
     *  @return The CSPReceiver this branch is trying to rendezvous with.
     */
    public CSPReceiver getReceiver() {
        return _receiver;
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

    ///////////////////////////////////////////////////////////////////
    ////                      package friendly methods                  ////

    /** Set a flag indicating this branch should fail.
     *  @param value Boolean indicating whether this branch is still alive.
     */
    void setAlive(boolean value) {
        _alive = value;
    }

    /** Set the CSPReceiver this branch is trying to rendezvous with.
     *  This method should only be called from derived classes.
     *  @param rec The CSPReceiver this branch is trying to rendezvous with.
     */
    void setReceiver(CSPReceiver rec) {
        _receiver = rec;
    }

    /** Set the token contained by this branch. For a ConditionalSend
     *  it is set upon creation, and set to null after the rendezvous.
     *  For a ConditionalReceive it is set after the rendezvous has
     *  occurred, and is null before that.
     *  @param token The token to be contained by this branch.
     */
    void setToken(Token token) {
        _token = token;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Called by subclasses to wait. Register with the containing actor
     *  that a conditional branch has blocked and then wait to be notified.
     *  Wrap the wait() call (on the receiver associated with this branch)
     *  between checks on the state of the receiver. After awaking from
     *  waiting, register with the containing actor that this conditional
     *  branch is no longer blocked.
     * @exception InterruptedException If this method is interrupted
     *  while waiting.
     */
    protected void _registerBlockAndWait() throws InterruptedException {
        // getController()._branchBlocked();
        getReceiver()._checkFlagsAndWait();
        // getController()._branchUnblocked();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables                 ////

    // The guard for this guarded communication statement.
    protected boolean _guard;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The identification number of this branch (according to its controller)
    private int _branchID;

    // Has another branch successfully rendezvoused? If so, then _alive
    // is set to false. Otherwise, this branch still can potentially
    // rendezvous. _alive remains true until it is no longer possible
    // for this branch to successfully rendezvous.
    private boolean _alive = true;

    // The controller of this thread is trying to perform a conditional
    // rendezvous for.
    private ConditionalBranchController _controller;

    // The receiver this thread is trying to rendezvous with. It is immutable.
    private CSPReceiver _receiver;

    // The Token transferred in a rendezvous.
    private Token _token;
}
