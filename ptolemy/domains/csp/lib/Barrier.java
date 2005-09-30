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

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.Token;
import ptolemy.domains.csp.kernel.CSPDirector;
import ptolemy.domains.csp.kernel.CSPReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
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
 then an exception is thrown.
 
 @author Edward A. Lee
 @version $Id$
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)

 */
public class Barrier extends TypedAtomicActor {
    
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

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);

        output = new TypedIOPort(this, "output", false, true);
        output.setMultiport(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port, which is a multiport that can accept any data type.
     */
    public TypedIOPort input;

    /** The output port, which is a multiport whose type is at least that
     *  of the input port.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Perform a multiway rendezvous with all input channels, collect
     *  one input token from each channel, and then perform a multiway
     *  rendezvous with the output channels, providing that data.
     *  @exception IllegalActionException If the input width is zero.
     *  @exception TerminateProcessException If the process termination
     *   is requested by the director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.getWidth() == 0) {
            throw new IllegalActionException(this, "Barrier requires at least one input.");
        }
        Director director = getDirector();
        if (!(director instanceof CSPDirector)) {
            throw new IllegalActionException(this,
                    "Barrier can only be used with CSPDirector.");
        }
        if (_debugging) {
            _debug("Performing multiway rendezvous on the input channels.");
        }
        Token[][] tokens = CSPReceiver.getFromAll(
                input.getReceivers(), (CSPDirector)director);
        if (_debugging) {
            _debug("Input yielded the tokens: " + tokens);
        }
        if (output.getWidth() > 0) {
            if (_debugging) {
                _debug("Performing multiway rendezvous on the output channels.");
            }
            CSPReceiver.putToAll(
                    tokens, output.getRemoteReceivers(), (CSPDirector)director);
        }
    }
}
