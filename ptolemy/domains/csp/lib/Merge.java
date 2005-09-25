/* An actor that merges input sequences onto one output sequence.

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

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.Token;
import ptolemy.domains.csp.kernel.CSPActor;
import ptolemy.domains.csp.kernel.ConditionalBranch;
import ptolemy.domains.csp.kernel.ConditionalReceive;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// 

/**
 This actor merges any number of input sequences onto one output
 sequence. It begins by being willing to rendezvous with any input.
 When it receives an input, it then becomes willing to rendezvous
 with the output. After successfully delivering the input token
 to the output, it returns again to being willing to rendezvous
 with any input.
 This actor is designed for use in the CSP domain, where it will
 execute in its own thread.
 <p>
 The behavior of this actor is similar to that of the ResourcePool
 actor in CSP, except that the ResourcePool actor does buffering.
 That is, the ResourcePool is always ready to rendezvous with
 any input, while this actor is ready to rendezvous with an
 input only after it has delivered the previous input token to
 the output.
 
 @author Edward A. Lee
 @see ResourcePool
 @version $Id$
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)

 */
public class Merge extends CSPActor {
    
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
    public Merge(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        output = new TypedIOPort(this, "output", false, true);

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        
        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-10,20 10,10 10,-10, -10,-20\" "
                + "style=\"fill:red\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port. The type must be at least that of the input.
     */
    public TypedIOPort output;

    /** The input port. This is a multiport that accepts any type.
     */
    public TypedIOPort input;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Perform a conditional rendezvous on any <i>input</i> channel,
     *  and then take the resulting token and send it via rendezvous
     *  on the <i>output</i>.
     *  @exception IllegalActionException If an error occurs during
     *   executing the process.
     *  @exception TerminateProcessException If the process termination
     *   is requested by the director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (_debugging) {
            _debug("Ready to rendezvous with an input.");
        }
        int numberOfConditionals = input.getWidth();
        ConditionalBranch[] branches = new ConditionalBranch[numberOfConditionals];
        for (int i = 0; i < input.getWidth(); i++) {
            // The branch has channel i and ID i.
            branches[i] = new ConditionalReceive(input, i, i);
            if (_debugging && _VERBOSE_DEBUGGING) {
                branches[i].addDebugListener(this);
            }
        }
        int successfulBranch = chooseBranch(branches);
        if (_debugging && _VERBOSE_DEBUGGING) {
            for (int i = 0; i < branches.length; i++) {
                branches[i].removeDebugListener(this);
            }
        }
        if (successfulBranch < 0) {
            // No input rendezvous preformed.
            _branchEnabled = false;
        } else {
            // Rendezvous occurred with a input.
            _branchEnabled = true;
            Token received = branches[successfulBranch].getToken();
            if (_debugging) {
                _debug("Received input on channel "
                        + successfulBranch
                        + ": "
                        + received);
                _debug("Sending to the output.");
            }
            output.send(0, received);
        }
    }
    
    /** Return true unless no input was received in
     *  the most recent invocation of fire().
     *  @return True if another iteration can occur.
     */
    public boolean postfire() {
        super.postfire();
        return _branchEnabled;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** Indicator that a branch was successfully enabled in the fire() method. */
    private boolean _branchEnabled;
    
    /** Flag to set verbose debugging messages. */
    private static boolean _VERBOSE_DEBUGGING = true;
}
