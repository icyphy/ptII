/* An actor representing a resource pool with a specified number of resources.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.domains.csp.lib;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.csp.kernel.CSPActor;
import ptolemy.domains.csp.kernel.ConditionalBranch;
import ptolemy.domains.csp.kernel.ConditionalReceive;
import ptolemy.domains.csp.kernel.ConditionalSend;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
////

/**
 FIXME
 <p>
 If the capacity changes during execution, and the buffer already
 contains more tokens than the new capacity, then no tokens are lost,
 but no new tokens are accepted at the input until the number of
 buffered tokens drops below the capacity.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)

 */
public class Buffer extends CSPActor {

    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Buffer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        capacity = new Parameter(this, "capacity");
        capacity.setTypeEquals(BaseType.INT);
        capacity.setExpression("1");

        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The capacity of the buffer. To provide "infinite" capacity,
     *  set this to a negative number.
     */
    public Parameter capacity;

    /** The input port.
     */
    public TypedIOPort input;

    /** The output port. The type of this output is constrained
     *  to be at least that of the input.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the buffer is not full, then accept any input rendezvous;
     *  if the buffer has tokens, then accept any output rendezvous.
     *  If the rendezvous is with the input, then append the input
     *  token to the end of the buffer. If the rendezvous is with the
     *  output, then take the first token from the buffer and send
     *  it to the output.
     *  @exception IllegalActionException If an error occurs during
     *   executing the process, or if the capacity is zero.
     *  @exception TerminateProcessException If the process termination
     *   is requested by the director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        int bufferSize = _buffer.size();
        if (_debugging) {
            _debug("Buffer size: " + bufferSize);
        }
        int capacityValue = ((IntToken) capacity.getToken()).intValue();
        if (capacityValue == 0) {
            throw new IllegalActionException(this,
                    "Capacity is required to be greater than zero.");
        }

        // Deal with simple cases first.
        // If the buffer is full and there is a token, send it to the output.
        if (bufferSize >= capacityValue // Buffer is full.
                && bufferSize >= 1) { // There is a token.
            Token token = (Token) _buffer.remove(0);
            if (_debugging) {
                _debug("Sending token: " + token);
            }
            output.send(0, token);
            _branchEnabled = true;
        } else if (bufferSize == 0) { // Buffer is empty.
            if (_debugging) {
                _debug("Waiting for input.");
            }
            Token token = input.get(0);
            if (_debugging) {
                _debug("Received token: " + token);
            }
            _buffer.add(token);
            _branchEnabled = true;
        } else {
            // Rendezvous with either the input or output
            // and act accordingly.
            ConditionalBranch[] branches = new ConditionalBranch[2];
            branches[0] = new ConditionalReceive(input, 0, 0);
            Token token = (Token) _buffer.get(0);
            branches[1] = new ConditionalSend(output, 0, 1, token);
            if (_debugging && _VERBOSE_DEBUGGING) {
                branches[0].addDebugListener(this);
                branches[1].addDebugListener(this);
            }
            if (_debugging) {
                _debug("Waiting for input, or to send output token: " + token);
            }
            int successfulBranch = chooseBranch(branches);
            if (_debugging && _VERBOSE_DEBUGGING) {
                branches[0].removeDebugListener(this);
                branches[1].removeDebugListener(this);
            }
            if (successfulBranch < 0) {
                _branchEnabled = false;
            } else if (successfulBranch == 0) {
                // Rendezvous occurred with the input.
                _branchEnabled = true;
                Token received = branches[0].getToken();
                if (_debugging) {
                    _debug("Received token: " + received);
                }
                _buffer.add(received);
            } else {
                // Rendezvous occurred with the output.
                _branchEnabled = true;
                _buffer.remove(0);
                if (_debugging) {
                    _debug("Sent a token: " + token);
                }
            }
        }
    }

    /** Clear the buffer.
     *  @exception IllegalActionException Not thrown in this base class,
     *  but might be in a derived class.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _buffer.clear();
    }

    /** Return true unless none of the branches were enabled in
     *  the most recent invocation of fire().
     *  @return True if another iteration can occur.
     *  @exception IllegalActionException If thrown by the base class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // FIXME: We deliberately ignore the return value of super.postfire()
        // here because CSPActor.postfire() returns false.
        super.postfire();

        // Note that CSPActor.postfire() also ignores the return value
        // AtomicActor.postfire(), which means that if a stop is
        // requested, then it is ignored.
        // However, if we check the value of AtomicActor._stopRequested
        // and return false if _stopRequested is true, then csp/test/auto/Sequencing.xml fails
        // if (!_stopRequested) {
        //    return false;
        //}

        return _branchEnabled;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The current buffer. */
    private List _buffer = new LinkedList();

    /** Indicator that a branch was successfully enabled in the fire() method. */
    private boolean _branchEnabled;

    /** Flag to set verbose debugging messages. */
    private static boolean _VERBOSE_DEBUGGING = true;
}
