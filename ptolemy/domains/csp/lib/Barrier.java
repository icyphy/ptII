/* An actor for barrier synchronization.

 Copyright (c) 1998-2005 The Regents of the University of California.
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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.csp.kernel.AbstractBranchController;
import ptolemy.domains.csp.kernel.ConditionalBranch;
import ptolemy.domains.csp.kernel.ConditionalBranchActor;
import ptolemy.domains.csp.kernel.ConditionalReceive;
import ptolemy.domains.csp.kernel.ConditionalSend;
import ptolemy.domains.csp.kernel.MultiwayBranchController;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// 

/**
 This actor implements multiway rendezvous on all channels
 of the input port, collects the tokens, and then performs
 a multiway rendezvous on all channels of the output port,
 sending the collected data. A token provided on an
 input channel is sent to the corresponding output channel.
 If there are fewer output channels than input channels, then
 the corresponding input data are discarded.
 If there are fewer input channels than output channels,
 then the last input channel provides the token for the
 remaining ones. If there are no input channels,
 then the output is an instance of Token.
 
 @author Edward A. Lee
 @version $Id$
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)

 */
public class Barrier extends TypedAtomicActor implements
        ConditionalBranchActor {
    
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
    public Barrier(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _branchController = new MultiwayBranchController(this);

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        // Create a parameter that indicates to the director that the
        // receivers in this port should be treated as a group for the
        // purposes of synchronizing their send and receive actions.
        Parameter groupReceivers = new Parameter(input, "_groupReceivers");
        groupReceivers.setPersistent(false);

        output = new TypedIOPort(this, "release", false, true);
        output.setMultiport(true);
        // Create a parameter that indicates to the director that the
        // receivers in this port should be treated as a group for the
        // purposes of synchronizing their send and receive actions.
        groupReceivers = new Parameter(output, "_groupReceivers");
        groupReceivers.setPersistent(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.
     */
    public TypedIOPort input;

    /** The output port.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Determine which branch succeeds with a rendezvous. Pass
     *  the branches to ConditionalBranchController to decide.
     *  <p>
     *  @param branches The set of conditional branches involved.
     *  @return True if the branches all succeed, false if any
     *   them was terminated before completing the rendezvous.
     *  @exception IllegalActionException If the rendezvous fails
     *   (e.g. because of incompatible types).
     */
    public boolean executeBranches(ConditionalBranch[] branches) throws IllegalActionException {
        return _branchController.executeBranches(branches);
    }

    /** If there are available resources, then perform a conditional
     *  branch on any <i>release</i> input or <i>grant</i> output. If the selected
     *  branch is a release input, then add the provided token to the
     *  end of the resource pool. If it is a grant output, then remove
     *  the first element from the resource pool and send it to the output.
     *  If there are no available resources, then perform a conditional branch
     *  only on the release inputs.
     *  @exception IllegalActionException If an error occurs during
     *   executing the process.
     *  @exception TerminateProcessException If the process termination
     *   is requested by the director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (_debugging && _VERBOSE_DEBUGGING && !_listeningToBranchController) {
            _branchController.addDebugListener(this);
            _listeningToBranchController = true;
        } else {
            _branchController.removeDebugListener(this);
            _listeningToBranchController = false;
        }
        // FIXME: What if the input width is zero?
        ConditionalBranch[] branches = new ConditionalBranch[input.getWidth()];
        for (int i = 0; i < input.getWidth(); i++) {
            // The branch has channel i and ID i.
            branches[i] = new ConditionalReceive(input, i, i);
            if (_debugging && _VERBOSE_DEBUGGING) {
                branches[i].addDebugListener(this);
            }
        }
        if (_debugging) {
            _debug("Performing multiway rendezvous on the input channels.");
        }
        if (!executeBranches(branches)) {
            if (_debugging) {
                _debug("At least one input rendezvous was terminated.");
            }
            return;
        }
        if (_debugging) {
            _debug("Input channels completed.");
            if (_VERBOSE_DEBUGGING) {
                for (int i = 0; i < branches.length; i++) {
                    branches[i].removeDebugListener(this);
                }
            }
        }
        Token[] data = new Token[input.getWidth()];
        for (int i = 0; i < input.getWidth(); i++) {
            data[i] = branches[i].getToken();
            if (_debugging) {
                _debug("Completed read input from channel " + i + ": " + data[i]);
            }
            if (data[i] == null) {
                throw new InternalErrorException("Input data is null!");
            }
        }
        
        if (output.getWidth() > 0) {
            branches = new ConditionalBranch[output.getWidth()];
            // FIXME: Is this the right default?
            Token token = new Token();
            for (int i = 0; i < output.getWidth(); i++) {
                if (i < input.getWidth()) {
                    token = data[i];
                }
                if (_debugging) {
                    _debug("Sending output to channel " + i + ": " + token);
                }
                branches[i] = new ConditionalSend(output, i, i, token);
                if (_debugging && _VERBOSE_DEBUGGING) {
                    branches[i].addDebugListener(this);
                }
            }
            if (_debugging) {
                _debug("Performing multiway rendezvous on the output channels.");
            }
            if (executeBranches(branches)) {
                if (_debugging) {
                    _debug("Output channels completed.");
                }
            } else {
                if (_debugging) {
                    _debug("Output channels failed.");
                }
            }                
            if (_debugging && _VERBOSE_DEBUGGING) {
                for (int i = 0; i < branches.length; i++) {
                    branches[i].removeDebugListener(this);
                }
            }
        }
    }
   
    /** Return the conditional branch control of this actor.
     */
    public AbstractBranchController getBranchController() {
        return _branchController;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The controller for multiway branches. */
    private MultiwayBranchController _branchController;
    
    /** Boolean flag indicating that we are already listening to the branch controller. */
    private boolean _listeningToBranchController = false;
    
    /** Flag to set verbose debugging messages. */
    private static boolean _VERBOSE_DEBUGGING = true;
}
